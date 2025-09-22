package com.minkang.ultimate.pixelrating;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class QueueManager {

    private final UltimatePixelmonRatingPlugin plugin;
    private final Set<UUID> queue = new HashSet<>();

    public QueueManager(UltimatePixelmonRatingPlugin plugin) {
        this.plugin = plugin;
    }

    public int size() { return queue.size(); }

    public boolean isQueued(UUID uuid) { return queue.contains(uuid); }

    public boolean join(UUID uuid) { return queue.add(uuid); }

    public boolean leave(UUID uuid) { return queue.remove(uuid); }

    public void clear() { queue.clear(); }
}