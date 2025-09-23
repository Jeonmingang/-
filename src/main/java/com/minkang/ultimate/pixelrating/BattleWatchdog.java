package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class BattleWatchdog {
    private final UltimatePixelmonRatingPlugin plugin;
    private final MatchSessionManager sessions;
    private final RatingManager ratings;

    public BattleWatchdog(UltimatePixelmonRatingPlugin plugin, MatchSessionManager sessions, RatingManager ratings) {
        this.plugin = plugin;
        this.sessions = sessions;
        this.ratings = ratings;
        int period = 40;
        new BukkitRunnable() {
            @Override public void run() { sweep(); }
        }.runTaskTimer(plugin, period, period);
    }

    private void sweep() {
        long now = System.currentTimeMillis();
        int timeout = plugin.getConfig().getInt("battle.timeout-seconds", 0);
        if (timeout <= 0) return;
        for (MatchSessionManager.Session s : sessions.allSessions()) {
            if (s.ended) continue;
            if (now - s.startMs >= timeout * 1000L) {
                s.ended = true;
                ratings.applyResult(s.p1, s.p2, "DRAW");
                sessions.restore(s);
                sessions.endFor(Bukkit.getPlayer(s.p1));
            }
        }
    }
}
