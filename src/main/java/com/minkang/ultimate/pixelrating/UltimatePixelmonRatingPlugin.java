package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimatePixelmonRatingPlugin extends JavaPlugin {
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

    public static UltimatePixelmonRatingPlugin getInstance(){ return instance; }

    @Override public void onEnable(){
        instance=this;
        saveDefaultConfig(); saveResource("rewards.yml", false);
        storage=new StorageYAML(this); storage.loadAll();
        rewardStore=new RewardStore(this); rewardStore.load();
        tierManager=new TierManager(this);
        seasonManager=new SeasonManager(this);
        banEnforcer=new BanEnforcer(this);

        ratingManager=new RatingManager(this, storage, tierManager);
        matchmakingService=new MatchmakingService(this, ratingManager, tierManager, banEnforcer);
        matchmakingService.start();

        rewardGUI=new RewardGUI(this, rewardStore);
        Bukkit.getPluginManager().registerEvents(rewardGUI, this);

        chatParseListener=new ChatParseListener(this, ratingManager);
        if (getConfig().getBoolean("auto-result-detection.enable-chat-parse", false)){
            Bukkit.getPluginManager().registerEvents(chatParseListener, this);
        }

        RatingCommand cmd=new RatingCommand(this, ratingManager, tierManager, seasonManager, rewardGUI, banEnforcer, chatParseListener);
        PluginCommand k=getCommand("레이팅"); if (k!=null){ k.setExecutor(cmd); k.setTabCompleter(cmd); }

        seasonManager.startTicker();
        getLogger().info("UltimatePixelmonRating v1.1.3 enabled.");
    }

    @Override public void onDisable(){
        if (matchmakingService!=null) matchmakingService.stop();
        if (seasonManager!=null) seasonManager.stopTicker();
        if (storage!=null) storage.saveAll();
        if (rewardStore!=null) rewardStore.save();
    }

    public StorageYAML storage(){ return storage; }
    public RatingManager ratings(){ return ratingManager; }
    public MatchmakingService queue(){ return matchmakingService; }
    public TierManager tiers(){ return tierManager; }
    public SeasonManager season(){ return seasonManager; }
    public RewardStore rewards(){ return rewardStore; }
}