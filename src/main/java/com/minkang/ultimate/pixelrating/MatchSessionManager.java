package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class MatchSessionManager {

    public static class Session {
        public java.util.UUID p1;
        public java.util.UUID p2;
        public String arenaName;
        public Location p1Prev;
        public Location p2Prev;
        public long startMs;
        public boolean ended=false;
    }

    private final UltimatePixelmonRatingPlugin plugin;
    private final Map<java.util.UUID, Session> byPlayer = new HashMap<>();
    private final java.util.Set<Session> sessions = new java.util.HashSet<>();

    public MatchSessionManager(UltimatePixelmonRatingPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(Player p1, Player p2, String arenaName, Location p1Prev, Location p2Prev) {
        Session s = new Session();
        s.p1 = p1.getUniqueId();
        s.p2 = p2.getUniqueId();
        s.arenaName = arenaName;
        s.p1Prev = p1Prev;
        s.p2Prev = p2Prev;
        s.startMs = System.currentTimeMillis();
        byPlayer.put(s.p1, s);
        byPlayer.put(s.p2, s);
        sessions.add(s);
    }

    public Session get(Player p) { return byPlayer.get(p.getUniqueId()); }

    public void endFor(Player p) {
        Session s = byPlayer.remove(p.getUniqueId());
        if (s != null) {
            byPlayer.remove(s.p1);
            byPlayer.remove(s.p2);
            sessions.remove(s);
        }
    }

    public void restore(Session s) {
        if (s == null) return;
        Player p1 = Bukkit.getPlayer(s.p1);
        Player p2 = Bukkit.getPlayer(s.p2);
        if (p1 != null && s.p1Prev != null) p1.teleport(s.p1Prev);
        if (p2 != null && s.p2Prev != null) p2.teleport(s.p2Prev);
    }

    public java.util.Set<Session> allSessions(){ return java.util.Collections.unmodifiableSet(sessions); }
}
