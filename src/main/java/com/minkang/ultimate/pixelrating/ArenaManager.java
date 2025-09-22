
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {
    public static class Arena {
        public String name;
        public boolean enabled = true;
        public Location p1;
        public Location p2;
        public Location stage;
        public Location spec;
    }

    private final UltimatePixelmonRatingPlugin plugin;
    private File file;
    private FileConfiguration conf;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    public ArenaManager(UltimatePixelmonRatingPlugin plugin) { this.plugin = plugin; }

    public void load() {
        file = new File(plugin.getDataFolder(), "arenas.yml");
        if (!file.exists()) plugin.saveResource("arenas.yml", false);
        conf = YamlConfiguration.loadConfiguration(file);
        arenas.clear();
        ConfigurationSection root = conf.getConfigurationSection("arenas");
        if (root != null) {
            for (String key : root.getKeys(false)) {
                ConfigurationSection cs = root.getConfigurationSection(key);
                Arena a = new Arena();
                a.name = key;
                a.enabled = cs.getBoolean("enabled", true);
                a.p1 = readLoc(cs.getConfigurationSection("spawns.p1"));
                a.p2 = readLoc(cs.getConfigurationSection("spawns.p2"));
                a.stage = readLoc(cs.getConfigurationSection("spawns.stage"));
                a.spec = readLoc(cs.getConfigurationSection("spawns.spec"));
                arenas.put(key.toLowerCase(), a);
            }
        }
    }

    public void save() {
        conf.set("arenas", null);
        for (Arena a : arenas.values()) {
            String base = "arenas." + a.name;
            conf.set(base + ".enabled", a.enabled);
            writeLoc(base + ".spawns.p1", a.p1);
            writeLoc(base + ".spawns.p2", a.p2);
            writeLoc(base + ".spawns.stage", a.stage);
            writeLoc(base + ".spawns.spec", a.spec);
        }
        try { conf.save(file); } catch (IOException e) { plugin.getLogger().severe("Failed to save arenas.yml: " + e.getMessage()); }
    }

    public boolean create(String name) {
        String key = name.toLowerCase();
        if (arenas.containsKey(key)) return false;
        Arena a = new Arena();
        a.name = name;
        a.enabled = true;
        arenas.put(key, a);
        save();
        return true;
    }

    public boolean delete(String name) {
        Arena removed = arenas.remove(name.toLowerCase());
        if (removed == null) return false;
        save();
        return true;
    }

    public boolean set(String name, String point, Location loc) {
        Arena a = arenas.get(name.toLowerCase());
        if (a == null) return false;
        if ("p1".equalsIgnoreCase(point)) a.p1 = loc;
        else if ("p2".equalsIgnoreCase(point)) a.p2 = loc;
        else if ("stage".equalsIgnoreCase(point)) a.stage = loc;
        else if ("spec".equalsIgnoreCase(point)) a.spec = loc;
        else return false;
        save();
        return true;
    }

    public boolean setEnabled(String name, boolean enabled) {
        Arena a = arenas.get(name.toLowerCase());
        if (a == null) return false;
        a.enabled = enabled;
        save();
        return true;
    }

    public List<Arena> list() { return new ArrayList<>(arenas.values()); }

    public Arena chooseRandomReady() {
        List<Arena> ready = new ArrayList<>();
        for (Arena a : arenas.values()) {
            if (a.enabled && a.p1 != null && a.p2 != null) ready.add(a);
        }
        if (ready.isEmpty()) return null;
        return ready.get(new Random().nextInt(ready.size()));
    }

    private Location readLoc(ConfigurationSection cs) {
        if (cs == null) return null;
        String worldName = cs.getString("world", null);
        if (worldName == null) return null;
        World w = Bukkit.getWorld(worldName);
        if (w == null) return null;
        double x = cs.getDouble("x"); double y = cs.getDouble("y"); double z = cs.getDouble("z");
        float yaw = (float) cs.getDouble("yaw", 0.0);
        float pitch = (float) cs.getDouble("pitch", 0.0);
        return new Location(w, x, y, z, yaw, pitch);
    }

    private void writeLoc(String path, Location loc) {
        if (loc == null) { conf.set(path, null); return; }
        conf.set(path + ".world", loc.getWorld().getName());
        conf.set(path + ".x", loc.getX());
        conf.set(path + ".y", loc.getY());
        conf.set(path + ".z", loc.getZ());
        conf.set(path + ".yaw", loc.getYaw());
        conf.set(path + ".pitch", loc.getPitch());
    }


    public int countEnabled(){
        int n=0;
        for (Arena a : arenas.values()){
            if (a.enabled && a.p1!=null && a.p2!=null) n++;
        }
        return n;
    }
}
