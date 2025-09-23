package com.minkang.ultimate.pixelrating;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatParseListener implements Listener {
    private final UltimatePixelmonRatingPlugin plugin;
    public ChatParseListener(UltimatePixelmonRatingPlugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        // As a fallback, analyze messages (rarely used since Pixelmon uses system messages)
        plugin.detector().handleOutgoingMessage(e.getPlayer(), e.getMessage());
    }
}