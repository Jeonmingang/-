package com.minkang.ultimate.pixelrating;

public class TierManager {
    private final UltimatePixelmonRatingPlugin plugin;
    public TierManager(UltimatePixelmonRatingPlugin plugin){ this.plugin = plugin; }
    public String tierOf(int rating){
        org.bukkit.configuration.file.FileConfiguration c = plugin.getConfig();
        java.util.List<java.util.Map<?,?>> list = c.getMapList("tier.bands");
        String last = "UNRANKED";
        for (java.util.Map<?,?> m : list){
            int min = ((Number)m.getOrDefault("min", 0)).intValue();
            String name = String.valueOf(m.getOrDefault("name", "T"));
            if (rating >= min) last = name;
        }
        return last;
    }
}
