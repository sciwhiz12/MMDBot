package com.mcmoddev.mmdbot.updatenotifiers.fabric;

import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.TimerTask;

public class FabricApiUpdateNotifier extends TimerTask {

    private String lastLatest;

    public FabricApiUpdateNotifier() {
        FabricVersionHelper.update();
        lastLatest = FabricVersionHelper.getLatestApi();
    }

    @Override
    public void run() {
        String latest = FabricVersionHelper.getLatestApi();

        final long guildId = MMDBot.getConfig().getGuildID();
        final Guild guild = MMDBot.getInstance().getGuildById(guildId);
        final long channelId = MMDBot.getConfig().getChannelIDForgeNotifier();
        final TextChannel channel = guild.getTextChannelById(channelId);

        if (!lastLatest.equals(latest)) {
            lastLatest = latest;

            final EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("New Fabric API release available!");
            embed.setDescription(latest);
            embed.setColor(Color.WHITE);
            embed.setTimestamp(Instant.now());
            channel.sendMessage(embed.build()).queue();
        }

        lastLatest = latest;
    }
}
