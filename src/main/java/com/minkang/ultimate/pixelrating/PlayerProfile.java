
package com.minkang.ultimate.pixelrating;

import java.util.UUID;

public class PlayerProfile {
    private final UUID uuid;
    private String lastKnownName;
    private int elo;
    private int wins;
    private int losses;
    private int draws;
    private int winStreak;
    private long lastMatchAt;
    public PlayerProfile(UUID uuid, String name){ this.uuid=uuid; this.lastKnownName=name; this.elo=1200; }
    public UUID getUuid(){ return uuid; }
    public String getLastKnownName(){ return lastKnownName; }
    public void setLastKnownName(String s){ this.lastKnownName=s; }
    public int getElo(){ return elo; }
    public void setElo(int v){ elo=v; }
    public int getWins(){ return wins; }
    public int getLosses(){ return losses; }
    public int getDraws(){ return draws; }
    public int getWinStreak(){ return winStreak; }
    public void setWins(int v){ wins=v; }
    public void setLosses(int v){ losses=v; }
    public void setDraws(int v){ draws=v; }
    public void setWinStreak(int v){ winStreak=v; }
    public long getLastMatchAt(){ return lastMatchAt; }
    public void setLastMatchAt(long t){ lastMatchAt=t; }
    public void addWin(){ wins+=1; winStreak+=1; }
    public void addLoss(){ losses+=1; winStreak=0; }
    public void addDraw(){ draws+=1; }
}
