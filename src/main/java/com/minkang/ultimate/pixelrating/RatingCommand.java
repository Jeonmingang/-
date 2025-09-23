package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class RatingCommand implements CommandExecutor, TabCompleter {

    private final UltimatePixelmonRatingPlugin plugin;
    private final ArenaManager arenas;
    private final QueueManager queue;
    private final RatingManager ratings;
    private final TierManager tiers;
    private final RewardManager rewardMgr;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public RatingCommand(UltimatePixelmonRatingPlugin plugin, ArenaManager arenas, QueueManager queue, RatingManager ratings, TierManager tiers, RewardManager rewardMgr) {
        this.plugin = plugin;
        this.arenas = arenas;
        this.queue = queue;
        this.ratings = ratings;
        this.tiers = tiers;
        this.rewardMgr = rewardMgr;
    }

    private String cfg(String path, String def) {
        String s = plugin.getConfig().getString(path, def);
        return s == null ? def : s;
    }

    private void sendHelp(CommandSender sender){
        sender.sendMessage(Util.color(plugin.getConfig().getString("ui.help.header","&6&l[ 픽셀몬 레이팅 도움말 ]")));
        java.util.List<String> lines = plugin.getConfig().getStringList("ui.help.lines");
        for (String l : lines) sender.sendMessage(Util.color(l));
        sender.sendMessage(Util.color(plugin.getConfig().getString("ui.help.footer","")));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "도움말".equals(args[0])) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0];
        if ("참가".equals(sub)) {
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
            Player p = (Player) sender;
            if (queue.join(p)) sender.sendMessage(Util.color(cfg("ui.queued","&a매칭 대기열에 등록되었습니다.")));
            else sender.sendMessage(Util.color(cfg("ui.already-in-queue","&e이미 대기열에 있습니다.")));
            return true;
        }
        if ("취소".equals(sub)) {
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
            Player p = (Player) sender;
            if (queue.leave(p)) sender.sendMessage(Util.color(cfg("ui.left-queue","&e대기열에서 나갔습니다.")));
            else sender.sendMessage(Util.color(cfg("ui.not-in-queue","&e대기열에 없습니다.")));
            return true;
        }
        if ("전적".equals(sub)) {
            Player target = sender instanceof Player ? (Player)sender : null;
            if (args.length >= 2) target = Bukkit.getPlayerExact(args[1]);
            if (target == null) { sender.sendMessage("온라인 플레이어만 조회 가능."); return true; }
            RatingManager.Stats s = ratings.get(target.getUniqueId());
            sender.sendMessage(Util.color(plugin.getConfig().getString("ui.stats.header","")
                    .replace("{player}", target.getName())
                    .replace("{rating}", String.valueOf(s.rating))
                    .replace("{win}", String.valueOf(s.win))
                    .replace("{loss}", String.valueOf(s.loss))
                    .replace("{draw}", String.valueOf(s.draw))
                    .replace("{forfeit}", String.valueOf(s.forfeit))
            ));
            sender.sendMessage(Util.color("&7"+String.join(", ", s.recent)));
            return true;
        }
        if ("랭킹".equals(sub)) {
            int page = 1;
            if (args.length >= 2) { try { page = Math.max(1, Integer.parseInt(args[1])); } catch (NumberFormatException ignored){} }
            java.util.List<java.util.Map.Entry<java.util.UUID, RatingManager.Stats>> list = new java.util.ArrayList<>(ratings.snapshot().entrySet());
            list.sort((a,b) -> Integer.compare(b.getValue().rating, a.getValue().rating));
            int per = 10;
            int from = (page-1)*per;
            int to = Math.min(list.size(), from+per);
            sender.sendMessage(Util.color(plugin.getConfig().getString("ui.ranking.header","&6&l[ 레이팅 랭킹 ]").replace("{page}", String.valueOf(page))));
            String seasonStr;
            long epoch = new SeasonManager(plugin).getSeasonEndEpoch();
            if (epoch <= 0) seasonStr = "미설정";
            else seasonStr = FMT.format(Instant.ofEpochSecond(epoch));
            for (int i=from;i<to;i++){
                java.util.Map.Entry<java.util.UUID, RatingManager.Stats> e = list.get(i);
                int rank = i+1;
                OfflinePlayer op = Bukkit.getOfflinePlayer(e.getKey());
                String name = (op != null && op.getName()!=null) ? op.getName() : e.getKey().toString().substring(0,8);
                RatingManager.Stats s = e.getValue();
                String line = plugin.getConfig().getString("ui.ranking.line","&e#{rank} &f{ name } &7- &b{rating}")
                        .replace("{rank}", String.valueOf(rank))
                        .replace("{name}", name)
                        .replace("{rating}", String.valueOf(s.rating))
                        .replace("{win}", String.valueOf(s.win))
                        .replace("{loss}", String.valueOf(s.loss))
                        .replace("{draw}", String.valueOf(s.draw))
                        .replace("{forfeit}", String.valueOf(s.forfeit));
                sender.sendMessage(Util.color(line));
            }
            sender.sendMessage(Util.color(plugin.getConfig().getString("ui.ranking.footer","")
                    .replace("{season}", seasonStr)));
            return true;
        }
        if ("아레나".equals(sub) || "경기장".equals(sub)) {
            if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(cfg("ui.admin-only","&c관리자 전용 명령입니다."))); return true; }
            if (args.length < 2) { sender.sendMessage("사용법: /레이팅 아레나 <생성|스폰|활성화|목록> ..."); return true; }
            String s2 = args[1];
            if ("목록".equals(s2)) {
                sender.sendMessage(Util.color("&6&l[ 아레나 목록 ]"));
                for (ArenaManager.Arena ar : arenas.list()) {
                    String nm = ar.name;
                    String ok = (ar.enabled && ar.a != null && ar.b != null) ? "&aOK" : "&cX";
                    sender.sendMessage(Util.color("&e- &f"+nm+" &7| 활성화: "+(ar.enabled?"&aON":"&cOFF")+"&7 | 스폰: a:"+(ar.a!=null?"&aO":"&cX")+"&7 / b:"+(ar.b!=null?"&aO":"&cX")+" &7["+ok+"&7]"));
                }
                return true;
            }
            if ("생성".equals(s2)) {
                if (args.length < 3) { sender.sendMessage("사용법: /레이팅 아레나 생성 <이름>"); return true; }
                String name = args[2];
                arenas.create(name);
                String msg = cfg("ui.arena-created","&a아레나 &e{arena}&a가 생성되었습니다.").replace("{arena}", name);
                sender.sendMessage(Util.color(msg));
                return true;
            } else if ("스폰".equals(s2)) {
                if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
                if (args.length < 4) { sender.sendMessage("사용법: /레이팅 아레나 스폰 <이름> a|b"); return true; }
                String name = args[2];
                String which = args[3].toLowerCase(java.util.Locale.ROOT);
                ArenaManager.Arena ar = arenas.get(name);
                if (ar == null) { sender.sendMessage("없는 아레나"); return true; }
                Location loc = ((Player)sender).getLocation();
                if ("a".equals(which)) ar.a = loc;
                else if ("b".equals(which)) ar.b = loc;
                else { sender.sendMessage("a 또는 b 입력"); return true; }
                arenas.save();
                String msg = cfg("ui.arena-updated","&a아레나 &e{arena}&a가 업데이트되었습니다.").replace("{arena}", name);
                sender.sendMessage(Util.color(msg));
                return true;
            } else if ("활성화".equals(s2)) {
                if (args.length < 4) { sender.sendMessage("사용법: /레이팅 아레나 활성화 <이름> <true|false>"); return true; }
                String name = args[2];
                boolean flag = Boolean.parseBoolean(args[3]);
                arenas.enable(name, flag);
                String msg = cfg("ui.arena-enabled","&a아레나 &e{arena}&a 활성화: &e{enabled}").replace("{arena}", name).replace("{enabled}", String.valueOf(flag));
                sender.sendMessage(Util.color(msg));
                return true;
            } else {
                sender.sendMessage("사용법: /레이팅 아레나 <생성|스폰|활성화|목록> ...");
                return true;
            }
        }
        if ("보상".equals(sub)) {
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
            Player p = (Player) sender;
            RatingManager.Stats s = ratings.get(p.getUniqueId());
            rewardMgr.openGUI(p, s.rating);
            return true;
        }
        if ("시즌종료".equals(sub)) {
            if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(cfg("ui.admin-only","&c관리자 전용 명령입니다."))); return true; }
            // /레이팅 시즌종료 <월> <일> <시> <분> [초]
            if (args.length < 5) { sender.sendMessage("사용법: /레이팅 시즌종료 <월> <일> <시> <분> [초]"); return true; }
            try {
                int mon = Integer.parseInt(args[1]);
                int day = Integer.parseInt(args[2]);
                int hh  = Integer.parseInt(args[3]);
                int mm  = Integer.parseInt(args[4]);
                int ss  = (args.length >= 6) ? Integer.parseInt(args[5]) : 0;
                java.time.ZoneId zone = java.time.ZoneId.systemDefault();
                java.time.LocalDate now = java.time.LocalDate.now(zone);
                java.time.LocalDate date = java.time.LocalDate.of(now.getYear(), mon, day);
                java.time.LocalDateTime ldt = java.time.LocalDateTime.of(date, java.time.LocalTime.of(hh, mm, ss));
                long epoch = ldt.atZone(zone).toEpochSecond();
                new SeasonManager(plugin).setSeasonEndEpoch(epoch);
                sender.sendMessage("시즌 종료 시각 설정: " + ldt.toString());
            } catch (NumberFormatException ex) {
                sender.sendMessage("숫자를 정확히 입력하세요.");
            }
            return true;
        }
        if ("리로드".equals(sub)) {
            if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(cfg("ui.admin-only","&c관리자 전용 명령입니다."))); return true; }
            plugin.reloadConfig();
            sender.sendMessage("reloaded.");
            return true;
        }
        sendHelp(sender);
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) return java.util.Arrays.asList("참가","취소","전적","랭킹","아레나","보상","시즌종료","리로드");
        if (args.length == 2 && ("아레나".equals(args[0]) || "경기장".equals(args[0]))) return java.util.Arrays.asList("생성","스폰","활성화","목록");
        if (args.length == 4 && ("아레나".equals(args[0]) || "경기장".equals(args[0])) && "스폰".equals(args[1])) return java.util.Arrays.asList("a","b");
        return java.util.Collections.emptyList();
    }
}
