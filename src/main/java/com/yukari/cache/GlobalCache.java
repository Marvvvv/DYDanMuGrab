package com.yukari.cache;

import com.yukari.entity.GiftInfo;

import java.util.Map;

/**
 * 全局缓存类
 */
public class GlobalCache {

    private static GlobalCache globalCache = new GlobalCache();

    private boolean isOnline;
    private int roomId;
    private Map<Integer, GiftInfo> giftInfoCache;

    public Map<Integer, GiftInfo> getGiftInfoCache() {
        return giftInfoCache;
    }

    public void setGiftInfoCache(Map<Integer, GiftInfo> giftInfoCache) {
        this.giftInfoCache = giftInfoCache;
    }
    public static GlobalCache getGlobalCache() {
        return globalCache;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
