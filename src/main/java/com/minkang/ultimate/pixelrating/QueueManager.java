package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class QueueManager {

    private final UltimatePixelmonRatingPlugin plugin;
    private final ArenaManager arenas;
    private final MatchSessionManager sessions;
    private final RatingManager ratings;

    private final LinkedHashSet<java.util.UUID> queue = new LinkedHashSet<>();

    public QueueManager(UltimatePixelmonRatingPlugin plugin, ArenaManager arenas, MatchSessionManager sessions, RatingManager ratings) {
        this.plugin = plugin;
        this.arenas = arenas;
        this.sessions = sessions;
        this.ratings = ratings;

        int period = plugin.getConfig().getInt("queue.tick-period-ticks", 20);
        new BukkitRunnable() {
            @Override public void run() { tryMatch(); }
        }.runTaskTimer(plugin, period, period);
    }

    public boolean join(Player p) {
        if (queue.contains(p.getUniqueId())) return false;
        queue.add(p.getUniqueId());
        return true;
    }

    public boolean leave(Player p) {
        return queue.remove(p.getUniqueId());
    }

    public java.util.List<java.util.UUID> snapshot(){ return new java.util.ArrayList<>(queue); }

    private void tryMatch() {
        if (queue.size() < 2) return;

        int base = plugin.getConfig().getInt("queue.base-threshold", 300);

        java.util.UUID[] arr = queue.toArray(new java.util.UUID[0]);
        java.util.Map<java.util.UUID, Integer> rt = new java.util.HashMap<>();
        for (java.util.UUID id : arr) {
            RatingManager.Stats s = ratings.get(id);
            rt.put(id, s.rating);
        }
        outer:
        for (int i=0;i<arr.length;i++) {
            for (int j=i+1;j<arr.length;j++) {
                int r1 = rt.get(arr[i]);
                int r2 = rt.get(arr[j]);
                int diff = Math.abs(r1 - r2);
                if (diff <= base) {
                    Player p1 = Bukkit.getPlayer(arr[i]);
                    Player p2 = Bukkit.getPlayer(arr[j]);
                    if (p1 == null || p2 == null) continue;
                    startMatch(p1, p2);
                    queue.remove(arr[i]);
                    queue.remove(arr[j]);
                    break outer;
                }
            }
        }
    }

    private void startMatch(Player p1, Player p2) {
        ArenaManager.Arena ar = arenas.pickUsable();
        Location a = null, b = null;
        String arenaName = null;
        if (ar == null) {
            if (plugin.getConfig().getBoolean("match.arena-required", true)) {
                String msg = com.minkang.ultimate.pixelrating.UltimatePixelmonRatingPlugin.get().getConfig().getString("ui.no-arena","&c사용 가능한 아레나가 없습니다.");
                p1.sendMessage(Util.color(msg));
                p2.sendMessage(Util.color(msg));
                return;
            } else {
                a = Util.cfgLoc(plugin.getConfig().getConfigurationSection("match.stage-location"));
                b = a.clone().add(3, 0, 0);
                arenaName = "stage";
            }
        } else {
            a = ar.a;
            b = ar.b;
            arenaName = ar.name;
        }

        Location p1Prev = p1.getLocation();
        Location p2Prev = p2.getLocation();
        if (a != null) p1.teleport(a);
        if (b != null) p2.teleport(b);

        sessions.start(p1, p2, arenaName, p1Prev, p2Prev);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pokebattle " + p1.getName() + " " + p2.getName());
        p1.sendMessage(Util.color(com.minkang.ultimate.pixelrating.UltimatePixelmonRatingPlugin.get().getConfig().getString("ui.match-start","&6{p1}&7 vs &6{p2}&7 매치 시작!").replace("{p1}", p1.getName()).replace("{p2}", p2.getName())));
        p2.sendMessage(Util.color(com.minkang.ultimate.pixelrating.UltimatePixelmonRatingPlugin.get().getConfig().getString("ui.match-start","&6{p1}&7 vs &6{p2}&7 매치 시작!").replace("{p1}", p1.getName()).replace("{p2}", p2.getName())));
    }
}
