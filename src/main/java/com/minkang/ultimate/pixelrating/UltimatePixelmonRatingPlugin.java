
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimatePixelmonRatingPlugin extends JavaPlugin {
    public int getQueueSize(){ return matchmakingService.size(); }
    public int getActiveArenaCount(){ return arenaManager.countEnabled(); }
    private static UltimatePixelmonRatingPlugin instance;
    private StorageYAML storage;
    private RatingManager ratingManager;
    private MatchmakingService matchmakingService;
    private ChatParseListener chatParseListener;
    private TierManager tierManager;
    private SeasonManager seasonManager;
    private RewardStore rewardStore;
    private RewardGUI rewardGUI;
    private BanEnforcer banEnforcer;
    private ArenaManager arenaManager;
    private MatchSessionManager sessionManager;

    public static UltimatePixelmonRatingPlugin getInstance(){ return instance; }

    @Override
    public void onEnable() {
        instance=this;
        saveDefaultConfig();
        saveResource("rewards.yml", false);
        saveResource("arenas.yml", false);

        storage = new StorageYAML(this); storage.loadAll();
        rewardStore = new RewardStore(this); rewardStore.load();
        tierManager = new TierManager(this);
        seasonManager = new SeasonManager(this);
        banEnforcer = new BanEnforcer(this);
        arenaManager = new ArenaManager(this); arenaManager.load();
        sessionManager = new MatchSessionManager(this);

        ratingManager = new RatingManager(this, storage, tierManager);
        matchmakingService = new MatchmakingService(this, ratingManager, tierManager, banEnforcer, arenaManager); matchmakingService.start();

        rewardGUI = new RewardGUI(this, rewardStore); Bukkit.getPluginManager().registerEvents(rewardGUI, this);

        Bukkit.getPluginManager().registerEvents(new BattleGuardListener(this), this);

        chatParseListener = new ChatParseListener(this, ratingManager);
        if (getConfig().getBoolean("auto-result-detection.enable-chat-parse", false)) Bukkit.getPluginManager().registerEvents(chatParseListener, this);

        RatingCommand cmd = new RatingCommand(this, ratingManager, tierManager, seasonManager, rewardGUI, arenaManager);
        PluginCommand ratingKr = getCommand("레이팅");
        if (ratingKr != null) { ratingKr.setExecutor(cmd); ratingKr.setTabCompleter(cmd); }

        org.bukkit.command.PluginCommand comp = getCommand("경쟁전");
        if (comp != null) { comp.setExecutor(cmd); comp.setTabCompleter(cmd); }

        seasonManager.startTicker();
        getLogger().info("UltimatePixelmonRating v1.3.0 enabled.");
    }

    @Override
    public void onDisable() {
        if (matchmakingService != null) matchmakingService.stop();
        if (seasonManager != null) seasonManager.stopTicker();
        if (storage != null) storage.saveAll();
        if (rewardStore != null) rewardStore.save();
        if (arenaManager != null) arenaManager.save();
        getLogger().info("UltimatePixelmonRating disabled.");
    }

    public StorageYAML storage(){ return storage; }
    public RatingManager ratings(){ return ratingManager; }
    public MatchmakingService queue(){ return matchmakingService; }
    public TierManager tiers(){ return tierManager; }
    public SeasonManager season(){ return seasonManager; }
    public RewardStore rewards(){ return rewardStore; }
    public RewardGUI rewardGUI(){ return rewardGUI; }
    public ArenaManager arenas(){ return arenaManager; }
    public MatchSessionManager sessions(){ return sessionManager; }
}
