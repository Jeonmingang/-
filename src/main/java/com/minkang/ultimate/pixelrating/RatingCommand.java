
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
        this.plugin = plugin;
        this.ratingManager = ratingManager;
        this.tierManager = tierManager;
        this.seasonManager = seasonManager;
        this.rewardGUI = rewardGUI;
        this.arenaManager = arenaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = (args.length == 0) ? "도움말" : args[0].toLowerCase();

        if (label.equalsIgnoreCase("경쟁전")) {
            // alias command: treat only 참가/취소 for simplicity
            if (sub.equals("도움말")) {
                sendHelp(sender);
                return true;
            }
            return handleQueue(sender, sub, args);
        }

        if (sub.equals("도움말")) { sendHelp(sender); return true; }

        if (sub.equals("참가") || sub.equals("취소")) {
            return handleQueue(sender, sub, args);
        }

        if (sub.equals("전적")) {
            if (args.length >= 2) {
                String name = args[1];
                PlayerProfile prof = ratingManager.byName(name);
                if (prof == null) { sender.sendMessage(Util.color("&c플레이어를 찾을 수 없습니다.")); return true; }
                TierManager.Tier t = tierManager.ofElo(prof.getElo());
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.stats-other", "&e{player} &7| Elo &b{elo} &7| 티어 &a{tier} &7| 승 {wins} 패 {losses} 무 {draws}")
                        .replace("{player}", prof.getLastKnownName())
                        .replace("{elo}", String.valueOf(prof.getElo()))
                        .replace("{tier}", Util.color(t.name))
                        .replace("{wins}", String.valueOf(prof.getWins()))
                        .replace("{losses}", String.valueOf(prof.getLosses()))
                        .replace("{draws}", String.valueOf(prof.getDraws()))));
            } else {
                if (!(sender instanceof Player)) { sender.sendMessage("사용법: /레이팅 전적 <닉>"); return true; }
                Player p = (Player) sender;
                PlayerProfile prof = ratingManager.getProfile(p.getUniqueId(), p.getName());
                TierManager.Tier t = tierManager.ofElo(prof.getElo());
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.stats-self", "&e내 전적 &7| Elo &b{elo} &7| 티어 &a{tier} &7| 승 {wins} 패 {losses} 무 {draws}")
                        .replace("{elo}", String.valueOf(prof.getElo()))
                        .replace("{tier}", Util.color(t.name))
                        .replace("{wins}", String.valueOf(prof.getWins()))
                        .replace("{losses}", String.valueOf(prof.getLosses()))
                        .replace("{draws}", String.valueOf(prof.getDraws()))));
            }
            return true;
        }

        if (sub.equals("랭킹") || sub.equals("rank")) {
            int page = 1;
            if (args.length >= 2) {
                try { page = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            }
            sendRanking(sender, Math.max(1, page));
            return true;
        }

        if (sub.equals("보상설정")) {
            if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (!(sender instanceof Player)) { sender.sendMessage("Player only."); return true; }
            if (args.length < 2) { sender.sendMessage("사용법: /레이팅 보상설정 <티어표시명>"); return true; }
            rewardGUI.openEditor((Player) sender, args[1]);
            return true;
        }

        
if (sub.equals("강제정산")) {
    if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
    if (args.length < 3) { sender.sendMessage("사용법: /레이팅 강제정산 <승자닉> <패자닉> [draw:true|false]"); return true; }
    boolean draw = args.length >=4 && Boolean.parseBoolean(args[3]);
    RatingManager.Change ch = ratingManager.recordResultNames(args[1], args[2], draw);
    if (ch == null) { sender.sendMessage("플레이어를 찾을 수 없습니다."); return true; }
    sender.sendMessage(Util.color("&a강제 정산 완료: &f" + ch.wName + " vs " + ch.lName + (draw?" &7(무승부)":"")));
    return true;
}

if (sub.equals("시즌종료")) {
    if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
    if (args.length < 6) { sender.sendMessage("사용법: /레이팅 시즌종료 <월> <일> <시> <분> <초>"); return true; }
    try {
        int mon = Integer.parseInt(args[1]);
        int day = Integer.parseInt(args[2]);
        int hh  = Integer.parseInt(args[3]);
        int mm  = Integer.parseInt(args[4]);
        int ss  = Integer.parseInt(args[5]);
        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        java.time.LocalDate now = java.time.LocalDate.now(zone);
        java.time.LocalDate targetDate = java.time.LocalDate.of(now.getYear(), mon, day);
        java.time.LocalDateTime ldt = java.time.LocalDateTime.of(targetDate, java.time.LocalTime.of(hh, mm, ss));
        java.time.ZonedDateTime zdt = ldt.atZone(zone);
        if (zdt.toInstant().toEpochMilli() <= System.currentTimeMillis()) {
            // 이미 지난 시각이면 1년 뒤 같은 날짜로
            zdt = ldt.plusYears(1).atZone(zone);
        }
        long epoch = zdt.toInstant().toEpochMilli();
        plugin.getConfig().set("season.ends-at-epoch-ms", epoch);
        plugin.getConfig().set("season.enabled", true);
        plugin.saveConfig();
        plugin.season().startTicker();
        sender.sendMessage(Util.color("&a시즌 종료 시각 설정 완료: &f" + zdt.toString()));
    } catch (Exception ex) {
        sender.sendMessage(Util.color("&c입력값이 올바르지 않습니다. 예) /레이팅 시즌종료 12 31 23 59 59"));
    }
    return true;
}
if (sub.equals("경기장")) {
            return handleArena(sender, args);
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            plugin.reloadConfig(); tierManager.reload(); plugin.arenas().load();
            sender.sendMessage(Util.color(plugin.getConfig().getString("ui.reloaded")));
            return true;
        }

        sender.sendMessage(Util.color("&c알 수 없는 서브명령. /레이팅 도움말"));
        return true;
    }

    private boolean handleQueue(CommandSender sender, String sub, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용 가능합니다."); return true; }
        Player p = (Player) sender;
        if (sub.equals("참가")) {
            boolean ok = plugin.queue().join(p.getUniqueId());
            if (ok) {
                QueueActionBarNotifier.start(plugin, p);
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.queue-join", "&a대기열에 참가했습니다.")));
            } else {
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.queue-already", "&c이미 대기열에 있습니다.")));
            }
            return true;
        } else if (sub.equals("취소")) {
            boolean ok = plugin.queue().leave(p.getUniqueId());
            QueueActionBarNotifier.stop(p);
            if (ok) sender.sendMessage(Util.color(plugin.getConfig().getString("ui.queue-leave", "&e대기열을 떠났습니다.")));
            else sender.sendMessage(Util.color(plugin.getConfig().getString("ui.queue-not-in", "&c대기열에 있지 않습니다.")));
            return true;
        }
        return false;
    }

    private void sendRanking(CommandSender sender, int page) {
        try {
            int per = 10;
            List<PlayerProfile> list = new ArrayList<>(ratingManager.getAllProfiles());
            list.sort((a, b) -> Integer.compare(b.getElo(), a.getElo()));
            String head = plugin.getConfig().getString("ui.top-header",
                    "&6[레이팅 랭킹] &7페이지 {page} &8(시즌 {left} 남음)")
                    .replace("{page}", String.valueOf(page))
                    .replace("{left}", plugin.season().leftString());
            sender.sendMessage(Util.color(head));
            int start = (page - 1) * per;
            for (int i = 0; i < per; i++) {
                int idx = start + i; if (idx >= list.size()) break;
                PlayerProfile pp = list.get(idx);
                TierManager.Tier t = tierManager.ofElo(pp.getElo());
                String line = plugin.getConfig().getString("ui.top-line", "&e#{rank} &f{player} &7Elo &b{elo} &7티어 &a{tier}");
                line = line.replace("{rank}", String.valueOf(idx + 1))
                        .replace("{player}", pp.getLastKnownName())
                        .replace("{elo}", String.valueOf(pp.getElo()))
                        .replace("{tier}", Util.color(t.name));
                sender.sendMessage(Util.color(line));
            }
        } catch (Throwable t) {
            sender.sendMessage("§c랭킹을 불러오지 못했습니다: " + t.getClass().getSimpleName());
            t.printStackTrace();
        }
    }

    private boolean handleArena(CommandSender sender, String[] args) {
        if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
        if (args.length == 1) { sender.sendMessage(Util.color("&6/레이팅 경기장 생성 <이름> | 삭제 <이름> | 활성화 <이름> <true|false> | 위치설정 <이름> <p1|p2|stage|spec> | 목록 | 정보")); return true; }
        String sub = args[1];
        if (sub.equalsIgnoreCase("생성")) {
            if (args.length < 3) { sender.sendMessage("사용법: /레이팅 경기장 생성 <이름>"); return true; }
            boolean ok = plugin.arenas().create(args[2]);
            sender.sendMessage(Util.color(ok ? "&a생성됨." : "&c이미 존재합니다."));
            return true;
        }
        if (sub.equalsIgnoreCase("삭제")) {
            if (args.length < 3) { sender.sendMessage("사용법: /레이팅 경기장 삭제 <이름>"); return true; }
            boolean ok = plugin.arenas().delete(args[2]);
            sender.sendMessage(Util.color(ok ? "&a삭제됨." : "&c없거나 삭제 실패."));
            return true;
        }
        if (sub.equalsIgnoreCase("활성화")) {
            if (args.length < 4) { sender.sendMessage("사용법: /레이팅 경기장 활성화 <이름> <true|false>"); return true; }
            boolean val = Boolean.parseBoolean(args[3]);
            com.minkang.ultimate.pixelrating.ArenaManager.Arena __a = plugin.arenas().get(args[2]);
            if (__a == null) { sender.sendMessage(Util.color("&c경기장을 찾을 수 없습니다.")); return true; }
            boolean ok = plugin.arenas().setEnabled(__a, val);
            sender.sendMessage(Util.color(ok ? "&a변경됨." : "&c없거나 변경 실패."));
            return true;
        }
        if (sub.equalsIgnoreCase("위치설정")) {
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용 가능합니다."); return true; }
            if (args.length < 4) { sender.sendMessage("사용법: /레이팅 경기장 위치설정 <이름> <p1|p2|stage|spec>"); return true; }
            Player p = (Player) sender;
            com.minkang.ultimate.pixelrating.ArenaManager.Arena __a2 = plugin.arenas().get(args[2]);
            if (__a2 == null) { sender.sendMessage(Util.color("&c경기장을 찾을 수 없습니다.")); return true; }
            boolean ok = plugin.arenas().set(__a2, args[3], p.getLocation());
            sender.sendMessage(Util.color(ok ? "&a저장됨." : "&c없거나 포인트명 오류."));
            return true;
        }
        if (sub.equalsIgnoreCase("목록")) {
            StringBuilder sb = new StringBuilder();
            for (ArenaManager.Arena a : plugin.arenas().list()) {
                sb.append(a.name).append(a.enabled ? "§a(활성)" : "§c(비활성)").append(" ");
            }
            sender.sendMessage(sb.length() == 0 ? "경기장 없음" : sb.toString());
            return true;
        }
        if (sub.equalsIgnoreCase("정보")) {
            if (args.length < 3) { sender.sendMessage("사용법: /레이팅 경기장 정보 <이름>"); return true; }
            for (ArenaManager.Arena a : plugin.arenas().list()) {
                if (a.name.equalsIgnoreCase(args[2])) {
                    sender.sendMessage(Util.color("&6이름: &f"+a.name+" &7활성: "+(a.enabled?"§a예":"§c아니오")));
                    sender.sendMessage(" p1=" + (a.p1==null? "null":a.p1) + " p2=" + (a.p2==null? "null":a.p2) + " stage=" + (a.stage==null? "null":a.stage) + " spec=" + (a.spec==null? "null":a.spec));
                    return true;
                }
            }
            sender.sendMessage("경기장을 찾을 수 없습니다.");
            return true;
        }
        sender.sendMessage(Util.color("&c알 수 없는 인자. /레이팅 경기장"));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Util.color("&6/레이팅 참가 &7- 대기열 참가"));
        sender.sendMessage(Util.color("&6/레이팅 취소 &7- 대기열 취소"));
        sender.sendMessage(Util.color("&6/레이팅 전적 [닉] &7- 내/다른 유저 전적 확인"));
        sender.sendMessage(Util.color("&6/레이팅 랭킹 [페이지] &7- 시즌 랭킹"));
        if (sender.hasPermission("upr.admin")) {
            sender.sendMessage(Util.color("&6/레이팅 경기장 ... &7- 경기장 관리"));
            sender.sendMessage(Util.color("&6/레이팅 보상설정 <티어표시명> &7- 보상 GUI"));
            sender.sendMessage(Util.color("&6/레이팅 reload &7- 설정 리로드"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>(Arrays.asList("참가","취소","전적","랭킹"));
            if (sender.hasPermission("upr.admin")) base.addAll(Arrays.asList("경기장","보상설정","reload"));
            return filter(base, args[0]);
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("경기장")) {
            if (args.length == 2) return filter(Arrays.asList("생성","삭제","활성화","위치설정","목록","정보"), args[1]);
            if (args.length == 3 && Arrays.asList("삭제","활성화","위치설정","정보").contains(args[1])) {
                List<String> names = new ArrayList<>();
                for (ArenaManager.Arena a : plugin.arenas().list()) names.add(a.name);
                return filter(names, args[2]);
            }
            if (args.length == 4 && args[1].equals("활성화")) return filter(Arrays.asList("true","false"), args[3]);
            if (args.length == 4 && args[1].equals("위치설정")) return filter(Arrays.asList("p1","p2","stage","spec"), args[3]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> src, String prefix) {
        if (prefix == null || prefix.isEmpty()) return src;
        List<String> out = new ArrayList<>();
        for (String s : src) if (s.toLowerCase().startsWith(prefix.toLowerCase())) out.add(s);
        return out;
    }
}