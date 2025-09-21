
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class MatchSessionManager {
    public static class Session {
        public final UUID a;
        public final UUID b;
        public final Location aOrig;
        public final Location bOrig;
        public final long startedAt;
        public final String arenaName;
        public Session(UUID a, UUID b, Location aOrig, Location bOrig, long startedAt, String arenaName){
            this.a=a; this.b=b; this.aOrig=aOrig; this.bOrig=bOrig; this.startedAt=startedAt; this.arenaName=arenaName;
        }
    }
    private final UltimatePixelmonRatingPlugin plugin;
    private final Map<String, Session> sessions = new HashMap<>();

    public MatchSessionManager(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; }

    private String key(UUID a, UUID b){
        if (a.compareTo(b) < 0) return a.toString()+"|"+b.toString();
        return b.toString()+"|"+a.toString();
    }

    public void start(Player p1, Player p2, String arenaName){
        Session s = new Session(p1.getUniqueId(), p2.getUniqueId(), p1.getLocation().clone(), p2.getLocation().clone(), System.currentTimeMillis(), arenaName);
        sessions.put(key(s.a, s.b), s);
    }

    public Session remove(UUID a, UUID b){
        return sessions.remove(key(a,b));
    }

    public void completeAndReturn(UUID winner, UUID loser){
        boolean should = plugin.getConfig().getBoolean("match.return-to-original", true);
        if (!should) return;
        int delay = plugin.getConfig().getInt("match.return-delay-seconds", 0);
        final Session s = remove(winner, loser);
        if (s == null) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player pa = Bukkit.getPlayer(s.a);
            Player pb = Bukkit.getPlayer(s.b);
            if (pa != null && s.aOrig != null) pa.teleport(s.aOrig);
            if (pb != null && s.bOrig != null) pb.teleport(s.bOrig);
        }, delay * 20L);
    }
}
