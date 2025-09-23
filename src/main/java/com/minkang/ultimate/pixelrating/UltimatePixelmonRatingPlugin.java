package com.minkang.ultimate.pixelrating;
import org.bukkit.Bukkit; import org.bukkit.command.PluginCommand; import org.bukkit.plugin.java.JavaPlugin; import java.util.logging.Level;
public class UltimatePixelmonRatingPlugin extends JavaPlugin {
  private static UltimatePixelmonRatingPlugin instance;
  private RatingManager ratingManager; private MatchmakingService matchmakingService; private TierManager tierManager; private SeasonManager seasonManager; private RewardStore rewardStore; private RewardGUI rewardGUI; private BanEnforcer banEnforcer; private ArenaManager arenaManager; private SessionManager sessionManager; private BattleResultDetector battleResultDetector; private EloStore eloStore;
  public static UltimatePixelmonRatingPlugin get(){ return instance; }
  @Override public void onEnable(){
    instance=this; saveDefaultConfig(); saveResource("rewards.yml", false); if(!new java.io.File(getDataFolder(),"arenas.yml").exists()) saveResource("arenas.yml", false);
    tierManager = new TierManager(this);
    rewardStore = new RewardStore(this); rewardStore.load();
    arenaManager = new ArenaManager(this); arenaManager.load();
    eloStore = new EloStore(this); eloStore.load();
    ratingManager = new RatingManager(this, tierManager, eloStore);
    seasonManager = new SeasonManager(this);
    banEnforcer = new BanEnforcer(this);
    sessionManager = new SessionManager(this);
    matchmakingService = new MatchmakingService(this, ratingManager, tierManager, banEnforcer, arenaManager); matchmakingService.start();
    rewardGUI = new RewardGUI(this, rewardStore); Bukkit.getPluginManager().registerEvents(rewardGUI, this);
    try { battleResultDetector = new BattleResultDetector(this, ratingManager); }
    catch (Throwable t){ getLogger().log(Level.INFO, "[UPR] BattleResultDetector optional: " + t.getClass().getSimpleName()); }
    try { ProtocolHook.safeInit(this); } catch(Throwable t){ getLogger().log(Level.WARNING, "[UPR] ProtocolLib hook failed: " + t.getClass().getName()); }
    RatingCommand cmd = new RatingCommand(this, ratingManager, tierManager, seasonManager, rewardGUI, banEnforcer, arenaManager);
    PluginCommand c = getCommand("레이팅"); if (c!=null){ c.setExecutor(cmd); c.setTabCompleter(new RatingTab(this, arenaManager)); }
    seasonManager.startTicker();
    getLogger().info("[UPR] UltimatePixelmonRating enabled (v1.6.12).");
  }
  @Override public void onDisable(){ if (matchmakingService!=null) matchmakingService.stop(); if (seasonManager!=null) seasonManager.stopTicker(); if (arenaManager!=null) arenaManager.save(); if (rewardStore!=null) rewardStore.save(); getLogger().info("[UPR] UltimatePixelmonRating disabled."); }
  public RatingManager ratings(){ return ratingManager; } public MatchmakingService queue(){ return matchmakingService; } public TierManager tiers(){ return tierManager; } public SeasonManager season(){ return seasonManager; } public RewardStore rewards(){ return rewardStore; } public ArenaManager arenas(){ return arenaManager; } public SessionManager sessions(){ return sessionManager; } public BattleResultDetector detector(){ return battleResultDetector; }
  public int getQueueSize(){ return matchmakingService!=null ? matchmakingService.queueSize() : 0; }
  public int getActiveArenaCount(){ if (arenaManager==null) return 0; int c=0; for (Arena a: arenaManager.list()) if (a.isEnabled() && a.getP1()!=null && a.getP2()!=null) c++; return c; }
}