package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.OLD_CHANNELS;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 *
 */
public final class CmdOldChannels extends Command {

    /**
     *
     */
    public CmdOldChannels() {
        super();
        name = "old-channels";
        help = "Gives channels which haven't been used in an amount of days given as an argument (default 60)." +
            "Usage: " + MMDBot.getConfig().getMainPrefix() + "old-channels [threshold] [channel or category list, separated by spaces]";
        hidden = true;
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final Guild guild = event.getGuild();
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel outputChannel = event.getTextChannel();
        final List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" "))); //I have to do this so we can use `remove` later

        final Predicate<TextChannel> permissionCheck = channel -> {
            if (!channel.getGuild().getSelfMember().hasAccess(channel)) return false;
            if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_HISTORY)) return true;
            LOGGER.warn(OLD_CHANNELS, "{} is accessible but bot does not have MESSAGE_HISTORY permission for that channel; skipping", channel);
            return false;
        };

        final int dayThreshold = args.size() > 0 && args.get(0).matches("-?\\d+") ? Integer.parseInt(args.remove(0)) : 60;

        List<String> toCheck = new ArrayList<>(args);

        embed.setTitle("Retrieving channel messages and calculating...")
            .appendDescription("Please wait until the operation finishes.\n")
            .appendDescription("If this takes too long, please contact the bot maintainers.")
            .setColor(Color.ORANGE);

        final Instant currentTime = Instant.now();

        outputChannel.sendMessage(embed.build())
            .queue(outputMsg -> {

                LOGGER.info(OLD_CHANNELS, "Checking for old channels...");
                CompletableFuture.allOf(guild.getTextChannels()
                    .parallelStream() // Parallel stream, so we do this all in parallel
                    .filter(isListedChannel(toCheck)) // Check if this channel is listed in the command arguments
                    .filter(permissionCheck) // Check if we have sufficient permission
                    .map(channel -> channel.getIterableHistory()
                        .takeWhileAsync(Message::isWebhookMessage) // Retrieve while we get webhook messages

                        // The list should contain either no webhook messages (most recent message is normal) or some amount
                        .thenCompose(messages -> !messages.isEmpty() ? // If there are webhook messages...
                            completedFuture(messages) : // Pass along the webhook messages, else...
                            channel.getHistory().retrievePast(1).submit() // Get the latest message of the channel
                        )

                        // We should now have at least 1 message in the list, or it's empty (the channel is empty)
                        .thenCompose(messages -> !messages.isEmpty() ? // If we have do have a message in the list...
                            channel.getHistoryBefore(messages.get(messages.size() - 1), 1)
                                .map(MessageHistory::getRetrievedHistory)
                                .submit() : // Get that's message's immediately preceding message (The parent), else...
                            completedFuture(messages) // Pass along the empty list
                        )

                        // We either have an empty list (channel is empty), or a list with 1 entry (a non-webhook message)
                        .thenApply(messages -> !messages.isEmpty() ? // If a message was found...
                            new ChannelData(channel, DAYS.between(messages.get(0).getTimeCreated().toInstant(), currentTime)) : // Get the days between the sent date of the message and now, else...
                            new ChannelData(channel, -1)) // Return an empty channel data, where -1 indicates no message was found

                        // Add to the embed
                        .thenAccept(data -> {
                            if (data.days > dayThreshold) {
                                embed.addField("#" + data.channel.getName(), data.days + " day" + (data.days == 1 ? "" : "s"), true);
                            } else if (data.days == -1) {
                                embed.addField("#" + data.channel.getName(), "_No message found_", true);
                            }
                        })
                    )
                    .toArray(CompletableFuture[]::new)
                ).thenAccept($ -> {
                    LOGGER.info(OLD_CHANNELS, "Finished checking for old channels, {} inactive channel(s) found", embed.getFields().size());

                    embed.setTitle("Days since last message in channel")
                        .setDescription(null)
                        .setColor(Color.YELLOW);

                    outputMsg.editMessage(embed.build()).queue();
                });

            });
    }

    private Predicate<TextChannel> isListedChannel(List<String> list) {
        return (channel) -> list.isEmpty() ||
            (channel.getParent() != null && list.contains(channel.getParent().getName().replace(' ', '-')))
            || list.contains(channel.getName());
    }

    private static class ChannelData {
        private final TextChannel channel;
        private final long days;

        private ChannelData(final TextChannel channel, final long days) {
            this.channel = channel;
            this.days = days;
        }
    }
}
