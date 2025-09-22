
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RewardGUI implements Listener {
    private final UltimatePixelmonRatingPlugin plugin;
    private final RewardStore store;
    private final java.util.Map<String, String> editing = new java.util.HashMap<>();
    public RewardGUI(UltimatePixelmonRatingPlugin plugin, RewardStore store){ this.plugin=plugin; this.store=store; }
    public void openEditor(Player p, String tierDisplay){
        Inventory inv = Bukkit.createInventory(p, 27, Util.color("&6보상편집: " + tierDisplay));
        for (ItemStack it : store.getTierItems(tierDisplay)) inv.addItem(it);
        ItemStack info = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) { meta.setDisplayName(Util.color("&e인벤 닫으면 저장됨")); info.setItemMeta(meta); }
        inv.setItem(26, info);
        editing.put(p.getName(), tierDisplay);
        p.openInventory(inv);
    }
    @EventHandler public void onClose(InventoryCloseEvent e){
        String name = e.getPlayer().getName();
        if (!editing.containsKey(name)) return;
        String tier = editing.remove(name);
        store.setTierItems(tier, e.getInventory().getContents());
        e.getPlayer().sendMessage(Util.color(plugin.getConfig().getString("ui.saved-rewards","&a보상 {tier} 설정 완료!").replace("{tier}", tier)));
    }
}
