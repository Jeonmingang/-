package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit; import org.bukkit.event.EventHandler; import org.bukkit.event.EventPriority; import org.bukkit.event.Listener; import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.List; import java.util.regex.Matcher; import java.util.regex.Pattern;
public class ChatParseListener implements Listener {
    private final UltimatePixelmonRatingPlugin plugin; private final RatingManager ratingManager; private Pattern[] victory, draw;
    public ChatParseListener(UltimatePixelmonRatingPlugin plugin, RatingManager ratingManager){ this.plugin=plugin; this.ratingManager=ratingManager; reload(); }
    public void reload(){ List<String> v=plugin.getConfig().getStringList("auto-result-detection.victory-message-patterns"); victory=new Pattern[v.size()]; for(int i=0;i<v.size();i++) victory[i]=Pattern.compile(v.get(i));
        List<String> d=plugin.getConfig().getStringList("auto-result-detection.draw-message-patterns"); draw=new Pattern[d.size()]; for(int i=0;i<d.size();i++) draw[i]=Pattern.compile(d.get(i)); }
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true) public void onChat(AsyncPlayerChatEvent e){
        String msg=e.getMessage();
        for(Pattern p: victory){ Matcher m=p.matcher(msg); if(m.find()){ String w=s(m,"winner"), l=s(m,"loser"); if (w==null||l==null) continue; Bukkit.getScheduler().runTask(plugin, ()-> ratingManager.recordResultNames(w,l,false)); return; } }
        for(Pattern p: draw){ Matcher m=p.matcher(msg); if(m.find()){ String a=s(m,"p1"), b=s(m,"p2"); if (a==null||b==null) continue; Bukkit.getScheduler().runTask(plugin, ()-> ratingManager.recordResultNames(a,b,true)); return; } }
    }
    private String s(Matcher m,String name){ try{ return m.group(name);}catch(IllegalArgumentException ex){ return null; } }
}