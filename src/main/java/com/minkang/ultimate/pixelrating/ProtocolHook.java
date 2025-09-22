package com.minkang.ultimate.pixelrating;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ProtocolHook {
    private final UltimatePixelmonRatingPlugin plugin;
    private PacketAdapter adapter;

    public ProtocolHook(UltimatePixelmonRatingPlugin plugin){ this.plugin = plugin; }

    public boolean init(){
        try {
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();
            adapter = new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT){
                @Override public void onPacketSending(PacketEvent event){
                    if (event.isCancelled()) return;
                    Player p = event.getPlayer();
                    try {
                        String json = event.getPacket().getChatComponents().read(0).getJson();
                        if (json == null) return;
                        String plain = Util.stripColor(Util.jsonToPlain(json)).trim();
                        if (plain.isEmpty()) return;
                        // Delegate to ChatParseListener-like logic
                        plugin.detector().handleOutgoingMessage(p, plain);
                    } catch (Throwable ignored){}
                }
            };
            pm.addPacketListener(adapter);
            plugin.getLogger().info("[UPR] ProtocolLib hook enabled.");
            return true;
        } catch (Throwable t){
            plugin.getLogger().warning("[UPR] ProtocolLib not present, chat-packet hook disabled.");
            return false;
        }
    }

    public void shutdown(){
        try {
            if (adapter != null) ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
        } catch (Throwable ignored){}
    }
}