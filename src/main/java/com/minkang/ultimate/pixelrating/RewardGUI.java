package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RewardGUI implements Listener {
    private final UltimatePixelmonRatingPlugin plugin;
    private final RewardManager rewards;
    private final TierManager tiers;

    public RewardGUI(UltimatePixelmonRatingPlugin plugin, RewardManager rewards, TierManager tiers){
        this.plugin = plugin;
        this.rewards = rewards;
        this.tiers = tiers;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p, int rating){
        String tier = tiers.tierOf(rating);
        Inventory inv = Bukkit.createInventory(null, 27, Util.color("&6시즌 보상 ("+tier+")"));
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Util.color("&a보상 수령"));
        it.setItemMeta(meta);
        inv.setItem(13, it);
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView().getTitle() == null) return;
        if (!org.bukkit.ChatColor.stripColor(e.getView().getTitle()).contains("시즌 보상")) return;
        e.setCancelled(true);
        if (e.getRawSlot() == 13){
            Player p = (Player)e.getWhoClicked();
            rewards.grant(p, "season");
            p.closeInventory();
        }
    }
}
