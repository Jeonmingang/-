package com.minkang.ultimate.pixelrating;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BattleState {
    private static final long TTL_MILLIS = 20 * 60 * 1000L; // safety 20m
    private static final Map<UUID, Long> IN = new ConcurrentHashMap<>();
    private BattleState(){}

    public static void mark(UUID... ids){
        long now = System.currentTimeMillis();
        for (UUID id : ids) IN.put(id, now);
    }
    public static void clear(UUID... ids){
        for (UUID id : ids) IN.remove(id);
    }
    public static boolean isIn(UUID id){
        Long t = IN.get(id);
        if (t == null) return false;
        if (System.currentTimeMillis() - t > TTL_MILLIS){ IN.remove(id); return false; }
        return true;
    }
}