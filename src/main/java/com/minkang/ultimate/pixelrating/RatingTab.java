package com.minkang.ultimate.pixelrating;
import org.bukkit.command.*; import java.util.*; import java.util.stream.Collectors;
public class RatingTab implements TabCompleter {
  private final UltimatePixelmonRatingPlugin plugin; private final ArenaManager arenas;
  public RatingTab(UltimatePixelmonRatingPlugin plugin, ArenaManager arenas){ this.plugin=plugin; this.arenas=arenas; }
  @Override public java.util.List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
    if(args.length==1) return pref(java.util.Arrays.asList("도움말","참가","취소","내정보","전적","랭킹","보상설정","시즌설정","밴포켓몬","밴아이템","경기장"), args[0]);
    if(args.length>=1 && "경기장".equals(args[0])){
      if(args.length==2) return pref(java.util.Arrays.asList("생성","위치설정","활성화","목록"), args[1]);
      if(args.length==3 && !("목록".equals(args[1]) || "생성".equals(args[1]))) return arenas.list().stream().map(Arena::getId).collect(Collectors.toList());
      if(args.length==4 && "위치설정".equals(args[1])) return pref(java.util.Arrays.asList("p1","p2"), args[3]);
      if(args.length==4 && "활성화".equals(args[1])) return pref(java.util.Arrays.asList("true","false"), args[3]);
    } return null;
  }
  private java.util.List<String> pref(java.util.List<String> list, String p){ String low=p.toLowerCase(java.util.Locale.ROOT); java.util.List<String> out=new java.util.ArrayList<>(); for(String s:list) if(s.toLowerCase(java.util.Locale.ROOT).startsWith(low)) out.add(s); return out; }
}