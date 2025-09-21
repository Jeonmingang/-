package com.minkang.ultimate.pixelrating;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class QueueActionBarNotifier {
    private static final Map<UUID, BukkitTask> TASKS = new ConcurrentHashMap<>();
    private QueueActionBarNotifier(){}

    public static void start(UltimatePixelmonRatingPlugin plugin, Player p){
        stop(p);
        if (!plugin.getConfig().getBoolean("ui.queue-actionbar.enable", true)) return;
        TASKS.put(p.getUniqueId(), Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!p.isOnline()){ stop(p); return; }
            int q = plugin.queue().size();
            int arenas = plugin.arena().countEnabled();
            int eta = plugin.getConfig().getInt("ui.queue-actionbar.eta-seconds", 2);
            String fmt = plugin.getConfig().getString("ui.queue-actionbar.format",
                    "&e대기열&7: &f{queue}&7명  &e경기장&7: &f{arenas}&7개  &e예상&7: &f~{eta}s");
            String msg = fmt.replace("{queue}", String.valueOf(q))
                            .replace("{arenas}", String.valueOf(arenas))
                            .replace("{eta}", String.valueOf(eta));
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg)));
        }, 0L, 20L));
    }

    public static void stop(Player p){
        BukkitTask t = TASKS.remove(p.getUniqueId());
        if (t != null) t.cancel();
    }
}