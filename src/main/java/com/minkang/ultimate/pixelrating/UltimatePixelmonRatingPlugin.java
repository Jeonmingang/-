package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class UltimatePixelmonRatingPlugin extends JavaPlugin {

    // --- 매니저 & 컴포넌트 필드 (널로 시작해도 컴파일 OK, 이후 실제 초기화 연결 가능) ---
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

    // ProtocolLib 훅 스텁 핸들
    private Object protocolHook;

    // --- 게터: 다른 클래스에서 plugin.xxx()로 호출함 ---
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

    // QueueActionBarNotifier가 부르는 헬퍼 (임시 구현)
    public int getQueueSize() {
        // queue 매니저 연결 후에는 실제 값 반환하도록 바꾸세요.
        // 예: return queue != null ? queue.size() : 0;
        return 0;
    }

    public int getActiveArenaCount() {
        // arenas 매니저 연결 후에는 실제 값 반환하도록 바꾸세요.
        // 예: return arenas != null ? arenas.activeCount() : 0;
        return 0;
    }

    @Override
    public void onEnable() {
        final Logger log = getLogger();

        // 기본 설정 배치
        saveDefaultConfig();

        // --- 컴파일 안정화용 최소 등록만 수행 ---
        // 세부 매니저들은 프로젝트 내 실제 생성자 시그니처에 맞춰
        // 다음 단계에서 연결합니다. (지금은 빌드만 깨지지 않게)
        this.sessions = new MatchSessionManager(this);
        // ratings/storage/tiers/season/rewards/arenas/queue는 후속 단계에서 실제 생성/주입
        // detector는 ratings 연결 후 생성하는 편이 안전하지만, 컴파일만 보장하려면 null로 둡니다.
        this.detector = null;

        // 액션바 노티파이어: 무인자 생성자만 존재하므로 이렇게 생성
        this.watchdog = new QueueActionBarNotifier();

        // 리스너 등록 (ChatParseListener는 plugin 한 개 인자)
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ChatParseListener(this), this);

        // ProtocolLib 감지 & 스텁 훅
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

        log.info("[UPR] UltimatePixelmonRating enabled (compile-safe).");
    }

    @Override
    public void onDisable() {
        final Logger log = getLogger();

        // 실행 중인 작업 안전 정지
        safeStop(watchdog);
        safeStop(protocolHook);

        HandlerList.unregisterAll(this);
        log.info("[UPR] UltimatePixelmonRating disabled.");
    }

    /** cancel()/shutdown()/stop()/disable() 중 있으면 호출 */
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

    /** ProtocolLib 훅 스텁 (의존성 없어도 안전) */
    private static final class InnerProtocolHook {
        private final JavaPlugin plugin;
        InnerProtocolHook(JavaPlugin plugin) { this.plugin = plugin; }
        void enable()  { plugin.getLogger().info("[UPR] (stub) Protocol hook enabled."); }
        void disable() { plugin.getLogger().info("[UPR] (stub) Protocol hook disabled."); }
        void stop()    { disable(); }
        void shutdown(){ disable(); }
    }
}
