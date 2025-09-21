package com.minkang.ultimate.pixelrating;
import java.util.UUID;
public class PlayerProfile {
    private final UUID uuid; private String lastKnownName; private int elo,wins,losses,draws,winStreak; private long lastMatchAt;
    public PlayerProfile(UUID uuid, String name){ this.uuid=uuid; this.lastKnownName=name; this.elo=1200; }
    public UUID getUuid(){ return uuid; } public String getLastKnownName(){ return lastKnownName; } public void setLastKnownName(String n){ lastKnownName=n; }
    public int getElo(){ return elo; } public void setElo(int v){ elo=v; }
    public int getWins(){ return wins; } public void setWins(int v){ wins=v; }
    public int getLosses(){ return losses; } public void setLosses(int v){ losses=v; }
    public int getDraws(){ return draws; } public void setDraws(int v){ draws=v; }
    public int getWinStreak(){ return winStreak; } public void setWinStreak(int v){ winStreak=v; }
    public long getLastMatchAt(){ return lastMatchAt; } public void setLastMatchAt(long v){ lastMatchAt=v; }
    public void addWin(){ wins+=1; winStreak+=1; } public void addLoss(){ losses+=1; winStreak=0; } public void addDraw(){ draws+=1; }
}