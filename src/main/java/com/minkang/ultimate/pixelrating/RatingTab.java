package com.minkang.ultimate.pixelrating;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RatingTab implements TabCompleter {
    private final UltimatePixelmonRatingPlugin plugin;
    public RatingTab(UltimatePixelmonRatingPlugin plugin) { this.plugin = plugin; }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return prefix(Arrays.asList("경기장"), args[0]);
        if (args.length == 2 && "경기장".equals(args[0])) return prefix(Arrays.asList("생성","위치설정","활성화","목록"), args[1]);
        if (args.length == 3 && "경기장".equals(args[0])) {
            if ("생성".equals(args[1]) || "목록".equals(args[1])) return null;
            return plugin.arenas().list().stream().map(Arena::getId).collect(Collectors.toList());
        }
        if (args.length == 4 && "경기장".equals(args[0]) && "위치설정".equals(args[1])) {
            return prefix(Arrays.asList("p1","p2"), args[3]);
        }
        if (args.length == 4 && "경기장".equals(args[0]) && "활성화".equals(args[1])) {
            return prefix(Arrays.asList("true","false"), args[3]);
        }
        return null;
    }

    private List<String> prefix(List<String> list, String p) {
        String low = p.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String s : list) if (s.toLowerCase(Locale.ROOT).startsWith(low)) out.add(s);
        return out;
    }
}