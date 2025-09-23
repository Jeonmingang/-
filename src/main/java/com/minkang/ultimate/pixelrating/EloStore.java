package com.minkang.ultimate.pixelrating;
import org.bukkit.configuration.file.YamlConfiguration; import java.io.*; import java.util.*; import java.util.logging.Level;
public class EloStore {
  private final UltimatePixelmonRatingPlugin plugin; private final File file; private YamlConfiguration yml;
  public EloStore(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; this.file=new File(plugin.getDataFolder(), plugin.getConfig().getString("storage.ratings-file","ratings.yml")); }
  public void load(){ ensure(); this.yml=YamlConfiguration.loadConfiguration(file); }
  public synchronized void save(){ ensure(); try{ this.yml.save(file);} catch(IOException e){ plugin.getLogger().log(Level.SEVERE,"[UPR] Failed to save ratings.yml",e);} }
  public int get(java.util.UUID id, int def){ ensure(); String key="elo."+id.toString(); return yml.getInt(key, def); }
  public void set(java.util.UUID id, int value){ ensure(); String key="elo."+id.toString(); yml.set(key, value); }
  public Map<java.util.UUID,Integer> all(){ ensure(); Map<java.util.UUID,Integer> map=new HashMap<>(); if(yml.isConfigurationSection("elo")) for(String k: yml.getConfigurationSection("elo").getKeys(false)){ try{ map.put(java.util.UUID.fromString(k), yml.getInt("elo."+k)); }catch(IllegalArgumentException ignored){} } return map; }
  private void ensure(){ try{ if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs(); if(this.yml==null){ if(!file.exists()){ file.createNewFile(); YamlConfiguration seed=new YamlConfiguration(); seed.createSection("elo"); seed.save(file);} this.yml=YamlConfiguration.loadConfiguration(file);} } catch (IOException e){ plugin.getLogger().log(Level.SEVERE,"[UPR] Failed to prepare ratings.yml", e);} }
}