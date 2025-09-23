package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class RewardManager {

    private final UltimatePixelmonRatingPlugin plugin;

    public RewardManager(UltimatePixelmonRatingPlugin plugin) {
        this.plugin = plugin;
    }

    // --- Season payout entrypoints (generic map to avoid type-erasure clashes) ---
    public void grantSeasonRewards() { /* placeholder for scheduler hook */ }
    public void distributeSeasonRewards() { /* placeholder */ }
    public void payoutSeasonRewards() { /* placeholder */ }
    public void giveSeasonRewards() { /* placeholder */ }

    public void grantSeasonRewards(Map<?, Integer> ranking, TierManager tiers) { /* not implemented here */ }
    public void distributeSeasonRewards(Map<?, Integer> ranking, TierManager tiers) { /* not implemented here */ }
    public void payoutSeasonRewards(Map<?, Integer> ranking, TierManager tiers) { /* not implemented here */ }
    public void giveSeasonRewards(Map<?, Integer> ranking, TierManager tiers) { /* not implemented here */ }

    // --- Single player tier reward API used by SeasonManager ---
    public void grantTierReward(PlayerProfile prof, TierManager.Tier tier){
        if (prof == null || tier == null) return;
        grantRewards(prof.getUuid(), tier.name);
    }

    /** Returns commands for a tier, e.g. ["eco give {player} 1000", "give {player} diamond 3"] */
    public List<String> getTierReward(String tier){
        if (tier == null) return Collections.emptyList();
        FileConfiguration cfg = plugin.getConfig();
        List<String> list = cfg.getStringList("rewards." + tier);
        if (list == null) list = Collections.emptyList();
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /** Replace {player} token and dispatch commands as console. */
    public void grantRewards(Player player, String tier) {
        if (player == null || tier == null) return;
        for (String cmd : getTierReward(tier)) {
            String built = cmd.replace("{player}", player.getName());
            if (built.trim().isEmpty()) continue;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), built);
        }
    }

    public void grantRewards(UUID uuid, String tier) {
        if (uuid == null) return;
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) grantRewards(p, tier);
    }

    public void load() { /* no-op */ }
    public void save() { /* no-op */ }
}
