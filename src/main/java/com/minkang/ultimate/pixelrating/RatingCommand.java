package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit; import org.bukkit.command.*; import org.bukkit.entity.Player; import java.time.LocalDate; import java.time.LocalDateTime; import java.util.*;

public class RatingCommand implements CommandExecutor, TabCompleter {
    private final UltimatePixelmonRatingPlugin plugin; private final RatingManager ratingManager; private final TierManager tierManager; private final SeasonManager seasonManager; private final RewardGUI rewardGUI; private final BanEnforcer banEnforcer; private final ChatParseListener chatListener;
    public RatingCommand(UltimatePixelmonRatingPlugin plugin, RatingManager ratingManager, TierManager tierManager, SeasonManager seasonManager, RewardGUI rewardGUI, BanEnforcer banEnforcer, ChatParseListener chatListener){
        this.plugin=plugin; this.ratingManager=ratingManager; this.tierManager=tierManager; this.seasonManager=seasonManager; this.rewardGUI=rewardGUI; this.banEnforcer=banEnforcer; this.chatListener=chatListener; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        String sub = args.length>0? args[0].toLowerCase() : "도움말";
        if (sub.equals("도움말")){
            sender.sendMessage(Util.color("&6/레이팅 참가, 취소, 상태, 전적 [닉], 랭킹 [페이지]"));
            sender.sendMessage(Util.color("&7관리자: 시즌설정 <초>, 시즌설정날짜 <월> <일> <시> <분> [초] [연], 시즌남은시간, 결과 <승자> <패자> [draw], 보상설정 <티어표시명>, elo설정 <닉> <elo>, 시즌즉시종료, reload"));
            return true;
        }
        if (sub.equals("참가")){
            if (!(sender instanceof Player)) { sender.sendMessage("Player only."); return true; }
            Player p=(Player)sender; boolean ok=plugin.queue().join(p);
            if (!ok){ p.sendMessage(Util.color(plugin.getConfig().getString("ui.already-in-queue"))); return true; }
            p.sendMessage(Util.color(plugin.getConfig().getString("ui.joined-queue"))); return true;
        }
        if (sub.equals("취소")){
            if (!(sender instanceof Player)) { sender.sendMessage("Player only."); return true; }
            Player p=(Player)sender; boolean ok=plugin.queue().leave(p);
            if (!ok){ p.sendMessage(Util.color(plugin.getConfig().getString("ui.not-in-queue"))); return true; }
            p.sendMessage(Util.color(plugin.getConfig().getString("ui.left-queue"))); return true;
        }
        if (sub.equals("상태")){ sender.sendMessage(Util.color("&7큐: &f"+String.join(", ", plugin.queue().names()))); return true; }
        if (sub.equals("전적")){
            if (args.length>=2){
                PlayerProfile pr=ratingManager.byName(args[1]); if(pr==null){ sender.sendMessage(Util.color("&c플레이어를 찾을 수 없습니다.")); return true; }
                TierManager.Tier t=tierManager.ofElo(pr.getElo());
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.stats-other")
                        .replace("{player}", pr.getLastKnownName())
                        .replace("{elo}", String.valueOf(pr.getElo()))
                        .replace("{tier}", Util.color(t.name))
                        .replace("{wins}", String.valueOf(pr.getWins()))
                        .replace("{losses}", String.valueOf(pr.getLosses()))
                        .replace("{draws}", String.valueOf(pr.getDraws()))));
            } else {
                if (!(sender instanceof Player)) { sender.sendMessage("사용법: /레이팅 전적 <닉>"); return true; }
                Player p=(Player)sender; PlayerProfile pr=ratingManager.getProfile(p.getUniqueId(), p.getName()); TierManager.Tier t=tierManager.ofElo(pr.getElo());
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.stats-self")
                        .replace("{elo}", String.valueOf(pr.getElo()))
                        .replace("{tier}", Util.color(t.name))
                        .replace("{wins}", String.valueOf(pr.getWins()))
                        .replace("{losses}", String.valueOf(pr.getLosses()))
                        .replace("{draws}", String.valueOf(pr.getDraws()))));
            }
            return true;
        }
        if (sub.equals("랭킹")){
            int page=1; if(args.length>=2){ try{ page=Integer.parseInt(args[1]); }catch(Exception ignored){} }
            int per=10; java.util.List<PlayerProfile> list=new java.util.ArrayList<>(ratingManager.getAllProfiles()); list.sort((a,b)->Integer.compare(b.getElo(), a.getElo()));
            String head=plugin.getConfig().getString("ui.top-header","&6TOP {page}").replace("{page}", String.valueOf(page)).replace("{left}", seasonManager.leftString()); sender.sendMessage(Util.color(head));
            int start=(page-1)*per; for(int i=0;i<per;i++){ int idx=start+i; if(idx>=list.size()) break; PlayerProfile pp=list.get(idx); TierManager.Tier t=tierManager.ofElo(pp.getElo());
                String line=plugin.getConfig().getString("ui.top-line","&e#{rank} &f{player} &7- &a{elo} Elo &8{tier}")
                        .replace("{rank}", String.valueOf(idx+1))
                        .replace("{player}", pp.getLastKnownName()==null? pp.getUuid().toString():pp.getLastKnownName())
                        .replace("{elo}", String.valueOf(pp.getElo()))
                        .replace("{tier}", Util.color(t.name));
                sender.sendMessage(Util.color(line)); }
            return true;
        }
        if (sub.equals("결과")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (args.length<3){ sender.sendMessage("사용법: /레이팅 결과 <승자> <패자> [draw]"); return true; }
            boolean draw=(args.length>=4)&&(args[3].equalsIgnoreCase("draw")||args[3].equalsIgnoreCase("무승부"));
            ratingManager.recordResultNames(args[1], args[2], draw); sender.sendMessage(Util.color("&a기록됨.")); return true;
        }
        if (sub.equals("elo설정")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (args.length<3){ sender.sendMessage("사용법: /레이팅 elo설정 <닉> <elo>"); return true; }
            int elo; try{ elo=Integer.parseInt(args[2]); }catch(Exception e){ sender.sendMessage("정수 입력"); return true; }
            PlayerProfile pr=ratingManager.byName(args[1]); if (pr==null){ sender.sendMessage("플레이어를 찾을 수 없습니다."); return true; }
            pr.setElo(elo); ratingManager.saveProfile(pr); sender.sendMessage("설정됨: "+pr.getLastKnownName()+" => "+elo); return true;
        }
        if (sub.equals("시즌설정")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (args.length<2){ sender.sendMessage("사용법: /레이팅 시즌설정 <초>"); return true; }
            long sec; try{ sec=Long.parseLong(args[1]); }catch(Exception e){ sender.sendMessage("정수 초 입력"); return true; }
            if (sec<=0){ sender.sendMessage("양수 입력"); return true; }
            seasonManager.setSeasonSecondsFromNow(sec); sender.sendMessage(Util.color(plugin.getConfig().getString("ui.set-season").replace("{seconds}", String.valueOf(sec)))); return true;
        }
        if (sub.equals("시즌설정날짜")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (args.length<5){ sender.sendMessage("사용법: /레이팅 시즌설정날짜 <월> <일> <시> <분> [초] [연]"); return true; }
            try{
                int month=Integer.parseInt(args[1]), day=Integer.parseInt(args[2]), hour=Integer.parseInt(args[3]), minute=Integer.parseInt(args[4]);
                int second = (args.length>=6? Integer.parseInt(args[5]) : 0);
                java.time.LocalDate now = java.time.LocalDate.now(seasonManager.getZoneId());
                int year = (args.length>=7? Integer.parseInt(args[6]) : now.getYear());
                seasonManager.setSeasonAbsolute(year, month, day, hour, minute, second);
                sender.sendMessage(Util.color("&a시즌 종료 시각 설정됨: "+year+"/"+month+"/"+day+" "+hour+":"+minute+":"+second));
            } catch(Exception ex){ sender.sendMessage(Util.color("&c날짜 형식을 확인하세요.")); }
            return true;
        }
        if (sub.equals("시즌즉시종료")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            seasonManager.performReset(); sender.sendMessage(Util.color("&a시즌을 즉시 종료하고 초기화했습니다.")); return true;
        }
        if (sub.equals("시즌남은시간")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.season-left").replace("{left}", seasonManager.leftString()))); return true; }
        if (sub.equals("보상설정")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (!(sender instanceof Player)){ sender.sendMessage("Player only."); return true; }
            if (args.length<2){ sender.sendMessage("사용법: /레이팅 보상설정 <티어표시명>"); return true; }
            rewardGUI.openEditor((Player)sender, args[1]); return true;
        }
        if (sub.equals("reload")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            plugin.reloadConfig(); tierManager.reload(); banEnforcer.reload(); chatListener.reload();
            sender.sendMessage(Util.color(plugin.getConfig().getString("ui.reloaded"))); return true;
        }
        sender.sendMessage(Util.color("&c알 수 없는 서브명령. /레이팅 도움말")); return true;
    }

    @Override public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if (args.length==1){
            java.util.List<String> base=new java.util.ArrayList<>(java.util.Arrays.asList("도움말","참가","취소","상태","전적","랭킹","시즌남은시간"));
            if (sender.hasPermission("upr.admin")) base.addAll(java.util.Arrays.asList("결과","시즌설정","시즌설정날짜","보상설정","elo설정","시즌즉시종료","reload"));
            String t=args[0].toLowerCase(); java.util.List<String> out=new java.util.ArrayList<>(); for(String s: base){ if (s.startsWith(t)) out.add(s); } return out;
        }
        if (args.length==2 && (args[0].equalsIgnoreCase("전적")||args[0].equalsIgnoreCase("결과")||args[0].equalsIgnoreCase("elo설정"))){
            java.util.List<String> names=new java.util.ArrayList<>(); for (Player p: Bukkit.getOnlinePlayers()) names.add(p.getName());
            String t=args[1].toLowerCase(); java.util.List<String> out=new java.util.ArrayList<>(); for(String s:names){ if (s.toLowerCase().startsWith(t)) out.add(s); } return out;
        }
        if (args.length==3 && args[0].equalsIgnoreCase("결과")){
            java.util.List<String> names=new java.util.ArrayList<>(); for (Player p: Bukkit.getOnlinePlayers()) names.add(p.getName());
            String t=args[2].toLowerCase(); java.util.List<String> out=new java.util.ArrayList<>(); for(String s:names){ if (s.toLowerCase().startsWith(t)) out.add(s); } return out;
        }
        if (args[0].equalsIgnoreCase("시즌설정날짜")){
            // provide simple numeric suggestions
            if (args.length==2) return java.util.Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12");
            if (args.length==3) return java.util.Arrays.asList("1","5","10","15","20","25","28","30","31");
            if (args.length==4) return java.util.Arrays.asList("0","12","18","23");
            if (args.length==5) return java.util.Arrays.asList("0","15","30","45");
            if (args.length==6) return java.util.Arrays.asList("0","15","30","45","59");
        }
        return java.util.Collections.emptyList();
    }
}
