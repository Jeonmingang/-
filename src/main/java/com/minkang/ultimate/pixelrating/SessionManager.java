package com.minkang.ultimate.pixelrating;
public class SessionManager { public static class Session{ public java.util.UUID p1,p2; public long startMs=System.currentTimeMillis(); }
  private final java.util.Map<java.util.UUID,Session> sessions=new java.util.HashMap<>();
  public SessionManager(UltimatePixelmonRatingPlugin plugin){} public Session get(java.util.UUID u){ return sessions.get(u);} public void put(java.util.UUID u, Session s){ sessions.put(u,s);} public void remove(java.util.UUID u){ sessions.remove(u);} }