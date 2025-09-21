package com.minkang.ultimate.pixelrating;

import org.bukkit.Material; import org.bukkit.entity.Player; import org.bukkit.inventory.ItemStack; import java.util.*;

public class BanEnforcer {
    private final UltimatePixelmonRatingPlugin plugin; private final java.util.Set<Material> bannedItems=new java.util.HashSet<>();
    public BanEnforcer(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; reload(); }
    public void reload(){ bannedItems.clear(); java.util.List<String> items=plugin.getConfig().getStringList("bans.items");
        for(String s: items){ Material m=Material.matchMaterial(s); if (m!=null) bannedItems.add(m); } }
    public boolean hasBannedItem(Player p){ for (ItemStack it: p.getInventory().getContents()){ if (it==null) continue; if (bannedItems.contains(it.getType())) return true; } return false; }
    public void enforcePreMatch(Player p){
        String action=plugin.getConfig().getString("bans.items-action","BLOCK"); if (!"REMOVE".equalsIgnoreCase(action)) return;
        for (ItemStack it: p.getInventory().getContents()){ if (it==null) continue; if (bannedItems.contains(it.getType())) p.getInventory().remove(it); }
    }
    public String banItemsCsv(){ java.util.List<String> names=plugin.getConfig().getStringList("bans.items"); return String.join(",", names); }
    public String banPokemonCsv(){ java.util.List<String> names=plugin.getConfig().getStringList("bans.pokemon"); return String.join(",", names); }
}