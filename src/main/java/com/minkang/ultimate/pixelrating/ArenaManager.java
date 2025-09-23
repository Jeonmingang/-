package com.minkang.ultimate.pixelrating;
import org.bukkit.Location; import org.bukkit.configuration.ConfigurationSection; import org.bukkit.configuration.file.YamlConfiguration;
import java.io.*; import java.util.*; import java.util.logging.Level;
public class ArenaManager {
  private final UltimatePixelmonRatingPlugin plugin; private final File arenasFile; private YamlConfiguration yml; private final Map<String,Arena> arenas=new LinkedHashMap<>(); private final Random rng=new Random();
  public ArenaManager(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; this.arenasFile=new File(plugin.getDataFolder(),"arenas.yml"); }
  public void load(){ ensureFileReady(); this.yml=YamlConfiguration.loadConfiguration(arenasFile); arenas.clear();
    ConfigurationSection sec=yml.getConfigurationSection("arenas"); if(sec!=null){ for(String id:sec.getKeys(false)){ ConfigurationSection child=sec.getConfigurationSection(id); if(child!=null) arenas.put(id.toLowerCase(Locale.ROOT), Arena.fromConfig(id,child)); } }
    plugin.getLogger().info("[UPR] Arenas loaded: "+arenas.size()); }
  public synchronized void save(){ ensureFileReady(); if(this.yml==null) this.yml=new YamlConfiguration(); this.yml.set("arenas",null); ConfigurationSection sec=this.yml.createSection("arenas");
    for(Map.Entry<String,Arena> e: arenas.entrySet()){ ConfigurationSection s=sec.createSection(e.getKey()); e.getValue().toConfig(s);} try{ this.yml.save(arenasFile);}catch(IOException ex){ plugin.getLogger().log(Level.SEVERE,"[UPR] Failed to save arenas.yml",ex);} }
  private void ensureFileReady(){ try{ if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs(); if(!arenasFile.exists()){ arenasFile.createNewFile(); YamlConfiguration seed=new YamlConfiguration(); seed.createSection("arenas"); seed.save(arenasFile);} }catch(IOException e){ plugin.getLogger().log(Level.SEVERE,"[UPR] Failed to prepare arenas.yml",e);} }
  public Arena create(String id){ String key=id.toLowerCase(Locale.ROOT); if(arenas.containsKey(key)) throw new IllegalArgumentException("이미 존재하는 경기장 ID 입니다."); Arena a=new Arena(key); arenas.put(key,a); save(); return a; }
  public boolean exists(String id){ return arenas.containsKey(id.toLowerCase(Locale.ROOT)); }
  public Arena get(String id){ return arenas.get(id.toLowerCase(Locale.ROOT)); }
  public java.util.List<Arena> list(){ return new java.util.ArrayList<>(arenas.values()); }
  public void set(String id,String point,Location loc){ Arena a=get(id); if(a==null) throw new IllegalArgumentException("없는 경기장 ID 입니다.");
    if("p1".equalsIgnoreCase(point)) a.setP1(loc); else if("p2".equalsIgnoreCase(point)) a.setP2(loc); else throw new IllegalArgumentException("point 는 p1 또는 p2 이어야 합니다."); save(); }
  public void setEnabled(String id, boolean enabled){ Arena a=get(id); if(a==null) throw new IllegalArgumentException("없는 경기장 ID 입니다."); a.setEnabled(enabled); save(); }
  public Arena chooseRandomReady(){ java.util.List<Arena> ready=new java.util.ArrayList<>(); for(Arena a: arenas.values()){ if(a.isEnabled() && a.getP1()!=null && a.getP2()!=null) ready.add(a);} if(ready.isEmpty()) return null; return ready.get(rng.nextInt(ready.size())); }
}