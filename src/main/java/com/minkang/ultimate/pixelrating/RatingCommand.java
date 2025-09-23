package com.minkang.ultimate.pixelrating;
import org.bukkit.Bukkit; import org.bukkit.ChatColor; import org.bukkit.Location; import org.bukkit.command.*; import org.bukkit.entity.Player;
public class RatingCommand implements CommandExecutor {
  private final UltimatePixelmonRatingPlugin plugin; private final RatingManager ratings; private final TierManager tiers; private final SeasonManager season; private final RewardGUI rewardGui; private final BanEnforcer bans; private final ArenaManager arenas;
  public RatingCommand(UltimatePixelmonRatingPlugin plugin, RatingManager ratings, TierManager tiers, SeasonManager season, RewardGUI rewardGui, BanEnforcer bans, ArenaManager arenas){ this.plugin=plugin; this.ratings=ratings; this.tiers=tiers; this.season=season; this.rewardGui=rewardGui; this.bans=bans; this.arenas=arenas; }
  private String pfx(){ return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ui.prefix","")); } private String c(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
  @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    try{
      if(args.length==0 || "도움말".equals(args[0])){ help(sender); return true; }
      switch(args[0]){
        case "참가": { if(!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용가능"); return true; } Player p=(Player)sender; plugin.queue().enqueue(p); p.sendMessage(pfx()+c("&a대기열에 참가했습니다. &7현재 인원: &e"+plugin.getQueueSize())); return true; }
        case "취소":
        case "나가기": { if(!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용가능"); return true; } Player p=(Player)sender; plugin.queue().dequeue(p); p.sendMessage(pfx()+c("&c대기열에서 나갔습니다.")); return true; }
        case "내정보":
        case "전적": { if(!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용가능"); return true; } Player p=(Player)sender; int elo=ratings.getElo(p); String tier=tiers.findTierName(elo); sender.sendMessage(pfx()+c("&fElo: &e"+elo+"&7, 티어: &r"+tier)); return true; }
        case "랭킹": { sender.sendMessage(pfx()+c("&6[랭킹] &7(초기화까지 &e"+ season.remainingString()+"&7)")); int i=1; for(java.util.Map.Entry<java.util.UUID,Integer> e: ratings.top(10)){ org.bukkit.OfflinePlayer op=Bukkit.getOfflinePlayer(e.getKey()); sender.sendMessage(c("&e"+(i++)+". &f"+(op!=null?op.getName():"?")+" &7- Elo "+e.getValue())); } return true; }
        case "보상설정": { if(!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용가능"); return true; } if(!sender.hasPermission("upr.admin")){ sender.sendMessage(c("&c권한이 없습니다.")); return true; } rewardGui.open((Player)sender); return true; }
        case "시즌설정": { if(!sender.hasPermission("upr.admin")){ sender.sendMessage(c("&c권한이 없습니다.")); return true; }
          if(args.length==5){ int mo=Integer.parseInt(args[1]); int d=Integer.parseInt(args[2]); int h=Integer.parseInt(args[3]); int mi=Integer.parseInt(args[4]); season.setSeasonByComponents(mo,d,h,mi); sender.sendMessage(pfx()+c("&a시즌 종료 시간이 설정되었습니다: &e"+season.remainingString())); }
          else if(args.length==2){ season.setSeasonByString(args[1]); sender.sendMessage(pfx()+c("&a시즌 종료 시간이 설정되었습니다: &e"+season.remainingString())); }
          else sender.sendMessage(c("&c사용법: /레이팅 시즌설정 <달> <일> <시> <분>  또는  /레이팅 시즌설정 yyyy-MM-dd_HH:mm:ss"));
          return true; }
        case "밴포켓몬": { if(!sender.hasPermission("upr.admin")){ sender.sendMessage(c("&c권한이 없습니다.")); return true; }
          if(args.length>=3){ String sub=args[1]; String name=args[2]; java.util.List<String> list=plugin.getConfig().getStringList("bans.pokemon");
            if("추가".equals(sub)){ list.add(name); plugin.getConfig().set("bans.pokemon", list); plugin.saveConfig(); bans.reload(); sender.sendMessage(pfx()+c("&a추가됨: &e"+name)); }
            else if("삭제".equals(sub)){ list.removeIf(s->s.equalsIgnoreCase(name)); plugin.getConfig().set("bans.pokemon", list); plugin.saveConfig(); bans.reload(); sender.sendMessage(pfx()+c("&c삭제됨: &e"+name)); }
            else sender.sendMessage(c("&c사용법: /레이팅 밴포켓몬 <추가|삭제> <이름>"));
          } else sender.sendMessage(c("&c사용법: /레이팅 밴포켓몬 <추가|삭제> <이름>")); return true; }
        case "밴아이템": { if(!sender.hasPermission("upr.admin")){ sender.sendMessage(c("&c권한이 없습니다.")); return true; }
          if(args.length>=3){ String sub=args[1]; String mat=args[2].toUpperCase(); java.util.List<String> list=plugin.getConfig().getStringList("bans.items");
            if("추가".equals(sub)){ if(!list.contains(mat)) list.add(mat); plugin.getConfig().set("bans.items", list); plugin.saveConfig(); bans.reload(); sender.sendMessage(pfx()+c("&a추가됨: &e"+mat)); }
            else if("삭제".equals(sub)){ list.removeIf(s->s.equalsIgnoreCase(mat)); plugin.getConfig().set("bans.items", list); plugin.saveConfig(); bans.reload(); sender.sendMessage(pfx()+c("&c삭제됨: &e"+mat)); }
            else sender.sendMessage(c("&c사용법: /레이팅 밴아이템 <추가|삭제> <재질>"));
          } else sender.sendMessage(c("&c사용법: /레이팅 밴아이템 <추가|삭제> <재질>")); return true; }
        case "경기장": { if(!sender.hasPermission("upr.admin")){ sender.sendMessage(c("&c권한이 없습니다.")); return true; } handleArena(sender,args); return true; }
      }
      sender.sendMessage(c("&c알 수 없는 하위명령. /레이팅 도움말")); return true;
    }catch(Exception ex){ sender.sendMessage(c("&c오류: ")+ex.getMessage()); return true; }
  }
  private void handleArena(CommandSender sender, String[] args){
    if(args.length<2){ sender.sendMessage(c("&c사용법: /레이팅 경기장 <생성|위치설정|활성화|목록> ...")); return; }
    switch(args[1]){
      case "생성": { if(args.length<3){ sender.sendMessage(c("&c사용법: /레이팅 경기장 생성 <id>")); return; } arenas.create(args[2]); sender.sendMessage(pfx()+c("&a경기장 생성: &f")+args[2]); return; }
      case "위치설정": { if(!(sender instanceof Player)){ sender.sendMessage(c("&c플레이어만 사용가능")); return; } if(args.length<4){ sender.sendMessage(c("&c사용법: /레이팅 경기장 위치설정 <id> <p1|p2>")); return; }
        Player p=(Player)sender; Location loc=p.getLocation(); arenas.set(args[2], args[3], loc); sender.sendMessage(pfx()+c("&a위치 설정 완료.")); return; }
      case "활성화": { if(args.length<4){ sender.sendMessage(c("&c사용법: /레이팅 경기장 활성화 <id> <true|false>")); return; } arenas.setEnabled(args[2], Boolean.parseBoolean(args[3])); sender.sendMessage(pfx()+c("&a활성화 변경됨.")); return; }
      case "목록": { sender.sendMessage(pfx()+c("&b[경기장 목록]")); for(Arena a: arenas.list()){ sender.sendMessage(c("&7- &f"+a.getId()+" &7enabled=&a"+a.isEnabled()+" &7p1="+(a.getP1()!=null)+" p2="+(a.getP2()!=null))); } return; }
      default: sender.sendMessage(c("&c사용법: /레이팅 경기장 <생성|위치설정|활성화|목록>"));
    }
  }
  private void help(CommandSender s){
    s.sendMessage(pfx()+c("&e/레이팅 참가 &7- 대기열 참가"));
    s.sendMessage(pfx()+c("&e/레이팅 취소 &7- 대기열 탈퇴"));
    s.sendMessage(pfx()+c("&e/레이팅 내정보 &7- Elo/티어 보기 (=/전적)"));
    s.sendMessage(pfx()+c("&e/레이팅 랭킹 &7- TOP10 (초기화까지 남은시간 표시)"));
    if(s.hasPermission("upr.admin")){
      s.sendMessage(pfx()+c("&e/레이팅 보상설정 &7- 티어 보상 GUI"));
      s.sendMessage(pfx()+c("&e/레이팅 시즌설정 <달> <일> <시> <분> &7또는 yyyy-MM-dd_HH:mm:ss"));
      s.sendMessage(pfx()+c("&e/레이팅 밴포켓몬 <추가|삭제> <이름>"));
      s.sendMessage(pfx()+c("&e/레이팅 밴아이템 <추가|삭제> <재질>"));
      s.sendMessage(pfx()+c("&e/레이팅 경기장 <생성|위치설정|활성화|목록> ..."));
    }
  }
}