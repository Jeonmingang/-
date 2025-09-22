package com.minkang.ultimate.pixelrating;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RatingCommand implements CommandExecutor {

    private final UltimatePixelmonRatingPlugin plugin;

    public RatingCommand(UltimatePixelmonRatingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(color("&b/레이팅 경기장 생성 <id>, 위치설정 <id> <p1|p2>, 활성화 <id> <true|false>, 목록"));
            return true;
        }
        if (!sender.hasPermission("upr.admin")) {
            sender.sendMessage(color("&c권한이 없습니다."));
            return true;
        }
        if ("경기장".equals(args[0])) {
            return handleArena(sender, args);
        }
        sender.sendMessage(color("&c알 수 없는 하위 명령입니다."));
        return true;
    }

    private boolean handleArena(CommandSender sender, String[] args) {
        try {
            if (args.length >= 3 && "생성".equals(args[1])) {
                String id = args[2];
                plugin.arenas().create(id);
                sender.sendMessage(color("&a경기장 생성: &f" + id));
                return true;
            }
            if (args.length >= 5 && "위치설정".equals(args[1])) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(color("&c플레이어만 사용 가능합니다."));
                    return true;
                }
                String id = args[2];
                String point = args[3];
                Player p = (Player) sender;
                Location loc = p.getLocation();
                plugin.arenas().set(id, point, loc);
                sender.sendMessage(color("&a경기장 위치설정 완료: &f" + id + " &7(" + point + ")"));
                return true;
            }
            if (args.length >= 5 && "활성화".equals(args[1])) {
                String id = args[2];
                boolean enabled = Boolean.parseBoolean(args[3]);
                plugin.arenas().setEnabled(id, enabled);
                sender.sendMessage(color("&a경기장 활성화 변경: &f" + id + " &7-> " + enabled));
                return true;
            }
            if (args.length == 2 && "목록".equals(args[1])) {
                sender.sendMessage(color("&b[경기장 목록]"));
                for (Arena a : plugin.arenas().list()) {
                    sender.sendMessage(color("&7- &f" + a.getId() + " &7enabled=&a" + a.isEnabled()));
                }
                return true;
            }
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(color("&c오류: " + ex.getMessage()));
            return true;
        } catch (Throwable t) {
            sender.sendMessage(color("&c알 수 없는 오류가 발생했습니다. 콘솔을 확인하세요."));
            t.printStackTrace();
            return true;
        }
        sender.sendMessage(color("&c사용법: /레이팅 경기장 생성 <id> | 위치설정 <id> <p1|p2> | 활성화 <id> <true|false> | 목록"));
        return true;
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}