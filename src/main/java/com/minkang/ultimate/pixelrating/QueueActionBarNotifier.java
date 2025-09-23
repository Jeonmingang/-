package com.minkang.ultimate.pixelrating;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class QueueActionBarNotifier {

    private final UltimatePixelmonRatingPlugin plugin;
    private final QueueManager queue;

    public QueueActionBarNotifier(UltimatePixelmonRatingPlugin plugin, QueueManager queue) {
        this.plugin = plugin;
        this.queue = queue;
        int period = Math.max(20, plugin.getConfig().getInt("ui.queue-actionbar.period-ticks", 40));
        if (plugin.getConfig().getBoolean("ui.queue-actionbar.enable", true)) {
            new BukkitRunnable() {
                @Override public void run() { tick(); }
            }.runTaskTimer(plugin, period, period);
        }
    }

    private void tick() {
        java.util.List<java.util.UUID> ids = queue.snapshot();
        int n = ids.size();
        String msg = Util.color("&6레이팅 대기열 &7: &a" + n + "명 대기 중");
        for (java.util.UUID id : ids) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
        }
    }
}
