package com.mcmoddev.bot.jobs;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.OldChannelsHelper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TimerTask;

public class ChannelMessageChecker extends TimerTask {

	private final long guildId;

	public ChannelMessageChecker(final long guildIdIn) {
		this.guildId = guildIdIn;
	}

	@Override
	public void run() {
		final Guild guild = MMDBot.getJda().getGuildById(guildId);
		if (guild == null) {
			MMDBot.LOGGER.error("Error while checking for old channels: guild {} doesn't exist!", guildId);
			return;
		}
		final List<TextChannel> channelList = guild.getTextChannels();
		OldChannelsHelper.clear();

		for (TextChannel channel : channelList) {
			final MessageHistory history = channel.getHistory();
			List<Message> latestMessages = history.retrievePast(1).complete();
			if (latestMessages.size() > 0) {
				while (latestMessages.get(0).isWebhookMessage()) {
					latestMessages = history.retrievePast(1).complete();
				}
			}
			final long daysSinceLastMessage = latestMessages.size() > 0 ?
				ChronoUnit.DAYS.between(latestMessages.get(latestMessages.size()-1).getTimeCreated(), OffsetDateTime.now()) :
				-1;
			OldChannelsHelper.put(channel, daysSinceLastMessage);
			MMDBot.LOGGER.info("{}: {}", channel.getName(), daysSinceLastMessage);
		}
	}
}
