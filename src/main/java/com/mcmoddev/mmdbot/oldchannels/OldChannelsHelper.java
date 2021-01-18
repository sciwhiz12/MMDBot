package com.mcmoddev.mmdbot.oldchannels;

import java.util.HashMap;
import java.util.Map;

public class OldChannelsHelper {
    public static final long NO_MESSAGE_FOUND = -1;

    private static final Map<Long, Long> channelLastMessageMap = new HashMap<>();

    private static volatile boolean ready = false;

    public static long getLastMessageTime(final long channelID) {
        return channelLastMessageMap.getOrDefault(channelID, NO_MESSAGE_FOUND);
    }

    public static void clear() {
        channelLastMessageMap.clear();
        setReady(false);
    }

    public static void put(final long channelID, final long timeSinceLastMessage) {
        channelLastMessageMap.put(channelID, timeSinceLastMessage);
    }

    public static boolean isReady() {
        return ready;
    }

    public static void setReady(boolean ready) {
        OldChannelsHelper.ready = ready;
    }
}
