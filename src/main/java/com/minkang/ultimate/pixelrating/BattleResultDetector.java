package com.minkang.ultimate.pixelrating;
import org.bukkit.event.EventHandler; import org.bukkit.event.Listener; import org.bukkit.event.player.AsyncPlayerChatEvent; import org.bukkit.entity.Player; import java.util.regex.*;
public class BattleResultDetector implements Listener {
  private final UltimatePixelmonRatingPlugin plugin; private final RatingManager ratings; private Pattern win, draw;
  public BattleResultDetector(UltimatePixelmonRatingPlugin plugin, RatingManager ratings){ this.plugin=plugin; this.ratings=ratings;
    java.util.List<String> v=plugin.getConfig().getStringList("auto-result-detection.victory-message-patterns");
    java.util.List<String> d=plugin.getConfig().getStringList("auto-result-detection.draw-message-patterns");
    if(!v.isEmpty()) win=Pattern.compile(v.get(0));
    if(!d.isEmpty()) draw=Pattern.compile(d.get(0));
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }
  @EventHandler public void onChat(AsyncPlayerChatEvent e){
    if(!plugin.getConfig().getBoolean("auto-result-detection.enable-chat-parse",true)) return;
    String msg=e.getMessage(); Player sender=e.getPlayer();
    String winKo = plugin.getConfig().getString("auto-result-detection.korean.victory","배틀에서 이겼다!");
    String loseKo = plugin.getConfig().getString("auto-result-detection.korean.defeat","모든포켓몬이 쓰러졌다!");
    if(msg.contains(winKo)){ java.util.UUID opp=plugin.sessions().opponentOf(sender.getUniqueId()); if(opp!=null){ ratings.applyMatch(sender.getUniqueId(), opp, false); plugin.sessions().clear(sender.getUniqueId()); return; } }
    if(msg.contains(loseKo)){ java.util.UUID opp=plugin.sessions().opponentOf(sender.getUniqueId()); if(opp!=null){ ratings.applyMatch(opp, sender.getUniqueId(), false); plugin.sessions().clear(sender.getUniqueId()); return; } }
    if(win!=null){ Matcher m=win.matcher(msg); if(m.find()){ String winner=m.group("winner"), loser=m.group("loser"); if(winner!=null && loser!=null){ org.bukkit.OfflinePlayer w=plugin.getServer().getOfflinePlayer(winner); org.bukkit.OfflinePlayer l=plugin.getServer().getOfflinePlayer(loser); if(w!=null && l!=null){ ratings.applyMatch(w.getUniqueId(), l.getUniqueId(), false); return; } } } }
    if(draw!=null){ Matcher m=draw.matcher(msg); if(m.find()){ String p1=m.group("p1"), p2=m.group("p2"); if(p1!=null && p2!=null){ org.bukkit.OfflinePlayer a=plugin.getServer().getOfflinePlayer(p1); org.bukkit.OfflinePlayer b=plugin.getServer().getOfflinePlayer(p2); if(a!=null && b!=null){ ratings.applyMatch(a.getUniqueId(), b.getUniqueId(), true); } } } }
  }
}