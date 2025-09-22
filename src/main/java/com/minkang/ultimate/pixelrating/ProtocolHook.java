package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ProtocolHook {
    public static void safeInit(UltimatePixelmonRatingPlugin plugin) {
        Plugin p = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (p == null || !p.isEnabled()) return;
        // Future: packet features; noop for now.
    }
}