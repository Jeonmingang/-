
package com.minkang.ultimate.pixelrating;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StorageYAML {
    private final UltimatePixelmonRatingPlugin plugin;
    private final File file;
    private FileConfiguration data;
    public StorageYAML(UltimatePixelmonRatingPlugin plugin){
        this.plugin=plugin;
        File dir=plugin.getDataFolder();
        if (!dir.exists()) { boolean ok=dir.mkdirs(); if (!ok) plugin.getLogger().warning("Cannot create data folder"); }
        this.file=new File(dir,"players.yml");
    }
    public void loadAll(){ if (!file.exists()) { try { file.createNewFile(); } catch (IOException ignored){} } data=YamlConfiguration.loadConfiguration(file); }
    public void saveAll(){ if (data==null) return; try { data.save(file);} catch (IOException e){ plugin.getLogger().severe("Save players.yml failed: "+e.getMessage()); } }
    public void save(PlayerProfile p){
        String b="players."+p.getUuid().toString();
        data.set(b+".name", p.getLastKnownName());
        data.set(b+".elo", p.getElo());
        data.set(b+".wins", p.getWins());
        data.set(b+".losses", p.getLosses());
        data.set(b+".draws", p.getDraws());
        data.set(b+".streak", p.getWinStreak());
        data.set(b+".lastMatchAt", p.getLastMatchAt());
        saveAll();
    }
    public PlayerProfile load(UUID id){
        String b="players."+id.toString();
        if (!data.contains(b)) return null;
        PlayerProfile p=new PlayerProfile(id, data.getString(b+".name","Unknown"));
        p.setElo(data.getInt(b+".elo",1200));
        p.setWins(data.getInt(b+".wins",0));
        p.setLosses(data.getInt(b+".losses",0));
        p.setDraws(data.getInt(b+".draws",0));
        p.setWinStreak(data.getInt(b+".streak",0));
        p.setLastMatchAt(data.getLong(b+".lastMatchAt",0L));
        return p;
    }
    public Set<UUID> allUUIDs(){
        Set<UUID> out=new HashSet<>();
        if (data==null || !data.isConfigurationSection("players")) return out;
        for (String k : data.getConfigurationSection("players").getKeys(false)) { try { out.add(UUID.fromString(k)); } catch (IllegalArgumentException ignored){} }
        return out;
    }
}
