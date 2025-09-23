package com.minkang.ultimate.pixelrating;
import org.bukkit.event.EventHandler; import org.bukkit.event.Listener; import org.bukkit.event.player.AsyncPlayerChatEvent; import java.util.regex.*;
public class BattleResultDetector implements Listener {
  private final UltimatePixelmonRatingPlugin plugin; private final RatingManager ratings; private Pattern win, draw;
  public BattleResultDetector(UltimatePixelmonRatingPlugin plugin, RatingManager ratings){ this.plugin=plugin; this.ratings=ratings;
    String w=plugin.getConfig().getStringList("auto-result-detection.victory-message-patterns").get(0);
    String d=plugin.getConfig().getStringList("auto-result-detection.draw-message-patterns").get(0);
    win=Pattern.compile(w); draw=Pattern.compile(d); plugin.getServer().getPluginManager().registerEvents(this, plugin); }
  @EventHandler public void onChat(AsyncPlayerChatEvent e){
    if(!plugin.getConfig().getBoolean("auto-result-detection.enable-chat-parse",false)) return;
    String msg=e.getMessage(); Matcher m=win.matcher(msg); if(m.find()){ String winner=m.group("winner"), loser=m.group("loser"); org.bukkit.OfflinePlayer w=plugin.getServer().getOfflinePlayer(winner); org.bukkit.OfflinePlayer l=plugin.getServer().getOfflinePlayer(loser); if(w!=null && l!=null) { plugin.getLogger().info("[UPR] Detected win: "+winner+" vs "+loser); ratings.applyMatch(w.getUniqueId(), l.getUniqueId(), false);} return; }
    m=draw.matcher(msg); if(m.find()){ String p1=m.group("p1"), p2=m.group("p2"); org.bukkit.OfflinePlayer a=plugin.getServer().getOfflinePlayer(p1); org.bukkit.OfflinePlayer b=plugin.getServer().getOfflinePlayer(p2); if(a!=null && b!=null) { plugin.getLogger().info("[UPR] Detected draw: "+p1+" vs "+p2); ratings.applyMatch(a.getUniqueId(), b.getUniqueId(), true);} }
  }
}