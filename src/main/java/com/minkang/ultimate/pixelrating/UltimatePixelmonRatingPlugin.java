package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class UltimatePixelmonRatingPlugin extends JavaPlugin {

    // ==== 추가된 필드 ====
    private RatingManager ratings;
    private MatchSessionManager sessions;
    private BattleResultDetector detector;
    private QueueActionBarNotifier watchdog;

    // ProtocolLib 훅이 없어도 컴파일/런타임 안전하게 하기 위한 스텁 핸들
    private Object protocolHook;

    // ==== 게터 (다른 클래스에서 plugin.ratings()/plugin.detector()/plugin.sessions() 등 호출) ====
    public RatingManager ratings() { return ratings; }
    public MatchSessionManager sessions() { return sessions; }
    public BattleResultDetector detector() { return detector; }
    public QueueActionBarNotifier watchdog() { return watchdog; }

    @Override
    public void onEnable() {
        final Logger log = getLogger();

        // 기본 설정 파일 생성
        saveDefaultConfig();

        // 매니저/디텍터 초기화 (순서 주의: ratings -> sessions -> detector -> watchdog)
        this.ratings  = new RatingManager(this);
        this.sessions = new MatchSessionManager(this);
        this.detector = new BattleResultDetector(this, this.ratings);
        this.watchdog = new QueueActionBarNotifier(this);

        // 리스너 등록: ChatParseListener는 plugin 한 개만 받도록 가정
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ChatParseListener(this), this);

        // (필요 시) 다른 리스너도 여기에 등록하세요
        // pm.registerEvents(new SomeOtherListener(this), this);

        // ProtocolLib 존재 시 훅 활성화(스텁). 실제 훅 클래스가 없어도 안전.
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

        log.info("[UPR] UltimatePixelmonRating enabled.");
    }

    @Override
    public void onDisable() {
        final Logger log = getLogger();

        // 액션바 알림기 등 실행 중인 작업을 최대한 안전하게 중지
        safeStop(watchdog);

        // 프로토콜 훅 중지
        safeStop(protocolHook);

        // 리스너 해제
        HandlerList.unregisterAll(this);

        log.info("[UPR] UltimatePixelmonRating disabled.");
    }

    /**
     * cancel()/shutdown()/stop() 중 존재하는 메서드를 찾아 안전하게 호출
     */
    private static void safeStop(Object obj) {
        if (obj == null) return;
        for (String m : new String[]{"cancel", "shutdown", "stop", "disable"}) {
            try {
                Method method = obj.getClass().getMethod(m);
                method.setAccessible(true);
                method.invoke(obj);
                return;
            } catch (NoSuchMethodException ignored) {
            } catch (Throwable t) {
                // 다음 후보 메서드 시도
            }
        }
    }

    /**
     * 외부 의존성 없이도 컴파일/런타임이 가능한 내부 훅 스텁.
     * 실제 ProtocolLib 연동이 필요한 경우, 별도의 클래스에서 구현해도 무방.
     */
    private static final class InnerProtocolHook {
        private final JavaPlugin plugin;
        InnerProtocolHook(JavaPlugin plugin) { this.plugin = plugin; }
        void enable()  { plugin.getLogger().info("[UPR] (stub) Protocol hook enabled."); }
        void disable() { plugin.getLogger().info("[UPR] (stub) Protocol hook disabled."); }
        // stop()/shutdown() 이름으로도 정리될 수 있도록 메서드 제공
        void stop()    { disable(); }
        void shutdown(){ disable(); }
    }
}
