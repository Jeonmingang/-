
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RatingCommand implements CommandExecutor, TabCompleter {

    private final UltimatePixelmonRatingPlugin plugin;
    private final RatingManager ratingManager;
    private final TierManager tierManager;
    private final SeasonManager seasonManager;
    private final RewardGUI rewardGUI;
    private final ArenaManager arenaManager;

    public RatingCommand(UltimatePixelmonRatingPlugin plugin, RatingManager ratingManager, TierManager tierManager, SeasonManager seasonManager, RewardGUI rewardGUI, ArenaManager arenaManager){
        this.plugin=plugin; this.ratingManager=ratingManager; this.tierManager=tierManager; this.seasonManager=seasonManager; this.rewardGUI=rewardGUI; this.arenaManager=arenaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length>0? args[0].toLowerCase() : "도움말";
        if (sub.equals("도움말")){
            sender.sendMessage(Util.color("&6/레이팅 참가, 취소, 상태, 전적 [닉], 랭킹 [페이지], 시즌남은시간"));
            sender.sendMessage(Util.color("&7관리자: 결과 <승자> <패자> [draw], 시즌설정 <초>, 보상설정 <티어표시명>, 경기장 <...>, reload"));
            return true;
        }
        if (sub.equals("참가")){
            if (!(sender instanceof Player)){ sender.sendMessage("Player only."); return true; }
            Player p=(Player)sender;
            boolean ok = plugin.queue().join(p);
            if (ok) QueueActionBarNotifier.start(plugin, p);
            p.sendMessage(Util.color(plugin.getConfig().getString(ok? "ui.joined-queue":"ui.already-in-queue")));
            return true;
        }
        if (sub.equals("취소")){
            if (!(sender instanceof Player)){ sender.sendMessage("Player only."); return true; }
            Player p=(Player)sender;
            boolean ok = plugin.queue().leave(p);
            QueueActionBarNotifier.stop(p);
            p.sendMessage(Util.color(plugin.getConfig().getString(ok? "ui.left-queue":"ui.not-in-queue")));
            return true;
        }
        if (sub.equals("상태")){
            sender.sendMessage(Util.color("&7큐: &f"+String.join(", ", plugin.queue().names())));
            return true;
        }
        if (sub.equals("전적")){
            if (args.length>=2){
                String name=args[1];
                PlayerProfile prof = ratingManager.byName(name);
                if (prof==null){ sender.sendMessage(Util.color("&c플레이어를 찾을 수 없습니다.")); return true; }
                TierManager.Tier t = tierManager.ofElo(prof.getElo());
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.stats-other")
                        .replace("{player}", prof.getLastKnownName())
                        .replace("{elo}", String.valueOf(prof.getElo()))
                        .replace("{tier}", Util.color(t.name))
                        .replace("{wins}", String.valueOf(prof.getWins()))
                        .replace("{losses}", String.valueOf(prof.getLosses()))
                        .replace("{draws}", String.valueOf(prof.getDraws()))));
            } else {
                if (!(sender instanceof Player)){ sender.sendMessage("사용법: /레이팅 전적 <닉>"); return true; }
                Player p=(Player)sender; PlayerProfile prof = ratingManager.getProfile(p.getUniqueId(), p.getName());
                TierManager.Tier t = tierManager.ofElo(prof.getElo());
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.stats-self")
                        .replace("{elo}", String.valueOf(prof.getElo()))
                        .replace("{tier}", Util.color(t.name))
                        .replace("{wins}", String.valueOf(prof.getWins()))
                        .replace("{losses}", String.valueOf(prof.getLosses()))
                        .replace("{draws}", String.valueOf(prof.getDraws()))));
            }
            return true;
        }
        if (sub.equals("랭킹")){
            int page=1; if (args.length>=2) try { page=Integer.parseInt(args[1]); } catch (NumberFormatException ignored){}
            int per=10; java.util.List<PlayerProfile> list=new java.util.ArrayList<>(ratingManager.getAllProfiles());
            list.sort((a,b)->Integer.compare(b.getElo(), a.getElo()));
            String head=plugin.getConfig().getString("ui.top-header").replace("{page}", String.valueOf(page)).replace("{left}", seasonManager.leftString());
            sender.sendMessage(Util.color(head));
            int start=(page-1)*per;
            for (int i=0;i<per;i++){
                int idx=start+i; if (idx>=list.size()) break;
                PlayerProfile p=list.get(idx);
                TierManager.Tier t=tierManager.ofElo(p.getElo());
                String line=plugin.getConfig().getString("ui.top-line")
                        .replace("{rank}", String.valueOf(idx+1))
                        .replace("{player}", p.getLastKnownName()==null? p.getUuid().toString() : p.getLastKnownName())
                        .replace("{elo}", String.valueOf(p.getElo()))
                        .replace("{tier}", Util.color(t.name));
                sender.sendMessage(Util.color(line));
            }
            return true;
        }
        if (sub.equals("결과")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (args.length<3){ sender.sendMessage("사용법: /레이팅 결과 <승자> <패자> [draw]"); return true; }
            String w=args[1], l=args[2]; boolean draw=false;
            if (args.length>=4){ String f=args[3].toLowerCase(); if (f.equals("draw")||f.equals("무승부")) draw=true; }
            ratingManager.recordResultNames(w,l,draw);
            sender.sendMessage(Util.color("&a기록됨."));
            return true;
        }
        if (sub.equals("시즌설정")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (args.length<2){ sender.sendMessage("사용법: /레이팅 시즌설정 <초>"); return true; }
            long sec=0L; try { sec=Long.parseLong(args[1]); } catch (NumberFormatException ignored){}
            if (sec<=0){ sender.sendMessage("양의 정수 초를 입력하세요."); return true; }
            seasonManager.setSeasonSecondsFromNow(sec);
            sender.sendMessage(Util.color(plugin.getConfig().getString("ui.set-season").replace("{seconds}", String.valueOf(sec))));
            return true;
        }
        if (sub.equals("시즌남은시간")){
            sender.sendMessage(Util.color(plugin.getConfig().getString("ui.season-left").replace("{left}", seasonManager.leftString())));
            return true;
        }
        if (sub.equals("보상설정")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (!(sender instanceof Player)){ sender.sendMessage("Player only."); return true; }
            if (args.length<2){ sender.sendMessage("사용법: /레이팅 보상설정 <티어표시명>"); return true; }
            rewardGUI.openEditor((Player)sender, args[1]); return true;
        }
        if (sub.equals("경기장")){ return handleArena(sender, args); }
        if (sub.equals("reload")){
            if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            plugin.reloadConfig(); tierManager.reload(); plugin.arenas().load();
            sender.sendMessage(Util.color(plugin.getConfig().getString("ui.reloaded"))); return true;
        }
        sender.sendMessage(Util.color("&c알 수 없는 서브명령. /레이팅 도움말")); return true;
    }

    private boolean handleArena(CommandSender sender, String[] args){
        if (!sender.hasPermission("upr.admin")){ sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
        if (args.length==1){ sender.sendMessage(Util.color("&6/레이팅 경기장 생성 <이름> | 삭제 <이름> | 목록 | 활성화 <이름> <true|false> | 위치설정 <이름> <p1|p2|stage|spec> | 정보 <이름>")); return true; }
        String sub = args[1].toLowerCase();
        if (sub.equals("생성")){
            if (args.length<3){ sender.sendMessage("사용법: /레이팅 경기장 생성 <이름>"); return true; }
            boolean ok = plugin.arenas().create(args[2]); sender.sendMessage(Util.color(ok? "&a생성됨." : "&c이미 존재합니다.")); return true;
        }
        if (sub.equals("삭제")){
            if (args.length<3){ sender.sendMessage("사용법: /레이팅 경기장 삭제 <이름>"); return true; }
            boolean ok = plugin.arenas().delete(args[2]); sender.sendMessage(Util.color(ok? "&a삭제됨." : "&c없거나 삭제 실패.")); return true;
        }
        if (sub.equals("목록")){
            StringBuilder sb=new StringBuilder();
            for (ArenaManager.Arena a : plugin.arenas().list()) sb.append(a.name).append(a.enabled? "§a(활성)":"§c(비활성)").append(" ");
            sender.sendMessage(Util.color("&6아레나: &f"+sb.toString())); return true;
        }
        if (sub.equals("활성화")){
            if (args.length<4){ sender.sendMessage("사용법: /레이팅 경기장 활성화 <이름> <true|false>"); return true; }
            boolean enabled = args[3].equalsIgnoreCase("true");
            boolean ok = plugin.arenas().setEnabled(args[2], enabled); sender.sendMessage(Util.color(ok? "&a변경됨." : "&c변경 실패.")); return true;
        }
        if (sub.equals("위치설정")){
            if (!(sender instanceof Player)){ sender.sendMessage("Player only."); return true; }
            if (args.length<4){ sender.sendMessage("사용법: /레이팅 경기장 위치설정 <이름> <p1|p2|stage|spec>"); return true; }
            boolean ok = plugin.arenas().set(args[2], args[3], ((Player)sender).getLocation());
            sender.sendMessage(Util.color(ok? "&a저장됨." : "&c실패.")); return true;
        }
        if (sub.equals("정보")){
            if (args.length<3){ sender.sendMessage("사용법: /레이팅 경기장 정보 <이름>"); return true; }
            for (ArenaManager.Arena a : plugin.arenas().list()) if (a.name.equalsIgnoreCase(args[2])){
                sender.sendMessage(Util.color("&6이름: &f"+a.name+" &7활성: "+(a.enabled?"&a예":"&c아니오")));
                sender.sendMessage(Util.color("&7p1: "+(a.p1==null?"&c미설정":"&a설정됨")+"  p2: "+(a.p2==null?"&c미설정":"&a설정됨")));
                sender.sendMessage(Util.color("&7stage: "+(a.stage==null?"&c미설정":"&a설정됨")+"  spec: "+(a.spec==null?"&c미설정":"&a설정됨")));
                return true;
            }
            sender.sendMessage(Util.color("&c해당 이름의 아레나가 없습니다.")); return true;
        }
        sender.sendMessage(Util.color("&c사용법: /레이팅 경기장 도움말")); return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length==1){
            List<String> base=new ArrayList<>(java.util.Arrays.asList("도움말","참가","취소","상태","전적","랭킹","시즌남은시간"));
            if (sender.hasPermission("upr.admin")) base.addAll(java.util.Arrays.asList("결과","시즌설정","보상설정","경기장","reload"));
            String t=args[0].toLowerCase(); List<String> out=new ArrayList<>(); for (String s:base) if (s.startsWith(t)) out.add(s); return out;
        }
        if (args.length>=2 && args[0].equalsIgnoreCase("경기장")){
            if (args.length==2){ List<String> subs=java.util.Arrays.asList("생성","삭제","목록","활성화","위치설정","정보"); String t=args[1].toLowerCase(); List<String> out=new ArrayList<>(); for (String s:subs) if (s.startsWith(t)) out.add(s); return out; }
            if (args.length==3 && (args[1].equals("삭제")||args[1].equals("활성화")||args[1].equals("위치설정")||args[1].equals("정보"))){
                List<String> names=new ArrayList<>(); for (ArenaManager.Arena a: plugin.arenas().list()) names.add(a.name);
                String t=args[2].toLowerCase(); List<String> out=new ArrayList<>(); for (String s:names) if (s.toLowerCase().startsWith(t)) out.add(s); return out;
            }
            if (args.length==4 && args[1].equals("위치설정")){
                List<String> pts=java.util.Arrays.asList("p1","p2","stage","spec"); String t=args[3].toLowerCase(); List<String> out=new ArrayList<>(); for (String s:pts) if (s.startsWith(t)) out.add(s); return out;
            }
            if (args.length==4 && args[1].equals("활성화")){
                List<String> tf=java.util.Arrays.asList("true","false"); String t=args[3].toLowerCase(); List<String> out=new ArrayList<>(); for (String s:tf) if (s.startsWith(t)) out.add(s); return out;
            }
        }
        if (args.length==2 && (args[0].equalsIgnoreCase("전적")||args[0].equalsIgnoreCase("결과"))){
            List<String> names=new ArrayList<>(); for (Player p: Bukkit.getOnlinePlayers()) names.add(p.getName());
            String t=args[1].toLowerCase(); List<String> out=new ArrayList<>(); for (String s:names) if (s.toLowerCase().startsWith(t)) out.add(s); return out;
        }
        if (args.length==3 && args[0].equalsIgnoreCase("결과")){
            List<String> names=new ArrayList<>(); for (Player p: Bukkit.getOnlinePlayers()) names.add(p.getName());
            String t=args[2].toLowerCase(); List<String> out=new ArrayList<>(); for (String s:names) if (s.toLowerCase().startsWith(t)) out.add(s); return out;
        }
        return Collections.emptyList();
    }
}
