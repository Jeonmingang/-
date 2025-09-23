package com.minkang.ultimate.pixelrating;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class BanEnforcer implements Listener {
    private final UltimatePixelmonRatingPlugin plugin;
    private final MatchSessionManager sessions;
    private final java.util.Set<String> blocked;

    public BanEnforcer(UltimatePixelmonRatingPlugin plugin, MatchSessionManager sessions) {
        this.plugin = plugin;
        this.sessions = sessions;
        java.util.List<String> list = plugin.getConfig().getStringList("bans.command-blacklist");
        java.util.Set<String> s = new java.util.HashSet<>();
        for (String x : list) s.add(x.toLowerCase(java.util.Locale.ROOT));
        this.blocked = java.util.Collections.unmodifiableSet(s);
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        MatchSessionManager.Session s = sessions.get(e.getPlayer());
        if (s == null) return;
        String msg = e.getMessage().trim().toLowerCase(java.util.Locale.ROOT);
        String base = msg.startsWith("/") ? msg.substring(1) : msg;
        String cmd = base.split("\\s+", 2)[0];
        if (blocked.contains(cmd)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Util.color("&c배틀 중 사용할 수 없는 명령입니다: &e/"+cmd));
        }
    }
}
