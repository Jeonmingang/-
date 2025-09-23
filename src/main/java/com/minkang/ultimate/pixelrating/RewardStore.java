package com.minkang.ultimate.pixelrating;

import java.io.File;
import java.io.IOException;

public class RewardStore {
    private final UltimatePixelmonRatingPlugin plugin;
    private final File file;
    private org.bukkit.configuration.file.FileConfiguration yml;

    public RewardStore(UltimatePixelmonRatingPlugin plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "rewards_state.yml");
        reload();
    }
    public void reload(){
        if (!file.exists()) try { file.createNewFile(); } catch (IOException ignored) {}
        this.yml = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
    }
    public boolean isClaimed(java.util.UUID id, String key){
        return yml.getBoolean(id.toString()+"."+key, false);
    }
    public void markClaimed(java.util.UUID id, String key){
        yml.set(id.toString()+"."+key, true);
        try { yml.save(file);} catch (IOException ignored){}
    }
}
