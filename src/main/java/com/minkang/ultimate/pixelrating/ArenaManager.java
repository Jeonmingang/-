package com.minkang.ultimate.pixelrating;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    public static class Arena {
        public String name;
        public boolean enabled;
        public Location a;
        public Location b;
        public Location stage;
        public Location spec;
    }

    private final UltimatePixelmonRatingPlugin plugin;
    private final File file;
    private final Map<String, Arena> map = new HashMap<>();

    public ArenaManager(UltimatePixelmonRatingPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        if (!this.file.exists()) {
            plugin.saveResource("arenas.yml", false);
        }
    }

    public void load() {
        map.clear();
        FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
        if (!yml.isConfigurationSection("arenas")) return;
        for (String key : yml.getConfigurationSection("arenas").getKeys(false)) {
            String p = "arenas."+key+".";
            Arena ar = new Arena();
            ar.name = key;
            ar.enabled = yml.getBoolean(p+"enabled", false);
            ar.a = Util.cfgLoc(yml.getConfigurationSection(p+"a"));
            ar.b = Util.cfgLoc(yml.getConfigurationSection(p+"b"));
            ar.stage = Util.cfgLoc(yml.getConfigurationSection(p+"stage"));
            ar.spec = Util.cfgLoc(yml.getConfigurationSection(p+"spec"));
            map.put(key.toLowerCase(java.util.Locale.ROOT), ar);
        }
    }

    public void save() {
        FileConfiguration yml = new YamlConfiguration();
        for (Arena ar : map.values()) {
            String p = "arenas."+ar.name+".";
            yml.set(p+"enabled", ar.enabled);
            setLoc(yml, p+"a", ar.a);
            setLoc(yml, p+"b", ar.b);
            setLoc(yml, p+"stage", ar.stage);
            setLoc(yml, p+"spec", ar.spec);
        }
        try { yml.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    private static void setLoc(FileConfiguration yml, String path, Location l) {
        if (l == null) return;
        yml.set(path+".world", l.getWorld()==null? "world" : l.getWorld().getName());
        yml.set(path+".x", l.getX());
        yml.set(path+".y", l.getY());
        yml.set(path+".z", l.getZ());
        yml.set(path+".yaw", l.getYaw());
        yml.set(path+".pitch", l.getPitch());
    }

    public Arena get(String name) {
        return map.get(name.toLowerCase(java.util.Locale.ROOT));
    }

    public Arena create(String name) {
        Arena ar = new Arena();
        ar.name = name;
        ar.enabled = false;
        map.put(name.toLowerCase(java.util.Locale.ROOT), ar);
        save();
        return ar;
    }

    public void enable(String name, boolean flag) {
        Arena ar = get(name);
        if (ar == null) return;
        ar.enabled = flag;
        save();
    }

    public Arena pickUsable() {
        for (Arena ar : map.values()) {
            if (ar.enabled && ar.a != null && ar.b != null) return ar;
        }
        return null;
    }

    public Collection<Arena> list() { return map.values(); }
}
