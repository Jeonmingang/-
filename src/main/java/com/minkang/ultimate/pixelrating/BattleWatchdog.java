
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;

public class BattleWatchdog implements Runnable {
    private final UltimatePixelmonRatingPlugin plugin;
    private int taskId = -1;
    public BattleWatchdog(UltimatePixelmonRatingPlugin plugin){ this.plugin = plugin; }
    public void start(){
        int period = 20 * 15; // 15s
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, period, period);
    }
    public void stop(){ if (taskId!=-1) { Bukkit.getScheduler().cancelTask(taskId); taskId=-1; } }
    @Override public void run(){
        int max = plugin.getConfig().getInt("battle.max-seconds", 900);
        long now = System.currentTimeMillis();
        for (MatchSessionManager.Session s : plugin.sessions().all()){
            if (now - s.startedAt > max * 1000L){
                // timeout -> draw to release players, avoid stuck state
                org.bukkit.entity.Player a = Bukkit.getPlayer(s.a);
                org.bukkit.entity.Player b = Bukkit.getPlayer(s.b);
                if (a != null && b != null){
                    plugin.ratings().recordResult(a, b, true);
                    plugin.getLogger().warning("[UPR] Watchdog ended a battle (timeout) between " + a.getName() + " and " + b.getName());
                } else {
                    // Just clear if offline
                    BattleState.clear(s.a, s.b);
                    plugin.sessions().remove(a!=null? a.getUniqueId():s.a, b!=null? b.getUniqueId():s.b);
                }
            }
        }
    }
}
