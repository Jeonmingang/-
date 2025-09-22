package com.minkang.ultimate.pixelrating;

import org.bukkit.ChatColor;

public class Util {
    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Very lenient helper to turn a JSON chat component (sometimes logged) into plain text.
     * Avoids heavy regex/backslashes to stay compiler-safe on Java 8.
     */
    public static String jsonToPlain(String json) {
        if (json == null) return "";
        String t = json;

        // Remove Minecraft color codes using the section sign (§) if present.
        // Example: "§a", "§l" ... we strip the pair.
        StringBuilder sb = new StringBuilder(t.length());
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '§') {
                i++; // skip next char as well if any
                continue;
            }
            sb.append(c);
        }
        t = sb.toString();

        // Replace newlines with spaces for safety.
        t = t.replace("\n", " ").replace("
", " ").replace("", " ");

        // Unescape simple " into "
        t = t.replace("\"", """);

        // If it looks like a very simple JSON chat component, try to pull `"text":"..."`
        // We intentionally avoid regex with escape sequences here.
        String needle = ""text":"";
        int idx = t.indexOf(needle);
        if (idx >= 0) {
            int start = idx + needle.length();
            int end = t.indexOf('"', start);
            if (end > start) {
                t = t.substring(start, end);
            }
        }
        return t;
    }
}
