package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class UltimatePixelmonRatingPlugin extends JavaPlugin {

    // --- Managers / Components (some may be wired later) ---
    private RatingManager ratings;
    private MatchSessionManager sessions;
    private BattleResultDetector detector;
    private BattleWatchdog watchdog;

    private StorageYAML storage;
    private TierManager tiers;
    private SeasonManager season;
    private RewardManager rewards;
    private ArenaManager arenas;
    private QueueManager queue;

    // ProtocolLib hook stub
    private Object protocolHook;

    // --- Getters used across the project ---
    public RatingManager ratings() { return ratings; }
    public MatchSessionManager sessions() { return sessions; }
    public BattleResultDetector detector() { return detector; }
    public BattleWatchdog watchdog() { return watchdog; }

    public StorageYAML storage() { return storage; }
    public TierManager tiers() { return tiers; }
    public SeasonManager season() { return season; }
    public RewardManager rewards() { return rewards; }
    public ArenaManager arenas() { return arenas; }
    public QueueManager queue() { return queue; }

    // Used by QueueActionBarNotifier
    public int getQueueSize() {
        return queue != null ? queue.size() : 0;
    }

    public int getActiveArenaCount() {
        return arenas != null ? arenas.countEnabled() : 0;
    }

    @Override
    public void onEnable() {
        final Logger log = getLogger();
        saveDefaultConfig();

        // --- Core managers ---
        this.storage  = new StorageYAML(this);
        this.tiers    = new TierManager(this);
        this.ratings  = new RatingManager(this, storage, tiers);
        this.sessions = new MatchSessionManager(this);
        this.queue    = new QueueManager(this);
        this.season   = new SeasonManager(this);
        this.arenas   = new ArenaManager(this);
        this.rewards  = new RewardManager(this);

        // Detector must be alive for chat/packet parsing
        this.detector = new BattleResultDetector(this, this.ratings);

        // Season ticker & watchdog (fail-safe)
        try { this.season.startTicker(); } catch (Throwable ignored) {}
        try {
            this.watchdog = new BattleWatchdog(this);
            this.watchdog.start();
        } catch (Throwable ignored) { this.watchdog = null; }

        // Listeners & command
        try {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(new ChatParseListener(this), this);
            pm.registerEvents(new BattleGuardListener(this), this);

            RewardStore rewardStore = new RewardStore(this);
            RewardGUI rewardGUI = new RewardGUI(this, rewardStore);
            pm.registerEvents(rewardGUI, this);

            RatingCommand rc = new RatingCommand(this, ratings, tiers, season, rewardGUI, arenas);
            if (getCommand("레이팅") != null) {
                getCommand("레이팅").setExecutor(rc);
                getCommand("레이팅").setTabCompleter(rc);
            } else {
                log.warning("[UPR] '레이팅' command not defined in plugin.yml");
            }
        } catch (Throwable t) {
            log.warning("[UPR] Listener/Command wiring failed: " + t.getMessage());
        }

        // ProtocolLib packet hook (reflection; optional at runtime)
        try {
            PluginManager pm = getServer().getPluginManager();
            Plugin proto = pm.getPlugin("ProtocolLib");
            if (proto != null && proto.isEnabled()) {
                try {
                    Class<?> cls = Class.forName("com.minkang.ultimate.pixelrating.ProtocolHook");
                    Object hook = cls.getConstructor(UltimatePixelmonRatingPlugin.class).newInstance(this);
                    java.lang.reflect.Method init = cls.getMethod("init");
                    Object ok = init.invoke(hook);
                    if (ok instanceof Boolean && (Boolean) ok) {
                        this.protocolHook = hook;
                        log.info("[UPR] ProtocolLib detected. Packet hook enabled.");
                    } else {
                        log.warning("[UPR] ProtocolLib present but hook did not enable.");
                    }
                } catch (Throwable t) {
                    log.warning("[UPR] ProtocolLib present but hook init failed: " + t.getMessage());
                }
            } else {
                log.info("[UPR] ProtocolLib not found; falling back to chat-event detector only.");
            }
        } catch (Throwable t) {
            log.warning("[UPR] ProtocolLib check failed: " + t.getMessage());
        }

        log.info("[UPR] UltimatePixelmonRating enabled (withPLIB-ready).");
    
}