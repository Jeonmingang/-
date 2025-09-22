package com.minkang.ultimate.pixelrating;

import org.bukkit.ChatColor;

public class Util {
    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Very lenient helper to turn a JSON chat component into plain text if such JSON sneaks into logs.
     * It is NOT a full JSON parser; it's only used for display/tooling.
     */
    public static String jsonToPlain(String json){
        if (json == null) return "";
        String t = json;

        // remove section sign color codes (e.g. §a)
        t = t.replaceAll("\\u00A7.", "");

        // common escapes
        t = t.replace("\\n", " ");

        // unescape simple quotes
        t = t.replace("\\"", """);

        // extract the "text":"..."" from very simple JSON chat components if present
        t = t.replaceAll("^\\s*\{.*?\"text\"\\s*:\\s*\"(.*?)\".*\}\\s*$", "$1");

        return t;
    }
}
