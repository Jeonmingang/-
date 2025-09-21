package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; import java.util.List; import java.util.TimeZone;

public class SeasonManager {
    private final UltimatePixelmonRatingPlugin plugin; private int taskId=-1;
    public SeasonManager(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; }
    public void startTicker(){ stopTicker(); if (!plugin.getConfig().getBoolean("season.enabled", true)) return;
        int period=20*10; taskId=Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, period, period); }
    public void stopTicker(){ if (taskId!=-1){ Bukkit.getScheduler().cancelTask(taskId); taskId=-1; } }
    private void tick(){ long endMs=plugin.getConfig().getLong("season.ends-at-epoch-ms",0L); if (endMs<=0) return;
        if (System.currentTimeMillis() < endMs) return; performReset(); plugin.getConfig().set("season.ends-at-epoch-ms",0L); plugin.saveConfig(); }
    public void performReset(){
        java.util.List<PlayerProfile> all=new java.util.ArrayList<>(plugin.ratings().getAllProfiles());
        all.sort((a,b)->Integer.compare(b.getElo(), a.getElo()));
        for (PlayerProfile prof: all){ TierManager.Tier tier=plugin.tiers().ofElo(prof.getElo()); if (tier!=null) plugin.rewards().grantTierReward(prof, tier); }
        String mode=plugin.getConfig().getString("season.reset-mode","FULL");
        int initial=plugin.getConfig().getInt("rating.initial-elo",1200);
        double soft=plugin.getConfig().getDouble("season.soft-carry",0.25);
        for (PlayerProfile p: all){
            if ("SOFT".equalsIgnoreCase(mode)){ int carry=(int)Math.round((p.getElo()-initial)*soft); p.setElo(initial+carry); }
            else { p.setElo(initial); }
            p.setWins(0); p.setLosses(0); p.setDraws(0); p.setWinStreak(0); plugin.ratings().saveProfile(p);
        }
        Bukkit.broadcastMessage(Util.color("&6[레이팅] 시즌 종료! 보상이 지급되고 Elo가 초기화되었습니다."));
    }
    public String leftString(){ long endMs=plugin.getConfig().getLong("season.ends-at-epoch-ms",0L); if (endMs<=0) return "비활성";
        long left=endMs-System.currentTimeMillis(); if (left<0) left=0; return Util.timeLeft(left); }
    public void setSeasonSecondsFromNow(long seconds){ long end=System.currentTimeMillis()+seconds*1000L;
        plugin.getConfig().set("season.ends-at-epoch-ms", end); plugin.saveConfig(); }
    public void setSeasonAbsolute(int year, int month, int day, int hour, int minute, int second){
        ZoneId zone = getZoneId();
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second);
        long epochMs = ldt.atZone(zone).toInstant().toEpochMilli();
        plugin.getConfig().set("season.ends-at-epoch-ms", epochMs); plugin.saveConfig();
    }
    public ZoneId getZoneId(){
        String tz = plugin.getConfig().getString("season.timezone", "server");
        if (tz==null || tz.equalsIgnoreCase("server")) return ZoneId.systemDefault();
        try { return ZoneId.of(tz); } catch(Exception e){ return ZoneId.systemDefault(); }
    }
}