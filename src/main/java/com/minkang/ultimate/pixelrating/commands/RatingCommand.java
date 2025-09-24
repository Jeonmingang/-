package com.minkang.ultimate.pixelrating.commands;

import com.minkang.ultimate.pixelrating.UltimatePixelmonRatingPlugin;
import com.minkang.ultimate.pixelrating.elo.EloService;
import com.minkang.ultimate.pixelrating.store.RatingStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class RatingCommand implements CommandExecutor, TabCompleter {

    private final UltimatePixelmonRatingPlugin plugin;
    private final EloService elo;
    private final RatingStore store;

    public RatingCommand(UltimatePixelmonRatingPlugin plugin, EloService elo, RatingStore store) {
        this.plugin = plugin;
        this.elo = elo;
        this.store = store;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { help(sender); return true; }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "랭킹":
            case "ranking": {
                int page = 1;
                if (args.length >= 2) try { page = Math.max(1, Integer.parseInt(args[1])); } catch (Exception ignored) {}
                showRanking(sender, page);
                return true;
            }
            case "전적":
            case "stats": {
                String name = args.length >= 2 ? args[1] : sender.getName();
                showStats(sender, name);
                return true;
            }
            case "시즌":
            case "season": {
                showSeason(sender);
                return true;
            }
            default: help(sender); return true;
        }
    }

    private void help(CommandSender s) {
        s.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "===== 픽셀몬 레이팅 (Elo) =====");
        s.sendMessage(ChatColor.YELLOW + " /레이팅 랭킹 [페이지] " + ChatColor.GRAY + " - 상위 Elo 순위");
        s.sendMessage(ChatColor.YELLOW + " /레이팅 전적 [닉네임] " + ChatColor.GRAY + " - 개인 Elo/전적");
        s.sendMessage(ChatColor.YELLOW + " /레이팅 시즌 " + ChatColor.GRAY + " - 시즌 종료 안내");
        s.sendMessage(ChatColor.AQUA  + " 전투 종료 시 자동으로 Elo가 정산됩니다.");
    }

    private void showSeason(CommandSender s) {
        int m = plugin.getConfig().getInt("season-end.month", 12);
        int d = plugin.getConfig().getInt("season-end.day", 31);
        int h = plugin.getConfig().getInt("season-end.hour", 23);
        int min = plugin.getConfig().getInt("season-end.minute", 59);
        s.sendMessage(ChatColor.GOLD + "시즌 종료 예정: " + ChatColor.YELLOW + String.format("%02d월 %02d일 %02d:%02d", m, d, h, min));
    }

    private void showRanking(CommandSender s, int page) {
        List<Map.Entry<UUID, Integer>> top = store.top(10, page);
        if (top.isEmpty()) { s.sendMessage(ChatColor.GRAY + "데이터가 없습니다."); return; }
        s.sendMessage(ChatColor.GOLD + "===== Elo 랭킹 (p." + page + ") =====");
        int rank = (page - 1) * 10 + 1;
        for (Map.Entry<UUID,Integer> e : top) {
            String name = store.nameOf(e.getKey());
            s.sendMessage(ChatColor.YELLOW + String.format("%2d위 ", rank++) + ChatColor.WHITE + name + ChatColor.GRAY + " - " + e.getValue());
        }
        showSeason(s);
    }

    private void showStats(CommandSender s, String name) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(name);
        if (op == null || op.getUniqueId() == null) {
            s.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + name);
            return;
        }
        int eloValue = store.getRating(op.getUniqueId());
        s.sendMessage(ChatColor.GOLD + name + ChatColor.GRAY + " 의 Elo: " + ChatColor.YELLOW + eloValue);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("랭킹","전적","시즌");
        if (args.length == 2 && args[0].equalsIgnoreCase("전적")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
