
package com.minkang.ultimate.pixelrating;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TierManager {
    public static class Tier {
        public final String name;
        public final int minElo;
        public final String color;
        public Tier(String name, int minElo, String color) { this.name=name; this.minElo=minElo; this.color=color; }
    }
    private final UltimatePixelmonRatingPlugin plugin;
    private final List<Tier> list = new ArrayList<>();
    public TierManager(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; reload(); }
    public void reload(){
        list.clear();
        java.util.List<?> src = plugin.getConfig().getList("tiers");
        if (src != null) for (Object o : src) {
            if (o instanceof ConfigurationSection){
                ConfigurationSection cs = (ConfigurationSection)o;
                list.add(new Tier(cs.getString("name","&7Bronze"), cs.getInt("min-elo",0), cs.getString("color","&7")));
            } else if (o instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String,Object> m = (java.util.Map<String,Object>) o;
            String name = String.valueOf(m.getOrDefault("name","&7Bronze"));
            int min = Integer.parseInt(String.valueOf(m.getOrDefault("min-elo",0)));
            String color = String.valueOf(m.getOrDefault("color","&7"));
            list.add(new Tier(name, min, color));
        }
        }
        list.sort((a,b)->Integer.compare(a.minElo, b.minElo));
    }
    public Tier ofElo(int elo){
        Tier cur = null;
        for (Tier t : list) if (elo >= t.minElo) cur = t;
        if (cur == null && list.size()>0) cur = list.get(0);
        return cur;
    }
    public java.util.List<Tier> list(){ return new ArrayList<>(list); }
}
