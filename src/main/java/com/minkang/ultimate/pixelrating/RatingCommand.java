package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class RatingCommand implements CommandExecutor, TabCompleter {

    private final UltimatePixelmonRatingPlugin plugin;
    private final ArenaManager arenas;
    private final QueueManager queue;
    private final RatingManager ratings;
    private final TierManager tiers;
    private final RewardManager rewardMgr;

    public RatingCommand(UltimatePixelmonRatingPlugin plugin, ArenaManager arenas, QueueManager queue, RatingManager ratings, TierManager tiers, RewardManager rewardMgr) {
        this.plugin = plugin;
        this.arenas = arenas;
        this.queue = queue;
        this.ratings = ratings;
        this.tiers = tiers;
        this.rewardMgr = rewardMgr;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "도움말".equals(args[0])) {
            sender.sendMessage("/레이팅 참가|취소|전적 [닉] | 아레나 생성 <이름> | 아레나 스폰 <이름> a|b | 아레나 활성화 <이름> <true|false>");
            return true;
        }
        String sub = args[0];
        if ("참가".equals(sub)) {
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
            Player p = (Player) sender;
            if (queue.join(p)) Util.msg(p, "queued");
            else Util.msg(p, "already-in-queue");
            return true;
        }
        if ("취소".equals(sub)) {
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
            Player p = (Player) sender;
            if (queue.leave(p)) Util.msg(p, "left-queue");
            else Util.msg(p, "not-in-queue");
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
        if ("아레나".equals(sub)) {
            if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            if (args.length < 2) { sender.sendMessage("사용법: /레이팅 아레나 <생성|스폰|활성화> ..."); return true; }
            String s2 = args[1];
            if ("생성".equals(s2)) {
                if (args.length < 3) { sender.sendMessage("사용법: /레이팅 아레나 생성 <이름>"); return true; }
                String name = args[2];
                arenas.create(name);
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.arena-created").replace("{arena}", name)));
                return true;
            } else if ("스폰".equals(s2)) {
                if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
                if (args.length < 4) { sender.sendMessage("사용법: /레이팅 아레나 스폰 <이름> a|b"); return true; }
                String name = args[2];
                String which = args[3].toLowerCase(Locale.ROOT);
                ArenaManager.Arena ar = arenas.get(name);
                if (ar == null) { sender.sendMessage("없는 아레나"); return true; }
                Location loc = ((Player)sender).getLocation();
                if ("a".equals(which)) ar.a = loc;
                else if ("b".equals(which)) ar.b = loc;
                else { sender.sendMessage("a 또는 b 입력"); return true; }
                arenas.save();
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.arena-updated").replace("{arena}", name)));
                return true;
            } else if ("활성화".equals(s2)) {
                if (args.length < 4) { sender.sendMessage("사용법: /레이팅 아레나 활성화 <이름> <true|false>"); return true; }
                String name = args[2];
                boolean flag = Boolean.parseBoolean(args[3]);
                arenas.enable(name, flag);
                sender.sendMessage(Util.color(plugin.getConfig().getString("ui.arena-enabled").replace("{arena}", name).replace("{enabled}", String.valueOf(flag))));
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
            if (!sender.hasPermission("upr.admin")) { sender.sendMessage(Util.color(plugin.getConfig().getString("ui.admin-only"))); return true; }
            plugin.reloadConfig();
            sender.sendMessage("reloaded.");
            return true;
        }
        return false;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) return java.util.Arrays.asList("참가","취소","전적","아레나","보상","시즌종료","리로드");
        if (args.length == 2 && "아레나".equals(args[0])) return java.util.Arrays.asList("생성","스폰","활성화");
        if (args.length == 4 && "아레나".equals(args[0]) && "스폰".equals(args[1])) return java.util.Arrays.asList("a","b");
        return java.util.Collections.emptyList();
    }
}
