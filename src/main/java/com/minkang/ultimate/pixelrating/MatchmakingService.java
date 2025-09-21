package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit; import org.bukkit.Location; import org.bukkit.configuration.file.FileConfiguration; import org.bukkit.entity.Player; import java.util.*;

public class MatchmakingService {
    private final UltimatePixelmonRatingPlugin plugin; private final RatingManager ratingManager; private final TierManager tierManager; private final BanEnforcer banEnforcer;
    private final java.util.List<java.util.UUID> queue=new java.util.ArrayList<>(); private int taskId=-1;
    public MatchmakingService(UltimatePixelmonRatingPlugin plugin, RatingManager ratingManager, TierManager tierManager, BanEnforcer banEnforcer){
        this.plugin=plugin; this.ratingManager=ratingManager; this.tierManager=tierManager; this.banEnforcer=banEnforcer; }
    public boolean join(Player p){ if (queue.contains(p.getUniqueId())) return false; queue.add(p.getUniqueId()); return true; }
    public boolean leave(Player p){ return queue.remove(p.getUniqueId()); }
    public boolean isInQueue(Player p){ return queue.contains(p.getUniqueId()); }
    public java.util.List<String> names(){ java.util.List<String> out=new java.util.ArrayList<>(); for (java.util.UUID u: queue){ Player p=Bukkit.getPlayer(u); if (p!=null) out.add(p.getName()); } return out; }
    public void start(){ int period=plugin.getConfig().getInt("queue.tick-period-ticks",40); taskId=Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, period, period); }
    public void stop(){ if (taskId!=-1) Bukkit.getScheduler().cancelTask(taskId); taskId=-1; }

    private void tick(){
        FileConfiguration c=plugin.getConfig(); if (!c.getBoolean("queue.enable",true)) return; if (queue.size()<2) return;
        java.util.List<java.util.UUID> copy=new java.util.ArrayList<>(queue);
        for (java.util.UUID u: copy){
            if (!queue.contains(u)) continue;
            Player a=Bukkit.getPlayer(u); if (a==null){ queue.remove(u); continue; }
            if (banEnforcer.hasBannedItem(a)){ a.sendMessage(Util.color("&c[레이팅] 금지 아이템 소지로 큐 제외.")); queue.remove(u); continue; }
            PlayerProfile ap=ratingManager.getProfile(a.getUniqueId(), a.getName());
            java.util.UUID best=null; int bestDelta=Integer.MAX_VALUE;
            for (java.util.UUID v: copy){
                if (u.equals(v)) continue; if (!queue.contains(v)) continue;
                Player b=Bukkit.getPlayer(v); if (b==null) continue;
                if (banEnforcer.hasBannedItem(b)) continue;
                PlayerProfile bp=ratingManager.getProfile(b.getUniqueId(), b.getName());
                int delta=Math.abs(ap.getElo()-bp.getElo()); if (delta<bestDelta){ bestDelta=delta; best=v; }
            }
            if (best!=null){
                int allowed=c.getInt("queue.base-threshold",150)+c.getInt("queue.expand-per-second",5)*10;
                if (bestDelta<=allowed || queue.size()>6){
                    queue.remove(u); queue.remove(best); Player b=Bukkit.getPlayer(best);
                    if (b==null){ if(!queue.contains(u)) queue.add(u); continue; }
                    startMatch(a,b); break;
                }
            }
        }
    }

    private void startMatch(Player p1, Player p2){
        PlayerProfile A=ratingManager.getProfile(p1.getUniqueId(), p1.getName());
        PlayerProfile B=ratingManager.getProfile(p2.getUniqueId(), p2.getName());
        TierManager.Tier t1=tierManager.ofElo(A.getElo()); TierManager.Tier t2=tierManager.ofElo(B.getElo());
        String prefix=plugin.getConfig().getString("ui.prefix","&7[&a레이팅&7]&r ");
        String line=plugin.getConfig().getString("ui.matched","&6매칭 성사! &f{p1} &7({elo1} {tier1}) &6vs &f{p2} &7({elo2} {tier2})");
        String msg=line.replace("{p1}", p1.getName()).replace("{p2}", p2.getName())
                .replace("{elo1}", String.valueOf(A.getElo())).replace("{elo2}", String.valueOf(B.getElo()))
                .replace("{tier1}", Util.color(t1.name)).replace("{tier2}", Util.color(t2.name));
        p1.sendMessage(Util.color(prefix+msg)); p2.sendMessage(Util.color(prefix+msg));
        if (plugin.getConfig().getBoolean("match.teleport-on-match-found", false)){ Location st=getStageLocation(); if (st!=null){ p1.teleport(st); p2.teleport(st); } }

        String banP = banEnforcer.banPokemonCsv(); String banI = banEnforcer.banItemsCsv();
        for (String raw: plugin.getConfig().getStringList("match.on-match-found-commands")){
            String cmd=raw.replace("{p1}",p1.getName()).replace("{p2}",p2.getName())
                    .replace("{elo1}",String.valueOf(A.getElo())).replace("{elo2}",String.valueOf(B.getElo()))
                    .replace("{tier1}",Util.color(t1.name)).replace("{tier2}",Util.color(t2.name))
                    .replace("{ban_pokemon_csv}", banP).replace("{ban_items_csv}", banI);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Util.color(cmd));
        }
        int pre=plugin.getConfig().getInt("match.pre-countdown-seconds",3);
        Bukkit.getScheduler().runTaskLater(plugin, ()->{
            banEnforcer.enforcePreMatch(p1); banEnforcer.enforcePreMatch(p2);
            for (String raw: plugin.getConfig().getStringList("match.on-battle-start-commands")){
                String cmd=raw.replace("{p1}",p1.getName()).replace("{p2}",p2.getName())
                        .replace("{elo1}",String.valueOf(A.getElo())).replace("{elo2}",String.valueOf(B.getElo()))
                        .replace("{tier1}",Util.color(t1.name)).replace("{tier2}",Util.color(t2.name))
                        .replace("{ban_pokemon_csv}", banP).replace("{ban_items_csv}", banI);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Util.color(cmd));
            }
        }, pre*20L);
    }

    private Location getStageLocation(){
        org.bukkit.configuration.file.FileConfiguration c=plugin.getConfig();
        String wName=c.getString("match.stage-location.world","bskyblock_world");
        org.bukkit.World w=org.bukkit.Bukkit.getWorld(wName); if (w==null) return null;
        double x=c.getDouble("match.stage-location.x",0.5), y=c.getDouble("match.stage-location.y",100.0), z=c.getDouble("match.stage-location.z",0.5);
        float yaw=(float)c.getDouble("match.stage-location.yaw",0.0), pitch=(float)c.getDouble("match.stage-location.pitch",0.0);
        return new Location(w,x,y,z,yaw,pitch);
    }
}