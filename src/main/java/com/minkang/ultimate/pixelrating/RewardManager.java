package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class RewardManager {
    private final UltimatePixelmonRatingPlugin plugin;
    private final RewardStore store;
    private final TierManager tiers;

    public RewardManager(UltimatePixelmonRatingPlugin plugin, TierManager tiers){
        this.plugin = plugin;
        this.tiers = tiers;
        this.store = new RewardStore(plugin);
    }

    public void openGUI(Player p, int rating){
        new RewardGUI(plugin, this, tiers).open(p, rating);
    }

    public void grant(Player p, String key){
        if (store.isClaimed(p.getUniqueId(), key)) {
            p.sendMessage(Util.color("&e이미 수령한 보상입니다."));
            return;
        }
        List<String> cmds = plugin.getConfig().getStringList("rewards."+key+".commands");
        for (String cmd : cmds){
            String run = cmd.replace("{player}", p.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), run);
        }
        store.markClaimed(p.getUniqueId(), key);
        p.sendMessage(Util.color("&a보상을 수령했습니다."));
    }
}
