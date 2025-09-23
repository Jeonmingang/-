package com.minkang.ultimate.pixelrating;
import org.bukkit.configuration.ConfigurationSection;
public class TierManager {
  public static class Tier { public final String name,color; public final int minElo; public Tier(String n,int e,String c){name=n;minElo=e;color=c;} }
  private final java.util.List<Tier> tiers=new java.util.ArrayList<>();
  public TierManager(UltimatePixelmonRatingPlugin plugin){
    java.util.List<?> list = plugin.getConfig().getList("tiers");
    if(list!=null){
      for(Object o: list){
        if(o instanceof ConfigurationSection){
          ConfigurationSection s=(ConfigurationSection)o;
          tiers.add(new Tier(s.getString("name","Unknown"), s.getInt("min-elo",0), s.getString("color","&f")));
        } else if(o instanceof java.util.Map){
          java.util.Map<?,?> m=(java.util.Map<?,?>)o;
          Object nameObj=m.get("name");
          Object minObj=m.get("min-elo");
          Object colorObj=m.containsKey("color") ? m.get("color") : "&f";
          String name = nameObj!=null ? String.valueOf(nameObj) : "Unknown";
          int min = (minObj instanceof Number) ? ((Number)minObj).intValue() : 0;
          String color = colorObj!=null ? String.valueOf(colorObj) : "&f";
          tiers.add(new Tier(name,min,color));
        } else if(o instanceof String){
          tiers.add(new Tier(String.valueOf(o), 0, "&f"));
        }
      }
    }
    tiers.sort((a,b)->Integer.compare(a.minElo,b.minElo));
  }
  public String findTierName(int elo){ String out=tiers.isEmpty()?"Unknown":tiers.get(0).name; for(Tier t: tiers) if(elo>=t.minElo) out=t.name; else break; return out; }
}