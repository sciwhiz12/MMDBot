package com.mcmoddev.mmdbot.oldchannels;

import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.OLD_CHANNELS;

public class ChannelMessageChecker extends TimerTask {
    @Override
    public void run() {
        final long guildId = MMDBot.getConfig().getGuildID();
        final Guild guild = MMDBot.getInstance().getGuildById(MMDBot.getConfig().getGuildID());
        if (guild == null) {
            LOGGER.error(OLD_CHANNELS, "Error while checking for old channels: guild {} doesn't exist!", guildId);
            return;
        }
        final Instant currentTime = Instant.now();

        LOGGER.info(OLD_CHANNELS, "Checking for old channels...");
        OldChannelsHelper.setReady(false);
        OldChannelsHelper.clear();

        Predicate<TextChannel> permissionCheck = channel -> {
            if (!channel.getGuild().getSelfMember().hasAccess(channel)) return false;
            if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_HISTORY)) return true;
            LOGGER.warn("We can access {} but no MESSAGE_HISTORY permission; skipping", channel);
            return false;
        };

        RestAction.allOf(guild.getTextChannels()
            .parallelStream()
            .filter(permissionCheck)
            .map(channel -> channel.getIterableHistory()
                .limit(100)
                .mapToResult()
                .map(result -> result
                    .onFailure(ex -> LOGGER.warn(OLD_CHANNELS, "Exception while retrieving messages for channel {}", channel, ex))
                    .map(messages -> messages.stream().parallel()
                        .filter(message -> !message.isWebhookMessage())
                        .findFirst()
                    )
                    .onSuccess(msgOpt ->
                        msgOpt.ifPresent(message ->
                            OldChannelsHelper.put(message.getChannel().getIdLong(), ChronoUnit.DAYS.between(message.getTimeCreated().toInstant(), currentTime))
                        )
                    )
                )
            )
            .collect(Collectors.toList()))
            .queue(success -> {
                LOGGER.info(OLD_CHANNELS, "Checked for old channels, command is now ready.");
                OldChannelsHelper.setReady(true);
            });
    }
}
