
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class SeasonManager {
    private final UltimatePixelmonRatingPlugin plugin;
    private int taskId = -1;
    public SeasonManager(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; }

    public void startTicker(){
        stopTicker();
        if (!plugin.getConfig().getBoolean("season.enabled", true)) return;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 200L, 200L);
    }
    public void stopTicker(){ if (taskId!=-1) Bukkit.getScheduler().cancelTask(taskId); taskId=-1; }

    private void tick(){
        long endMs = plugin.getConfig().getLong("season.ends-at-epoch-ms", 0L);
        if (endMs <= 0) return;
        long now = System.currentTimeMillis();
        if (now < endMs) return;
        performReset();
        plugin.getConfig().set("season.ends-at-epoch-ms", 0L);
        plugin.saveConfig();
    }

    public void performReset(){
        List<PlayerProfile> all = new ArrayList<>(plugin.ratings().getAllProfiles());
        all.sort((a,b)->Integer.compare(b.getElo(), a.getElo()));
        for (PlayerProfile prof : all) {
            TierManager.Tier tier = plugin.tiers().ofElo(prof.getElo());
            plugin.rewards().grantTierReward(prof, tier);
        }
        String mode = plugin.getConfig().getString("season.reset-mode", "FULL");
        int initial = plugin.getConfig().getInt("rating.initial-elo", 1200);
        double soft = plugin.getConfig().getDouble("season.soft-carry", 0.25);
        for (PlayerProfile p : all) {
            if ("SOFT".equalsIgnoreCase(mode)) {
                int next = initial + (int)Math.round((p.getElo()-initial)*soft);
                p.setElo(next);
            } else {
                p.setElo(initial);
            }
            p.setWins(0); p.setLosses(0); p.setDraws(0); p.setWinStreak(0);
            plugin.ratings().saveProfile(p);
        }
        Bukkit.broadcastMessage(Util.color("&6[레이팅] 시즌 종료! 보상 지급 및 Elo 초기화."));
    }

    public String leftString(){
        long endMs = plugin.getConfig().getLong("season.ends-at-epoch-ms", 0L);
        if (endMs <= 0) return "비활성";
        long left = Math.max(0, endMs - System.currentTimeMillis());
        return Util.timeLeft(left);
    }

    public void setSeasonSecondsFromNow(long sec){
        plugin.getConfig().set("season.ends-at-epoch-ms", System.currentTimeMillis() + sec*1000L);
        plugin.saveConfig();
    }
}
