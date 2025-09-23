package com.minkang.ultimate.pixelrating;

public class TierManager {
    private final UltimatePixelmonRatingPlugin plugin;
    public TierManager(UltimatePixelmonRatingPlugin plugin){ this.plugin = plugin; }

    @SuppressWarnings("unchecked")
    public String tierOf(int rating){
        org.bukkit.configuration.file.FileConfiguration c = plugin.getConfig();
        java.util.List<?> raw = c.getList("tier.bands");
        String last = "UNRANKED";
        if (raw == null) return last;
        for (Object o : raw){
            if (!(o instanceof java.util.Map)) continue;
            java.util.Map<String, Object> m = (java.util.Map<String, Object>) o;
            Object vMin = m.get("min");
            Object vName = m.get("name");
            int min = (vMin instanceof Number) ? ((Number)vMin).intValue() : 0;
            String name = (vName != null) ? String.valueOf(vName) : "T";
            if (rating >= min) last = name;
        }
        return last;
    }
}
