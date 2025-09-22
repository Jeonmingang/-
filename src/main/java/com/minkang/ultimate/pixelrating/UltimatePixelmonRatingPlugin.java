package com.minkang.ultimate.pixelrating;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class UltimatePixelmonRatingPlugin extends JavaPlugin {

    private static UltimatePixelmonRatingPlugin instance;
    private ArenaManager arenaManager;

    public static UltimatePixelmonRatingPlugin get() { return instance; }
    public ArenaManager arenas() { return arenaManager; }

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig();

        arenaManager = new ArenaManager(this);
        arenaManager.load();

        PluginCommand cmd = getCommand("레이팅");
        if (cmd != null) {
            cmd.setExecutor(new RatingCommand(this));
            cmd.setTabCompleter(new RatingTab(this));
        } else {
            getLogger().severe("[UPR] Command '레이팅' not found in plugin.yml!");
        }

        try { ProtocolHook.safeInit(this); }
        catch (Throwable t) { getLogger().log(Level.WARNING, "[UPR] ProtocolLib hook init failed: " + t.getClass().getName()); }

        getLogger().info("[UltimatePixelmonRating] Enabled v1.6.1-hotfix");
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) arenaManager.save();
        getLogger().info("[UltimatePixelmonRating] Disabled.");
    }
}