package com.minkang.ultimate.pixelrating;
import org.bukkit.configuration.ConfigurationSection; public class TierManager {
  public static class Tier { public final String name,color; public final int minElo; public Tier(String n,int e,String c){name=n;minElo=e;color=c;} }
  private final java.util.List<Tier> tiers=new java.util.ArrayList<>();
  public TierManager(UltimatePixelmonRatingPlugin plugin){
    for(Object o: plugin.getConfig().getList("tiers")){
      if(o instanceof ConfigurationSection){ ConfigurationSection s=(ConfigurationSection)o; tiers.add(new Tier(s.getString("name","Unknown"), s.getInt("min-elo",0), s.getString("color","&f"))); }
      else if(o instanceof java.util.Map){ java.util.Map<?,?> m=(java.util.Map<?,?>)o; String name=String.valueOf(m.get("name")); int min=(m.get("min-elo") instanceof Number)?((Number)m.get("min-elo")).intValue():0; String color=String.valueOf(m.getOrDefault("color","&f")); tiers.add(new Tier(name,min,color)); }
    } tiers.sort((a,b)->Integer.compare(a.minElo,b.minElo)); }
  public String findTierName(int elo){ String out=tiers.isEmpty()?"Unknown":tiers.get(0).name; for(Tier t: tiers) if(elo>=t.minElo) out=t.name; else break; return out; }
}