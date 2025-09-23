package com.minkang.ultimate.pixelrating;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RatingManager {

    public static class Stats {
        public int rating;
        public int win;
        public int loss;
        public int draw;
        public int forfeit;
        public java.util.Deque<String> recent = new java.util.ArrayDeque<>();
    }

    private final UltimatePixelmonRatingPlugin plugin;
    private final File file;
    private final Map<java.util.UUID, Stats> map = new HashMap<>();

    public RatingManager(UltimatePixelmonRatingPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.exists()) try { file.createNewFile(); } catch (IOException ignored) {}
        load();
    }

    public void load() {
        map.clear();
        FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
        if (!yml.getKeys(false).isEmpty()) {
            for (String k : yml.getKeys(false)) {
                java.util.UUID uuid = java.util.UUID.fromString(k);
                Stats s = new Stats();
                s.rating = yml.getInt(k+".rating", plugin.getConfig().getInt("rating.default", 1200));
                s.win = yml.getInt(k+".win", 0);
                s.loss = yml.getInt(k+".loss", 0);
                s.draw = yml.getInt(k+".draw", 0);
                s.forfeit = yml.getInt(k+".forfeit", 0);
                java.util.List<String> r = yml.getStringList(k+".recent");
                for (String x : r) s.recent.addLast(x);
                map.put(uuid, s);
            }
        }
    }

    public void save() {
        FileConfiguration yml = new YamlConfiguration();
        for (java.util.Map.Entry<java.util.UUID, Stats> e : map.entrySet()) {
            String k = e.getKey().toString();
            Stats s = e.getValue();
            yml.set(k+".rating", s.rating);
            yml.set(k+".win", s.win);
            yml.set(k+".loss", s.loss);
            yml.set(k+".draw", s.draw);
            yml.set(k+".forfeit", s.forfeit);
            yml.set(k+".recent", new java.util.ArrayList<>(s.recent));
        }
        try { yml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public Stats get(java.util.UUID id) {
        return map.computeIfAbsent(id, k -> {
            Stats s = new Stats();
            s.rating = plugin.getConfig().getInt("rating.default", 1200);
            return s;
        });
    }

    public int kFactor(int rating) {
        int def = plugin.getConfig().getInt("rating.k-factor.default", 32);
        int loT = plugin.getConfig().getInt("rating.k-factor.low-threshold", 1000);
        int hiT = plugin.getConfig().getInt("rating.k-factor.high-threshold", 1800);
        int loK = plugin.getConfig().getInt("rating.k-factor.low-bracket-k", 40);
        int hiK = plugin.getConfig().getInt("rating.k-factor.high-bracket-k", 24);
        if (rating <= loT) return loK;
        if (rating >= hiT) return hiK;
        return def;
    }

    public void applyResult(java.util.UUID winner, java.util.UUID loser, String type) {
        // type: VICTORY/DEFEAT/DRAW/FORFEIT (winner perspective)
        if (type == null) type = "VICTORY";
        if ("DRAW".equals(type)) {
            Stats s1 = get(winner);
            Stats s2 = get(loser);
            s1.draw++; s2.draw++;
            pushRecent(s1, "DRAW");
            pushRecent(s2, "DRAW");
            save();
            return;
        }
        Stats w = get(winner);
        Stats l = get(loser);
        int kw = kFactor(w.rating);
        int kl = kFactor(l.rating);
        double ew = 1.0/(1.0 + Math.pow(10, (l.rating - w.rating)/400.0));
        double el = 1.0/(1.0 + Math.pow(10, (w.rating - l.rating)/400.0));
        int dw = (int)Math.round(kw * (1.0 - ew));
        int dl = (int)Math.round(kl * (0.0 - el));
        if ("FORFEIT".equals(type)) {
            // extra penalty
            dl -= 10;
        }
        w.rating += dw;
        l.rating += dl;
        w.win++; l.loss++;
        pushRecent(w, "W+"+dw); pushRecent(l, "L"+dl);
        save();
    }

    private void pushRecent(Stats s, String item) {
        s.recent.addFirst(item);
        while (s.recent.size() > 10) s.recent.removeLast();
    }
}


    public java.util.Map<java.util.UUID, Stats> snapshot() {
        return java.util.Collections.unmodifiableMap(map);
    }

    public void resetAll() {
        int def = plugin.getConfig().getInt("rating.default", 1200);
        for (Stats s : map.values()) {
            s.rating = def;
            s.win = s.loss = s.draw = s.forfeit = 0;
            s.recent.clear();
        }
        save();
    }
