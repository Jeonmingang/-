package com.minkang.ultimate.pixelrating;
import org.bukkit.configuration.file.YamlConfiguration; import java.io.*; import java.util.*;
public class RewardStore { private final UltimatePixelmonRatingPlugin plugin; private File file; private YamlConfiguration yml;
  public RewardStore(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; }
  public void load(){ file=new File(plugin.getDataFolder(),"rewards.yml"); if(!file.exists()) plugin.saveResource("rewards.yml", false); yml=YamlConfiguration.loadConfiguration(file); }
  public void save(){ if(yml==null||file==null) return; try{ yml.save(file);}catch(IOException ignored){} }
  public java.util.List<String> getCommandsForTier(String tier){ if(yml==null) load(); return new java.util.ArrayList<>(yml.getStringList("tiers."+org.bukkit.ChatColor.stripColor(tier)+".commands")); }
  public void setCommandsForTier(String tier, java.util.List<String> cmds){ if(yml==null) load(); yml.set("tiers."+org.bukkit.ChatColor.stripColor(tier)+".commands", new java.util.ArrayList<>(cmds)); save(); }
}