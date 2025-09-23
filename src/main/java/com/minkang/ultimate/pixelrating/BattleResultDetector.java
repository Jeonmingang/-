package com.minkang.ultimate.pixelrating;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Pattern;

public class BattleResultDetector implements Listener {

    private final UltimatePixelmonRatingPlugin plugin;
    private final MatchSessionManager sessions;
    private final RatingManager ratings;

    private Pattern[] win, lose, draw, forfeit, faintAll;
    private boolean packetHook;

    public BattleResultDetector(UltimatePixelmonRatingPlugin plugin, MatchSessionManager sessions, RatingManager ratings) {
        this.plugin = plugin;
        this.sessions = sessions;
        this.ratings = ratings;
    }

    public void enable() {
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        win = Util.compilePatterns(cfg.getStringList("detection.win-message-patterns"));
        lose = Util.compilePatterns(cfg.getStringList("detection.lose-message-patterns"));
        draw = Util.compilePatterns(cfg.getStringList("detection.draw-message-patterns"));
        forfeit = Util.compilePatterns(cfg.getStringList("detection.forfeit-message-patterns"));
        faintAll = Util.compilePatterns(cfg.getStringList("detection.faint-all-hint-patterns"));

        packetHook = cfg.getBoolean("detection.use-packet-hook", true) && Util.hasProtocolLib();
        if (packetHook) {
            try {
                ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {
                    @Override public void onPacketSending(PacketEvent event) {
                        if (!(event.getPlayer() instanceof Player)) return;
                        // Most Pixelmon battle logs are delivered as JSON chat components; use raw JSON text
                        String json = null;
                        try {
                            com.comphenix.protocol.wrappers.WrappedChatComponent comp = event.getPacket().getChatComponents().readSafely(0);
                            if (comp != null) json = comp.getJson();
                        } catch (Throwable ignored) {}
                        if (json == null) return;
                        // crude strip: remove keys, braces; keep Korean text tokens for regex
                        String plainish = json.replaceAll("\\\\[nrt\"/]", " ")
                                             .replaceAll("[{}\\[\\],:]", " ")
                                             .replaceAll("\\s+", " ")
                                             .trim();
                        handleLine((Player) event.getPlayer(), plainish);
                    }
                });
            } catch (Throwable t) {
                plugin.getLogger().warning("[UPR] ProtocolLib hook failed: " + t.getMessage());
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        handleLine(e.getPlayer(), e.getMessage());
    }

    private void handleLine(Player viewer, String line) {
        if (line == null) return;
        MatchSessionManager.Session s = sessions.get(viewer);
        if (s == null || s.ended) return;

        if (matches(line, draw)) {
            finishDraw(s);
            return;
        }
        if (matches(line, forfeit)) {
            finish(viewer, s, "FORFEIT");
            return;
        }
        if (matches(line, win)) {
            finish(viewer, s, "VICTORY");
            return;
        }
        if (matches(line, lose) || matches(line, faintAll)) {
            finish(viewer, s, "DEFEAT");
        }
    }

    private boolean matches(String line, Pattern[] arr) {
        for (Pattern p : arr) {
            if (p.matcher(line).find()) return true;
        }
        return false;
    }

    private void finishDraw(MatchSessionManager.Session s) {
        if (s.ended) return;
        s.ended = true;
        ratings.applyResult(s.p1, s.p2, "DRAW");
        sessions.restore(s);
        sessions.endFor(org.bukkit.Bukkit.getPlayer(s.p1));
    }

    private void finish(Player viewer, MatchSessionManager.Session s, String type) {
        if (s.ended) return;
        s.ended = true;
        java.util.UUID winner, loser;
        if ("VICTORY".equals(type)) {
            winner = viewer.getUniqueId();
            loser = viewer.getUniqueId().equals(s.p1) ? s.p2 : s.p1;
        } else { // DEFEAT / FORFEIT
            loser = viewer.getUniqueId();
            winner = viewer.getUniqueId().equals(s.p1) ? s.p2 : s.p1;
        }
        ratings.applyResult(winner, loser, type);
        sessions.restore(s);
        sessions.endFor(org.bukkit.Bukkit.getPlayer(s.p1));
    }
}
