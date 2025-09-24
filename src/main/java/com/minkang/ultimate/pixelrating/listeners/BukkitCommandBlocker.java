package com.minkang.ultimate.pixelrating.listeners;

import com.minkang.ultimate.pixelrating.UltimatePixelmonRatingPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class BukkitCommandBlocker implements Listener {
    private final Set<String> blocked;

    public BukkitCommandBlocker(UltimatePixelmonRatingPlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        this.blocked = new HashSet<>(cfg.getStringList("blocked-commands"));
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String raw = e.getMessage();
        String head = (raw.startsWith("/") ? raw.substring(1) : raw).split(" ")[0].toLowerCase(Locale.ROOT);
        if (head.equals("레이팅") || head.equals("rating") || head.equals("elo")) return;
        if (BattleLock.isLocked(p.getUniqueId()) && blocked.contains(head)) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "배틀중에는 사용할 수 없는 명령어입니다: /" + head);
        }
    }
}
