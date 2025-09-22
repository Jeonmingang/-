package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BattleResultDetector {
    private final UltimatePixelmonRatingPlugin plugin;
    private final RatingManager ratings;

    private final Pattern[] winPatterns;
    private final Pattern[] drawPatterns;

    public BattleResultDetector(UltimatePixelmonRatingPlugin plugin, RatingManager ratings){
        this.plugin = plugin;
        this.ratings = ratings;
        this.winPatterns = new Pattern[]{
                // English
                Pattern.compile("(?<p1>\\w{2,16})\\s+(?:has\\s+)?defeated\\s+(?<p2>\\w{2,16})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?<p1>\\w{2,16}).*?won\\s+the\\s+battle", Pattern.CASE_INSENSITIVE),
                // Korean (very loose heuristics)
                Pattern.compile("(?<p1>\\S{2,16}).*?(?<p2>\\S{2,16}).*?이겼", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?<p1>\\S{2,16}).*?배틀에서\\s*승리", Pattern.CASE_INSENSITIVE)
        };
        this.drawPatterns = new Pattern[]{
                Pattern.compile("battle\\s+ended\\s+in\\s+a\\s+draw", Pattern.CASE_INSENSITIVE),
                Pattern.compile("무승부|비겼", Pattern.CASE_INSENSITIVE)
        };
    }

    /**
     * Try to interpret a raw chat/plain string and notify RatingManager about an outcome,
     * when both participants are currently tracked in an active session.
     */
    public void tryParseAndRecord(String line){
        if (line == null || line.isEmpty()) return;
        String plain = Util.jsonToPlain(line);

        // WIN
        for (Pattern p : winPatterns){
            Matcher m = p.matcher(plain);
            if (m.find()){
                String pa = safe(m, "p1");
                String pb = safe(m, "p2"); // may be null for the "won the battle" pattern
                if (pa != null){
                    if (pb == null){
                        // try to derive opponent from active sessions
                        for (MatchSessionManager.Session s : plugin.sessions().all()){
                            Player a = Bukkit.getPlayerExact(pa);
                            if (a != null){
                                if (a.getUniqueId().equals(s.a) || a.getUniqueId().equals(s.b)){
                                    Player opp = a.getUniqueId().equals(s.a) ? Bukkit.getPlayer(s.b) : Bukkit.getPlayer(s.a);
                                    if (opp != null) pb = opp.getName();
                                    break;
                                }
                            }
                        }
                    }
                    if (pb != null){
                        final String A = pa, B = pb;
                        Bukkit.getScheduler().runTask(plugin, () -> ratings.recordResultNames(A, B, false));
                        return;
                    }
                }
            }
        }

        // DRAW
        for (Pattern p : drawPatterns){
            Matcher m = p.matcher(plain);
            if (m.find()){
                for (MatchSessionManager.Session s : plugin.sessions().all()){
                    Player a = Bukkit.getPlayer(s.a);
                    Player b = Bukkit.getPlayer(s.b);
                    if (a != null && b != null){
                        final String A = a.getName(), B = b.getName();
                        Bukkit.getScheduler().runTask(plugin, () -> ratings.recordResultNames(A, B, true));
                    }
                }
                return;
            }
        }
    }

    private static String safe(Matcher m, String g){
        try { return m.group(g); } catch (Throwable ignored){ return null; }
    }
}
