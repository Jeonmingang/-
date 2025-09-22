package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class RewardManager {

    private final UltimatePixelmonRatingPlugin plugin;

    public RewardManager(UltimatePixelmonRatingPlugin plugin) {
        this.plugin = plugin;
    }

    // --- Common season payout APIs (overloaded so callers compile) ---
    public void grantSeasonRewards() { /* no-op stub */ }
    public void distributeSeasonRewards() { /* no-op stub */ }
    public void payoutSeasonRewards() { /* no-op stub */ }
    public void giveSeasonRewards() { /* no-op stub */ }

    public void grantSeasonRewards(Map<UUID, Integer> ranking, TierManager tiers) { /* no-op stub */ }
    public void distributeSeasonRewards(Map<UUID, Integer> ranking, TierManager tiers) { /* no-op stub */ }
    public void payoutSeasonRewards(Map<UUID, Integer> ranking, TierManager tiers) { /* no-op stub */ }
    public void giveSeasonRewards(Map<UUID, Integer> ranking, TierManager tiers) { /* no-op stub */ }

    public void grantSeasonRewards(Map<String, Integer> ranking, TierManager tiers) { /* no-op stub */ }
    public void distributeSeasonRewards(Map<String, Integer> ranking, TierManager tiers) { /* no-op stub */ }
    public void payoutSeasonRewards(Map<String, Integer> ranking, TierManager tiers) { /* no-op stub */ }
    public void giveSeasonRewards(Map<String, Integer> ranking, TierManager tiers) { /* no-op stub */ }

    // --- Per-tier reward helpers ---
    public void setTierReward(String tier, List<String> commands) { /* no-op stub */ }
    public List<String> getTierReward(String tier) { return Collections.emptyList(); }

    public void grantRewards(Player player, String tier) {
        // Example: dispatch configured commands; stubbed
        for (String cmd : getTierReward(tier)) {
            String built = cmd.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), built);
        }
    }

    public void grantRewards(UUID uuid, String tier) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) grantRewards(p, tier);
    }

    public void load() { /* no-op */ }
    public void save() { /* no-op */ }
}