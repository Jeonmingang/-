package com.minkang.ultimate.pixelrating;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class BattleGuardListener implements Listener {
    private final UltimatePixelmonRatingPlugin plugin;
    public BattleGuardListener(UltimatePixelmonRatingPlugin plugin){ this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCmd(PlayerCommandPreprocessEvent e){
        if (!plugin.getConfig().getBoolean("battle.block-commands", true)) return;
        Player p = e.getPlayer();
        if (!BattleState.isIn(p.getUniqueId())) return;
        if (p.hasPermission("upr.battle.bypass")) return;
        List<String> allow = plugin.getConfig().getStringList("battle.command-allowlist");
        String raw = e.getMessage().toLowerCase();
        for (String a : allow){ if (a!=null && !a.isEmpty() && raw.startsWith("/"+a.toLowerCase())) return; }
        e.setCancelled(true);
        String msg = plugin.getConfig().getString("battle.blocked-message","&c배틀 중에는 명령어를 사용할 수 없습니다.");
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        BattleState.clear(e.getPlayer().getUniqueId());
    }
}