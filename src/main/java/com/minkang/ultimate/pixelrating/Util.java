package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public final class Util {
    private Util() {}

    public static Location cfgLoc(org.bukkit.configuration.ConfigurationSection sec) {
        if (sec == null) return null;
        World w = Bukkit.getWorld(sec.getString("world", "world"));
        double x = sec.getDouble("x", 0.5);
        double y = sec.getDouble("y", 64.0);
        double z = sec.getDouble("z", 0.5);
        float yaw = (float) sec.getDouble("yaw", 0);
        float pitch = (float) sec.getDouble("pitch", 0);
        return new Location(w, x, y, z, yaw, pitch);
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    public static boolean hasProtocolLib() {
        return Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
    }

    public static Pattern[] compilePatterns(java.util.List<String> list) {
        if (list == null) return new Pattern[0];
        Pattern[] out = new Pattern[list.size()];
        for (int i=0;i<list.size();i++) {
            out[i] = Pattern.compile(list.get(i));
        }
        return out;
    }

    public static void msg(Player p, String key, Object... kv) {
        if (p == null) return;
        String base = com.minkang.ultimate.pixelrating.UltimatePixelmonRatingPlugin.get().getConfig().getString("ui."+key, key);
        for (int i=0;i<kv.length;i+=2) {
            base = base.replace("{"+String.valueOf(kv[i])+"}", String.valueOf(kv[i+1]));
        }
        p.sendMessage(color(base));
    }
}
