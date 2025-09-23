package com.minkang.ultimate.pixelrating;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
public class ProtocolHook {
  public static void safeInit(final UltimatePixelmonRatingPlugin plugin){
    try{
      if(!plugin.getConfig().getBoolean("auto-result-detection.protocol-chat-hook", true)) return;
      if(Bukkit.getPluginManager().getPlugin("ProtocolLib")==null) return;
      final ProtocolManager pm = ProtocolLibrary.getProtocolManager();
      pm.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT){
        @Override public void onPacketSending(PacketEvent event){
          try{
            if(event.getPacketType()!=PacketType.Play.Server.CHAT) return;
            Player receiver = event.getPlayer();
            String txt=null;
            try { txt = event.getPacket().getChatComponents().readSafely(0) != null ? event.getPacket().getChatComponents().read(0).getJson() : null; } catch(Throwable ignored){}
            if(txt==null){
              try { txt = event.getPacket().getStrings().readSafely(0); } catch(Throwable ignored){}
            }
            if(txt==null) return;
            String plain = txt.replaceAll("§.","");
            final String winMsg = plugin.getConfig().getString("auto-result-detection.korean.victory","배틀에서 이겼다!");
            final String loseMsg = plugin.getConfig().getString("auto-result-detection.korean.defeat","모든포켓몬이 쓰러졌다!");
            if(plain.contains(winMsg)){
              java.util.UUID opp = plugin.sessions().opponentOf(receiver.getUniqueId());
              if(opp!=null){ plugin.ratings().applyMatch(receiver.getUniqueId(), opp, false); plugin.sessions().clear(receiver.getUniqueId()); plugin.getLogger().info("[UPR] PacketHook WIN: "+receiver.getName()); }
            } else if(plain.contains(loseMsg)){
              java.util.UUID opp = plugin.sessions().opponentOf(receiver.getUniqueId());
              if(opp!=null){ plugin.ratings().applyMatch(opp, receiver.getUniqueId(), false); plugin.sessions().clear(receiver.getUniqueId()); plugin.getLogger().info("[UPR] PacketHook LOSE: "+receiver.getName()); }
            }
          }catch(Throwable ignored){}
        }
      });
      plugin.getLogger().info("[UPR] ProtocolLib chat hook enabled.");
    }catch(Throwable t){
      plugin.getLogger().warning("[UPR] ProtocolLib detected but hook failed: "+t.getClass().getSimpleName());
    }
  }
}