package com.minkang.ultimate.pixelrating;

import org.bukkit.ChatColor;

public class Util {
    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Extremely lenient JSON-to-plain converter without regex or fragile escapes.
     * - Strips section color codes (e.g., §a, §l)
     * - Replaces newline characters with a space
     * - Unescapes simple \" into "
     * - If it looks like {"text":"..."} extracts the "text" value
     */
    public static String jsonToPlain(String json) {
        if (json == null) return "";
        String t = json;

        // 1) Strip Minecraft color codes using the section sign (§) and next char
        StringBuilder sb = new StringBuilder(t.length());
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '§') {
                i++; // skip next char as well
                continue;
            }
            sb.append(c);
        }
        t = sb.toString();

        // 2) Replace literal "\n" with a space, and real newlines as well
        //    Do it without regex and without special string escapes.
        StringBuilder sb2 = new StringBuilder(t.length());
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '\\') {
                if (i + 1 < t.length() && t.charAt(i + 1) == 'n') {
                    sb2.append(' ');
                    i++; // skip 'n'
                    continue;
                }
            }
            if (c == '\n' || c == '\r') {
                sb2.append(' ');
            } else {
                sb2.append(c);
            }
        }
        t = sb2.toString();

        // 3) Unescape simple \" into "
        StringBuilder sb3 = new StringBuilder(t.length());
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '\\') {
                if (i + 1 < t.length() && t.charAt(i + 1) == '\"') {
                    sb3.append('\"');
                    i++; // skip the quote
                    continue;
                }
            }
            sb3.append(c);
        }
        t = sb3.toString();

        // 4) Try to pull "text":"..."
        char dq = '\"';
        String needle = "" + dq + "text" + dq + ":" + dq;
        int idx = t.indexOf(needle);
        if (idx >= 0) {
            int start = idx + needle.length();
            int end = t.indexOf(dq, start);
            if (end > start) {
                t = t.substring(start, end);
            }
        }
        return t;
    }
}
