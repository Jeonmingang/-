package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit; import org.bukkit.Material; import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration; import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player; import org.bukkit.inventory.ItemStack; import org.bukkit.inventory.PlayerInventory;
import java.io.File; import java.io.IOException; import java.util.*;

public class RewardStore {
    private final UltimatePixelmonRatingPlugin plugin; private File file; private FileConfiguration conf;
    public RewardStore(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; }
    public void load(){ file=new File(plugin.getDataFolder(),"rewards.yml"); if (!file.exists()) plugin.saveResource("rewards.yml", false);
        conf=YamlConfiguration.loadConfiguration(file); if (!conf.isConfigurationSection("tiers")){ conf.createSection("tiers"); save(); } }
    public void save(){ try{ conf.save(file);} catch(IOException e){ plugin.getLogger().severe("rewards.yml save fail: "+e.getMessage()); } }
    public void setTierItems(String tierDisplay, ItemStack[] contents){
        List<ItemStack> items=new ArrayList<>(); for (ItemStack it: contents){ if (it==null) continue; if (it.getType()==Material.AIR) continue; items.add(it.clone()); }
        conf.set("tiers."+strip(Util.color(tierDisplay)), items); save();
    }
    public List<ItemStack> getTierItems(String tierDisplay){
        List<ItemStack> out=new ArrayList<>(); List<?> list=conf.getList("tiers."+strip(Util.color(tierDisplay)));
        if (list!=null) for(Object o:list){ if (o instanceof ItemStack) out.add(((ItemStack)o).clone()); } return out;
    }
    public void grantTierReward(PlayerProfile prof, TierManager.Tier tier){
        OfflinePlayer off=Bukkit.getOfflinePlayer(prof.getUuid()); List<ItemStack> items=getTierItems(tier.name); if (items.isEmpty()) return;
        if (off!=null && off.isOnline()){ Player p=off.getPlayer(); if (p!=null){ PlayerInventory inv=p.getInventory();
            for (ItemStack it: items){ java.util.Map<Integer,ItemStack> rem=inv.addItem(it); if (!rem.isEmpty()) for(ItemStack r: rem.values()) p.getWorld().dropItemNaturally(p.getLocation(), r); }
            p.sendMessage(Util.color("&a[레이팅] 시즌 보상 지급: "+Util.color(tier.name))); } }
    }
    private String strip(String s){ return s.replaceAll("§[0-9A-FK-ORa-fk-or]", ""); }
}