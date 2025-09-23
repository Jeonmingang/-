package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimatePixelmonRatingPlugin extends JavaPlugin {

    private static UltimatePixelmonRatingPlugin inst;

    private ArenaManager arenas;
    private RatingManager ratings;
    private QueueManager queue;
    private MatchSessionManager sessions;
    private BattleResultDetector detector;
    private BanEnforcer bans;
    private TierManager tiers;
    private RewardManager rewardMgr;
    private QueueActionBarNotifier actionbar;
    private BattleWatchdog watchdog;
    private SeasonManager season;

    public static UltimatePixelmonRatingPlugin get() { return inst; }

    @Override
    public void onEnable() {
        inst = this;
        saveDefaultConfig();

        this.arenas = new ArenaManager(this); this.arenas.load();
        this.ratings = new RatingManager(this);
        this.sessions = new MatchSessionManager(this);
        this.queue = new QueueManager(this, arenas, sessions, ratings);
        this.tiers = new TierManager(this);

        this.detector = new BattleResultDetector(this, sessions, ratings); this.detector.enable();

        this.bans = new BanEnforcer(this, sessions);
        this.rewardMgr = new RewardManager(this, tiers);
        this.actionbar = new QueueActionBarNotifier(this, queue);
        this.watchdog = new BattleWatchdog(this, sessions, ratings);
        this.season = new SeasonManager(this);

        RatingCommand rc = new RatingCommand(this, arenas, queue, ratings, tiers, rewardMgr);
        getCommand("레이팅").setExecutor(rc);
        getCommand("레이팅").setTabCompleter(rc);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(detector, this);
        pm.registerEvents(bans, this);
        pm.registerEvents(new RewardGUI(this, rewardMgr, tiers), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        if (arenas != null) arenas.save();
        if (ratings != null) ratings.save();
    }
}
