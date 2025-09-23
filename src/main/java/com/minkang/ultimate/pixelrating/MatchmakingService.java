package com.minkang.ultimate.pixelrating;
import org.bukkit.Bukkit; import org.bukkit.ChatColor; import org.bukkit.Location; import org.bukkit.entity.Player; import org.bukkit.scheduler.BukkitRunnable;
public class MatchmakingService {
  private final UltimatePixelmonRatingPlugin plugin; private final RatingManager ratings; private final TierManager tiers; private final BanEnforcer bans; private final ArenaManager arenas;
  private final java.util.LinkedHashSet<java.util.UUID> queue=new java.util.LinkedHashSet<>(); private int taskId=-1;
  public MatchmakingService(UltimatePixelmonRatingPlugin plugin, RatingManager ratings, TierManager tiers, BanEnforcer bans, ArenaManager arenas){ this.plugin=plugin; this.ratings=ratings; this.tiers=tiers; this.bans=bans; this.arenas=arenas; }
  public void start(){ stop(); int period=plugin.getConfig().getInt("queue.tick-period-ticks",40); taskId=new BukkitRunnable(){ @Override public void run(){ tick(); } }.runTaskTimer(plugin,period,period).getTaskId(); plugin.getLogger().info("[UPR] Matchmaking started. period="+period+"t"); }
  public void stop(){ if(taskId!=-1){ Bukkit.getScheduler().cancelTask(taskId); taskId=-1; } }
  public void enqueue(Player p){ queue.add(p.getUniqueId()); if(plugin.getConfig().getBoolean("debug.matchmaking",true)) plugin.getLogger().info("[UPR] Enqueued: "+p.getName()+" size="+queue.size()); }
  public void dequeue(Player p){ queue.remove(p.getUniqueId()); if(plugin.getConfig().getBoolean("debug.matchmaking",true)) plugin.getLogger().info("[UPR] Dequeued: "+p.getName()+" size="+queue.size()); }
  public boolean isQueued(Player p){ return queue.contains(p.getUniqueId()); }
  public int queueSize(){ return queue.size(); }
  private void tick(){
    if(plugin.getConfig().getBoolean("debug.matchmaking",true)) plugin.getLogger().info("[UPR] Queue tick. size="+queue.size());
    if(queue.size()<2) return;
    java.util.Iterator<java.util.UUID> it=queue.iterator();
    java.util.UUID u1=it.next(); it.remove();
    if(!it.hasNext()){ queue.add(u1); return; }
    java.util.UUID u2=it.next(); it.remove();
    Player p1=Bukkit.getPlayer(u1), p2=Bukkit.getPlayer(u2); if(p1==null||p2==null){ if(p1!=null) queue.add(u1); if(p2!=null) queue.add(u2); if(plugin.getConfig().getBoolean("debug.matchmaking",true)) plugin.getLogger().info("[UPR] One player offline, requeue."); return; }
    int e1=ratings.getElo(u1), e2=ratings.getElo(u2); String t1=tiers.findTierName(e1), t2=tiers.findTierName(e2);
    Location l1=null,l2=null; if(plugin.getConfig().getBoolean("match.use-arenas",true)){ Arena a=arenas.chooseRandomReady(); if(a!=null){ if(a.getP1()!=null) l1=a.getP1().toLocation(); if(a.getP2()!=null) l2=a.getP2().toLocation(); } }
    if(l1==null || l2==null){ String world=plugin.getConfig().getString("match.stage-location.world","world"); double x=plugin.getConfig().getDouble("match.stage-location.x",0.5), y=plugin.getConfig().getDouble("match.stage-location.y",100.0), z=plugin.getConfig().getDouble("match.stage-location.z",0.5); float yaw=(float)plugin.getConfig().getDouble("match.stage-location.yaw",0), pitch=(float)plugin.getConfig().getDouble("match.stage-location.pitch",0); org.bukkit.World w=Bukkit.getWorld(world); if(w!=null){ l1=new Location(w,x,y,z,yaw,pitch); l2=new Location(w,x+3,y,z,yaw,pitch);} }
    for(String raw: plugin.getConfig().getStringList("match.on-match-found-commands")){ String cmd=raw.replace("{p1}",p1.getName()).replace("{p2}",p2.getName()).replace("{elo1}",String.valueOf(e1)).replace("{elo2}",String.valueOf(e2)).replace("{tier1}",ChatColor.stripColor(t1)).replace("{tier2}",ChatColor.stripColor(t2)).replace("{ban_pokemon_csv}",bans.bannedPokemonCsv()).replace("{ban_items_csv}",bans.bannedItemsCsv()); Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd); }
    plugin.getLogger().info("[UPR] Match found: "+p1.getName()+" vs "+p2.getName()+" ("+e1+"/"+e2+") l1="+(l1!=null)+" l2="+(l2!=null));
    if(plugin.getConfig().getBoolean("match.teleport-on-match-found", true)){ if(l1!=null) p1.teleport(l1); if(l2!=null) p2.teleport(l2); }
    int countdown=plugin.getConfig().getInt("match.pre-countdown-seconds",3);
    String battleCmd=plugin.getConfig().getString("match.battle-start-command","pokebattle {p1} {p2}");
    Bukkit.getScheduler().runTaskLater(plugin,()->{
      for(String raw: plugin.getConfig().getStringList("match.on-battle-start-commands")){ String cmd=raw.replace("{p1}",p1.getName()).replace("{p2}",p2.getName()).replace("{elo1}",String.valueOf(e1)).replace("{elo2}",String.valueOf(e2)).replace("{tier1}",ChatColor.stripColor(t1)).replace("{tier2}",ChatColor.stripColor(t2)).replace("{ban_pokemon_csv}",bans.bannedPokemonCsv()).replace("{ban_items_csv}",bans.bannedItemsCsv()); Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);} 
      if(battleCmd!=null && !battleCmd.trim().isEmpty()){ String cmd=battleCmd.replace("{p1}",p1.getName()).replace("{p2}",p2.getName()); boolean ok=Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd); if(plugin.getConfig().getBoolean("debug.battle",true)) plugin.getLogger().info("[UPR] Battle start cmd: '"+cmd+"' dispatched="+ok); }
      else { plugin.getLogger().warning("[UPR] battle-start-command is empty. Configure 'match.battle-start-command' in config.yml"); }
    }, countdown*20L);
  }
}