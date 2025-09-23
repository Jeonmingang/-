package com.minkang.ultimate.pixelrating;

import java.io.File;
import java.io.IOException;

public class SeasonManager {
    private final UltimatePixelmonRatingPlugin plugin;
    private final org.bukkit.configuration.file.FileConfiguration yml;
    private final File file;

    public SeasonManager(UltimatePixelmonRatingPlugin plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "season.yml");
        if (!file.exists()) try { file.createNewFile(); } catch (IOException ignored){}
        this.yml = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
    }

    public long getSeasonEndEpoch(){
        return yml.getLong("endEpoch", 0L);
    }

    public void setSeasonEndEpoch(long epoch){
        yml.set("endEpoch", epoch);
        try { yml.save(file);} catch (IOException ignored){}
    }

    public void resetSeason(RatingManager ratings){
        ratings.resetAll();
    }
}
