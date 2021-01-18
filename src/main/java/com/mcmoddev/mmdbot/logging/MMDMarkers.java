package com.mcmoddev.mmdbot.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Class for holding the {@link Marker}s used for logging.
 */
public class MMDMarkers {
    /**
     * The {@link Marker} for the {@link com.mcmoddev.mmdbot.updatenotifiers.fabric.FabricApiUpdateNotifier}.
     */
    public static final Marker NOTIFIER_FABRIC = MarkerFactory.getMarker("Notifier.Fabric");
    /**
     * The {@link Marker} for the {@link com.mcmoddev.mmdbot.updatenotifiers.forge.ForgeUpdateNotifier}.
     */
    public static final Marker NOTIFIER_FORGE = MarkerFactory.getMarker("Notifier.Forge");
    /**
     * The {@link Marker} for the {@link com.mcmoddev.mmdbot.updatenotifiers.game.MinecraftUpdateNotifier}.
     */
    public static final Marker NOTIFIER_MC = MarkerFactory.getMarker("Notifier.MC");

    /**
     * The {@link Marker} for the requests removal system.
     *
     * @see com.mcmoddev.mmdbot.events.EventReactionAdded
     */
    public static final Marker REQUESTS = MarkerFactory.getMarker("Requests");

    /**
     * The {@link Marker} for different guild-related events, such as role addition/removal or nickname changes.
     */
    public static final Marker EVENTS = MarkerFactory.getMarker("Events");

    /**
     * The {@link Marker} for the muting system.
     *
     * @see com.mcmoddev.mmdbot.commands.staff.CmdMute
     * @see com.mcmoddev.mmdbot.commands.staff.CmdUnmute
     */
    public static final Marker MUTING = MarkerFactory.getMarker("Muting");

    /**
     * The {@link Marker} for the old channels system.
     *
     * @see com.mcmoddev.mmdbot.oldchannels.ChannelMessageChecker
     * @see com.mcmoddev.mmdbot.oldchannels.OldChannelsHelper
     * @see com.mcmoddev.mmdbot.commands.staff.CmdOldChannels
     */
    public static final Marker OLD_CHANNELS = MarkerFactory.getMarker("OldChannels");
}
