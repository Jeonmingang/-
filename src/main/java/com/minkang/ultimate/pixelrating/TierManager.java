package com.minkang.ultimate.pixelrating;

import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList; import java.util.List;

public class TierManager {
    public static class Tier { public final String name; public final int minElo; public final String color;
        public Tier(String name,int minElo,String color){ this.name=name; this.minElo=minElo; this.color=color; } }
    private final UltimatePixelmonRatingPlugin plugin; private final List<Tier> tiers=new ArrayList<>();
    public TierManager(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; reload(); }
    public void reload(){
        tiers.clear();
        List<?> list=plugin.getConfig().getList("tiers");
        if (list!=null){
            for(Object o:list){
                if (o instanceof ConfigurationSection){
                    ConfigurationSection cs=(ConfigurationSection)o;
                    tiers.add(new Tier(cs.getString("name","&7Bronze"), cs.getInt("min-elo",0), cs.getString("color","&7")));
                } else if (o instanceof java.util.Map){
                    @SuppressWarnings("unchecked") java.util.Map<String,Object> m=(java.util.Map<String,Object>)o;
                    String name=String.valueOf(m.getOrDefault("name","&7Bronze"));
                    int min=Integer.parseInt(String.valueOf(m.getOrDefault("min-elo",0)));
                    String color=String.valueOf(m.getOrDefault("color","&7"));
                    tiers.add(new Tier(name,min,color));
                }
            }
        }
        tiers.sort((a,b)->Integer.compare(a.minElo, b.minElo));
    }
    public Tier ofElo(int elo){ Tier cur=null; for (Tier t:tiers){ if (elo>=t.minElo) cur=t; } if (cur==null && !tiers.isEmpty()) cur=tiers.get(0); return cur; }
    public java.util.List<Tier> list(){ return new java.util.ArrayList<>(tiers); }
}