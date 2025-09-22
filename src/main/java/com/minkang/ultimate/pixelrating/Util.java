package com.minkang.ultimate.pixelrating;

import org.bukkit.ChatColor;

public final class Util {
    private Util() {}

    /** Translate & color codes to Bukkit § colors. */
    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /** Very small helper to make a JSON-ish chat string readable (best-effort). */
    public static String jsonToPlain(String json) {
        if (json == null) return "";
        String t = json;
        // Remove Minecraft color section sign sequences (§x)
        t = t.replace("\u00A7", "");
        // Collapse new lines/backslash-n sequences
        t = t.replace("\\n", " ").replace("\n", " ");
        // Remove double quotes to avoid showing raw quotes
        t = t.replace("\"", "");
        return t.trim();
    }

    /** Format remaining time like 1d 2h 3m 4s (no zeros). */
    public static String timeLeft(long msLeft) {
        if (msLeft <= 0) return "0s";
        long seconds = msLeft / 1000L;
        long days = seconds / 86400L; seconds %= 86400L;
        long hours = seconds / 3600L; seconds %= 3600L;
        long minutes = seconds / 60L; seconds %= 60L;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
