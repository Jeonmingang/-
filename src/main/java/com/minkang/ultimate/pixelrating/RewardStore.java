
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RewardStore {
    private final UltimatePixelmonRatingPlugin plugin;
    private File file;
    private FileConfiguration conf;
    public RewardStore(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; }
    public void load(){
        file = new File(plugin.getDataFolder(), "rewards.yml");
        if (!file.exists()) plugin.saveResource("rewards.yml", false);
        conf = YamlConfiguration.loadConfiguration(file);
        if (!conf.isConfigurationSection("tiers")) { conf.createSection("tiers"); save(); }
    }
    public void save(){ try { conf.save(file);} catch (IOException e){ plugin.getLogger().severe("rewards.yml save fail: "+e.getMessage()); } }
    public void setTierItems(String tier, ItemStack[] contents){
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack it : contents) { if (it!=null && it.getType()!= Material.AIR) items.add(it.clone()); }
        conf.set("tiers."+strip(Util.color(tier)), items); save();
    }
    public List<ItemStack> getTierItems(String tier){
        List<ItemStack> out = new ArrayList<>(); List<?> list = conf.getList("tiers."+strip(Util.color(tier)));
        if (list != null) for (Object o : list) if (o instanceof ItemStack) out.add(((ItemStack)o).clone());
        return out;
    }
    public void grantTierReward(PlayerProfile p, TierManager.Tier tier){
        List<ItemStack> items = getTierItems(tier.name);
        if (items.isEmpty()) return;
        OfflinePlayer off = Bukkit.getOfflinePlayer(p.getUuid());
        if (off != null && off.isOnline()) {
            Player pl = off.getPlayer();
            if (pl != null) {
                PlayerInventory inv = pl.getInventory();
                for (ItemStack it : items) {
                    HashMap<Integer, ItemStack> rem = inv.addItem(it);
                    if (!rem.isEmpty()) for (ItemStack r : rem.values()) pl.getWorld().dropItemNaturally(pl.getLocation(), r);
                }
                pl.sendMessage(Util.color("&a[레이팅] 시즌 보상이 지급되었습니다: " + Util.color(tier.name)));
            }
        }
    }
    private String strip(String s){ return s.replaceAll("§[0-9A-FK-ORa-fk-or]", ""); }
}
