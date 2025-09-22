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

        // English + Korean common messages (Pixelmon & translations)
        winPatterns = new Pattern[]{
            // "{p1} has defeated {p2}!"
            Pattern.compile("(?<p1>\\w{2,16})\\s+(?:has\\s+)?defeated\\s+(?<p2>\\w{2,16})", Pattern.CASE_INSENSITIVE),
            // "{p1} won the battle"
            Pattern.compile("(?<p1>\\w{2,16})\\s+won\\s+the\\s+battle", Pattern.CASE_INSENSITIVE),

            // Korean variants
            // "{p1}(님)이/가 {p2}(님)을 이겼..."
            Pattern.compile("(?<p1>\\S{2,16})\\s*(?:님)?\\s*(?:이|가)\\s*(?<p2>\\S{2,16})\\s*(?:님)?\\s*을?\\s*이겼", Pattern.CASE_INSENSITIVE),
            // "{p1}(님)이 배틀에서 승리..."
            Pattern.compile("(?<p1>\\S{2,16})\\s*(?:님)?\\s*이\\s*배틀에서\\s*승리", Pattern.CASE_INSENSITIVE),
        };

        drawPatterns = new Pattern[]{
            Pattern.compile("battle\\s+ended\\s+in\\s+a\\s+draw", Pattern.CASE_INSENSITIVE),
            Pattern.compile("무승부|비겼", Pattern.CASE_INSENSITIVE),
        };
    }

    public void handleOutgoingMessage(Player receiver, String plain){
        // Try win patterns
        for (Pattern p : winPatterns){
            Matcher m = p.matcher(plain);
            if (m.find()){
                String p1 = safe(m, "p1"), p2 = safe(m, "p2");

                // Resolve: if one is missing, try pairing with receiver vs last opponent in session
                if (p1 == null && receiver != null) p1 = receiver.getName();
                if (p2 == null && receiver != null){
                    MatchSessionManager.Session s = plugin.sessions().findByPlayer(receiver.getUniqueId());
                    if (s != null){
                        java.util.UUID opp = s.a.equals(receiver.getUniqueId()) ? s.b : s.a;
                        Player oppP = Bukkit.getPlayer(opp);
                        if (oppP != null) p2 = oppP.getName();
                    }
                }

                final String A = p1, B = p2;
                if (A != null && B != null){
                    Bukkit.getScheduler().runTask(plugin, () -> ratings.recordResultNames(A, B, false));
                }
                return;
            }
        }

        // Try draw patterns
        for (Pattern p : drawPatterns){
            Matcher m = p.matcher(plain);
            if (m.find() && receiver != null){
                MatchSessionManager.Session s = plugin.sessions().findByPlayer(receiver.getUniqueId());
                if (s != null){
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
