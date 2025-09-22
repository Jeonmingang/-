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
    private QueueActionBarNotifier watchdog;

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
    public QueueActionBarNotifier watchdog() { return watchdog; }

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

        // Minimal, compile-safe wiring. Fill in real constructors later as needed.
        this.sessions = new MatchSessionManager(this);
        this.queue = new QueueManager(this);
        // ratings/storage/tiers/season/rewards/arenas can be wired later when exact constructors are known
        // detector depends on ratings; you can wire it after ratings is ready, e.g.:
        // this.detector = new BattleResultDetector(this, this.ratings);

        // NOTE: QueueActionBarNotifier has a private/no-access constructor in your tree,
        // so we skip instantiation here to keep compilation green.
        this.watchdog = null;

        // Listeners
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ChatParseListener(this), this);

        // ProtocolLib (optional)
        Plugin proto = pm.getPlugin("ProtocolLib");
        if (proto != null && proto.isEnabled()) {
            try {
                protocolHook = new InnerProtocolHook(this);
                ((InnerProtocolHook) protocolHook).enable();
                log.info("[UPR] ProtocolLib detected. Hook enabled.");
            } catch (Throwable t) {
                log.warning("[UPR] ProtocolLib hook init failed: " + t.getMessage());
                protocolHook = null;
            }
        } else {
            log.info("[UPR] ProtocolLib not present. Continuing without hook.");
        }

        
        // Commands
        try {
            RatingCommand rc = new RatingCommand(this, ratings, tiers, season, new RewardGUI(this), arenas);
            if (getCommand("레이팅") != null) {
                getCommand("레이팅").setExecutor(rc);
                getCommand("레이팅").setTabCompleter(rc);
            } else {
                log.warning("[UPR] '레이팅' command not defined in plugin.yml");
            }
        } catch (Throwable t) {
            log.warning("[UPR] Command wiring failed: " + t.getMessage());
        }

        log.info("[UPR] UltimatePixelmonRating enabled (compile-safe).");
    }

    @Override
    public void onDisable() {
        final Logger log = getLogger();
        safeStop(watchdog);
        safeStop(protocolHook);
        HandlerList.unregisterAll(this);
        log.info("[UPR] UltimatePixelmonRating disabled.");
    }

    /** cancel()/shutdown()/stop()/disable() if present */
    private static void safeStop(Object obj) {
        if (obj == null) return;
        for (String m : new String[]{"cancel", "shutdown", "stop", "disable"}) {
            try {
                Method method = obj.getClass().getMethod(m);
                method.setAccessible(true);
                method.invoke(obj);
                return;
            } catch (NoSuchMethodException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    /** ProtocolLib hook stub (no hard dependency needed) */
    private static final class InnerProtocolHook {
        private final JavaPlugin plugin;
        InnerProtocolHook(JavaPlugin plugin) { this.plugin = plugin; }
        void enable()  { plugin.getLogger().info("[UPR] (stub) Protocol hook enabled."); }
        void disable() { plugin.getLogger().info("[UPR] (stub) Protocol hook disabled."); }
        void stop()    { disable(); }
        void shutdown(){ disable(); }
    }
}