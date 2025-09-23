package com.minkang.ultimate.pixelrating;
import org.bukkit.entity.Player; import java.util.*;
public class RatingManager {
  private final UltimatePixelmonRatingPlugin plugin; private final TierManager tiers; private final EloStore store;
  private final Map<java.util.UUID,Integer> elo=new HashMap<>();
  public RatingManager(UltimatePixelmonRatingPlugin plugin, TierManager tiers, EloStore store){ this.plugin=plugin; this.tiers=tiers; this.store=store; this.elo.putAll(store.all()); }
  public int getElo(java.util.UUID u){ return elo.getOrDefault(u, plugin.getConfig().getInt("rating.initial-elo",1200)); }
  public int getElo(Player p){ return getElo(p.getUniqueId()); }
  public void setElo(java.util.UUID u,int v){ elo.put(u,v); store.set(u,v); store.save(); }
  public String tierNameByElo(int e){ return tiers.findTierName(e); }
  public void applyMatch(java.util.UUID w, java.util.UUID l, boolean draw){
    int ew=getElo(w), el=getElo(l);
    double sw=draw?plugin.getConfig().getDouble("rating.draw-score",0.5):1.0;
    double sl=draw?plugin.getConfig().getDouble("rating.draw-score",0.5):0.0;
    double exw=1.0/(1.0+Math.pow(10,(el-ew)/400.0)); double exl=1.0-exw;
    int kw=EloMath.k(ew, plugin.getConfig().getInt("rating.k-factor.default",32), plugin.getConfig().getInt("rating.k-factor.high-bracket-k",24), plugin.getConfig().getInt("rating.k-factor.low-bracket-k",40), plugin.getConfig().getInt("rating.k-factor.high-threshold",1800), plugin.getConfig().getInt("rating.k-factor.low-threshold",1000));
    int kl=EloMath.k(el, plugin.getConfig().getInt("rating.k-factor.default",32), plugin.getConfig().getInt("rating.k-factor.high-bracket-k",24), plugin.getConfig().getInt("rating.k-factor.low-bracket-k",40), plugin.getConfig().getInt("rating.k-factor.high-threshold",1800), plugin.getConfig().getInt("rating.k-factor.low-threshold",1000));
    setElo(w,(int)Math.round(ew+kw*(sw-exw))); setElo(l,(int)Math.round(el+kl*(sl-exl))); }
  public List<Map.Entry<java.util.UUID,Integer>> top(int n){ List<Map.Entry<java.util.UUID,Integer>> L=new ArrayList<>(elo.entrySet()); L.sort((a,b)->Integer.compare(b.getValue(),a.getValue())); return L.size()>n?L.subList(0,n):L; }
}