
package com.minkang.ultimate.pixelrating;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BanEnforcer {
    private final UltimatePixelmonRatingPlugin plugin;
    private final Set<Material> bannedItems = new HashSet<>();
    public BanEnforcer(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; reload(); }
    public void reload(){
        bannedItems.clear();
        List<String> items = plugin.getConfig().getStringList("bans.items");
        for (String s : items) {
            Material m = Material.matchMaterial(s);
            if (m != null) bannedItems.add(m);
        }
    }
    public boolean hasBannedItem(Player p){
        for (ItemStack it : p.getInventory().getContents()) { if (it!=null && bannedItems.contains(it.getType())) return true; }
        return false;
    }
    public void enforcePreMatch(Player p){
        boolean remove = plugin.getConfig().getString("bans.items-action","BLOCK").equalsIgnoreCase("REMOVE");
        if (!remove) return;
        for (ItemStack it : p.getInventory().getContents()) { if (it!=null && bannedItems.contains(it.getType())) p.getInventory().remove(it); }
    }
}
