
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatParseListener implements Listener {
    private final UltimatePixelmonRatingPlugin plugin;
    private final RatingManager ratings;
    private Pattern[] victory; private Pattern[] draw;
    public ChatParseListener(UltimatePixelmonRatingPlugin plugin, RatingManager ratings){ this.plugin=plugin; this.ratings=ratings; reload(); }
    public void reload(){
        List<String> v = plugin.getConfig().getStringList("auto-result-detection.victory-message-patterns");
        victory = new Pattern[v.size()]; for (int i=0;i<v.size();i++) victory[i]=Pattern.compile(v.get(i));
        List<String> d = plugin.getConfig().getStringList("auto-result-detection.draw-message-patterns");
        draw = new Pattern[d.size()]; for (int i=0;i<d.size();i++) draw[i]=Pattern.compile(d.get(i));
    }
    @EventHandler(priority= EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e){
        String msg = e.getMessage();
        for (Pattern p : victory) {
            Matcher m = p.matcher(msg);
            if (m.find()) {
                String w = safe(m,"winner"); String l = safe(m,"loser");
                if (w!=null && l!=null) Bukkit.getScheduler().runTask(plugin, () -> ratings.recordResultNames(w,l,false));
                return;
            }
        }
        for (Pattern p : draw) {
            Matcher m = p.matcher(msg);
            if (m.find()) {
                String p1 = safe(m,"p1"); String p2 = safe(m,"p2");
                if (p1!=null && p2!=null) Bukkit.getScheduler().runTask(plugin, () -> ratings.recordResultNames(p1,p2,true));
                return;
            }
        }
    }
    private String safe(Matcher m,String name){ try { return m.group(name);} catch (IllegalArgumentException ex){ return null; } }
}
