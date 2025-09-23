package com.minkang.ultimate.pixelrating;
public class SessionManager {
  public static class Session{ public java.util.UUID p1,p2; public long startMs=System.currentTimeMillis(); }
  private final java.util.Map<java.util.UUID,Session> byPlayer=new java.util.HashMap<>();
  public SessionManager(UltimatePixelmonRatingPlugin plugin){} 
  public synchronized void createPair(java.util.UUID a, java.util.UUID b){ Session s=new Session(); s.p1=a; s.p2=b; byPlayer.put(a,s); byPlayer.put(b,s); }
  public synchronized Session get(java.util.UUID u){ return byPlayer.get(u); }
  public synchronized java.util.UUID opponentOf(java.util.UUID u){ Session s=byPlayer.get(u); if(s==null) return null; return u.equals(s.p1)?s.p2:s.p1; }
  public synchronized void clear(java.util.UUID u){ Session s=byPlayer.remove(u); if(s!=null){ byPlayer.remove(s.p1); byPlayer.remove(s.p2);} }
}