package com.minkang.ultimate.pixelrating;

import com.minkang.ultimate.pixelrating.commands.RatingCommand;
import com.minkang.ultimate.pixelrating.elo.EloService;
import com.minkang.ultimate.pixelrating.listeners.BukkitCommandBlocker;
import com.minkang.ultimate.pixelrating.listeners.ForgePixelmonBridge;
import com.minkang.ultimate.pixelrating.store.RatingStore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimatePixelmonRatingPlugin extends JavaPlugin {

    private EloService eloService;
    private RatingStore ratingStore;
    private ForgePixelmonBridge forgeBridge;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.ratingStore = new RatingStore(this);
        this.eloService = new EloService(this, ratingStore);

        // Bukkit listeners + command
        Bukkit.getPluginManager().registerEvents(new BukkitCommandBlocker(this), this);
        RatingCommand cmd = new RatingCommand(this, eloService, ratingStore);
        getCommand("레이팅").setExecutor(cmd);
        getCommand("레이팅").setTabCompleter(cmd);

        // Forge Pixelmon hook via reflection
        this.forgeBridge = new ForgePixelmonBridge(this, eloService);
        this.forgeBridge.tryRegister();

        getLogger().info("[UltimatePixelmonRating] Enabled.");
    }

    @Override
    public void onDisable() {
        if (this.forgeBridge != null) this.forgeBridge.tryUnregister();
        ratingStore.save();
        getLogger().info("[UltimatePixelmonRating] Disabled.");
    }
}
