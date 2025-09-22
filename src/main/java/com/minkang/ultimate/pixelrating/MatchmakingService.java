
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchmakingService {

    private void ensureSameWorld(org.bukkit.entity.Player a, org.bukkit.entity.Player b, org.bukkit.Location stage) {
        try {
            if (a.getWorld() != b.getWorld()) {
                if (stage != null) {
                    b.teleport(stage);
                } else {
                    b.teleport(a.getLocation());
                }
            }
        } catch (Throwable ignored) {}
    }

    private final UltimatePixelmonRatingPlugin plugin;
    private final RatingManager ratingManager;
    private final TierManager tierManager;
    private final BanEnforcer banEnforcer;
    private final ArenaManager arenaManager;
    private final List<UUID> queue = new ArrayList<>();
    private int taskId = -1;
    public MatchmakingService(UltimatePixelmonRatingPlugin plugin, RatingManager ratingManager, TierManager tierManager, BanEnforcer banEnforcer, ArenaManager arenaManager){
        this.plugin=plugin; this.ratingManager=ratingManager; this.tierManager=tierManager; this.banEnforcer=banEnforcer; this.arenaManager=arenaManager;
    }
    public boolean join(Player p){ if (queue.contains(p.getUniqueId())) return false; queue.add(p.getUniqueId()); return true; }
    public boolean leave(Player p){ return queue.remove(p.getUniqueId()); }
    public boolean isInQueue(Player p){ return queue.contains(p.getUniqueId()); }
    public List<String> names(){ List<String> n=new ArrayList<>(); for (UUID u:queue){ Player p=Bukkit.getPlayer(u); if (p!=null) n.add(p.getName()); } return n; }
    public void start(){ int period=plugin.getConfig().getInt("queue.tick-period-ticks",40); taskId=Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,this::tick,period,period); }
    public int size(){ return queue.size(); }

    public void stop(){ if (taskId!=-1) Bukkit.getScheduler().cancelTask(taskId); taskId=-1; }

    private void tick(){
        FileConfiguration c = plugin.getConfig();
        if (!c.getBoolean("queue.enable", true)) return;
        if (queue.size() < 2) return;
        List<UUID> copy = new ArrayList<>(queue);
        for (UUID u : copy) {
            if (!queue.contains(u)) continue;
            Player a = Bukkit.getPlayer(u);
            if (a==null){ queue.remove(u); continue; }
            if (banEnforcer.hasBannedItem(a)) { a.sendMessage(Util.color("&c[레이팅] 금지 아이템으로 큐 제외.")); queue.remove(u); continue; }
            PlayerProfile ap = ratingManager.getProfile(a.getUniqueId(), a.getName());
            UUID best=null; int bestDelta=Integer.MAX_VALUE;
            for (UUID v : copy) {
                if (u.equals(v)) continue;
                if (!queue.contains(v)) continue;
                Player b = Bukkit.getPlayer(v);
                if (b==null) continue;
                if (banEnforcer.hasBannedItem(b)) continue;
                PlayerProfile bp = ratingManager.getProfile(b.getUniqueId(), b.getName());
                int delta = Math.abs(ap.getElo() - bp.getElo());
                if (delta < bestDelta) { bestDelta=delta; best=v; }
            }
            if (best != null) {
                int base=c.getInt("queue.base-threshold",150);
                int expandPerSec=c.getInt("queue.expand-per-second",5);
                int allowed = base + expandPerSec * 10;
                if (bestDelta <= allowed || queue.size() > 6) {
                    queue.remove(u); queue.remove(best);
                    Player b=Bukkit.getPlayer(best); if (b==null){ if (!queue.contains(u)) queue.add(u); continue; }
                    startMatch(a,b);
                    break;
                }
            }
        }
    }

    private void startMatch(Player p1, Player p2){
        try { QueueActionBarNotifier.stop(p1); QueueActionBarNotifier.stop(p2); } catch (Throwable ignored) {}
        PlayerProfile A = ratingManager.getProfile(p1.getUniqueId(), p1.getName());
        PlayerProfile B = ratingManager.getProfile(p2.getUniqueId(), p2.getName());
        TierManager.Tier t1 = tierManager.ofElo(A.getElo());
        TierManager.Tier t2 = tierManager.ofElo(B.getElo());

        ArenaManager.Arena arena = arenaManager.chooseRandomReady();
        boolean require = plugin.getConfig().getBoolean("match.arena-required", false);
        if (arena == null && require) {
            p1.sendMessage(Util.color("&c[레이팅] 사용 가능한 아레나가 없어 매칭 취소."));
            p2.sendMessage(Util.color("&c[레이팅] 사용 가능한 아레나가 없어 매칭 취소."));
            return;
        }

        // Start session (save original positions)
        plugin.sessions().start(p1, p2, arena==null? null : arena.name);

        String prefix = plugin.getConfig().getString("ui.prefix", "&7[&a레이팅&7]&r ");
        String line = plugin.getConfig().getString("ui.matched", "&6매칭 성사! &f{p1} &7({elo1} {tier1}) &6vs &f{p2} &7({elo2} {tier2})");
        String msg = line.replace("{p1}", p1.getName()).replace("{p2}", p2.getName())
                .replace("{elo1}", String.valueOf(A.getElo())).replace("{elo2}", String.valueOf(B.getElo()))
                .replace("{tier1}", Util.color(t1.name)).replace("{tier2}", Util.color(t2.name));
        p1.sendMessage(Util.color(prefix + msg));
        p2.sendMessage(Util.color(prefix + msg));

        if (arena != null && arena.p1 != null && arena.p2 != null) {
            p1.teleport(arena.p1); p2.teleport(arena.p2);
        } else if (plugin.getConfig().getBoolean("match.teleport-on-match-found", false)) {
            Location st = stageLocation();
            if (st != null) { p1.teleport(st); p2.teleport(st); }
        }

        for (String raw : plugin.getConfig().getStringList("match.on-match-found-commands")) {
            String cmd = raw.replace("{p1}", p1.getName()).replace("{p2}", p2.getName())
                    .replace("{elo1}", String.valueOf(A.getElo())).replace("{elo2}", String.valueOf(B.getElo()))
                    .replace("{tier1}", Util.color(t1.name)).replace("{tier2}", Util.color(t2.name));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Util.color(cmd));
        }

        int pre = plugin.getConfig().getInt("match.pre-countdown-seconds", 3);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // extra delay to ensure tp settled
        }, 5L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Mark players as 'in battle' and auto-start
            BattleState.mark(p1.getUniqueId(), p2.getUniqueId());
            banEnforcer.enforcePreMatch(p1);
            banEnforcer.enforcePreMatch(p2);
            for (String raw : plugin.getConfig().getStringList("match.on-battle-start-commands")) {
                String cmd = raw.replace("{p1}", p1.getName()).replace("{p2}", p2.getName())
                        .replace("{elo1}", String.valueOf(A.getElo())).replace("{elo2}", String.valueOf(B.getElo()))
                        .replace("{tier1}", Util.color(t1.name)).replace("{tier2}", Util.color(t2.name));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Util.color(cmd));
            }
        }, pre*20L);
    }

    private Location stageLocation(){
        org.bukkit.configuration.file.FileConfiguration c = plugin.getConfig();
        String world = c.getString("match.stage-location.world", "bskyblock_world");
        org.bukkit.World w = Bukkit.getWorld(world);
        if (w==null) return null;
        double x=c.getDouble("match.stage-location.x",0.5);
        double y=c.getDouble("match.stage-location.y",100.0);
        double z=c.getDouble("match.stage-location.z",0.5);
        float yaw=(float)c.getDouble("match.stage-location.yaw",0.0);
        float pitch=(float)c.getDouble("match.stage-location.pitch",0.0);
        return new Location(w,x,y,z,yaw,pitch);
    }
}
