package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit; import org.bukkit.OfflinePlayer; import org.bukkit.entity.Player; import java.util.*; import java.util.concurrent.ConcurrentHashMap;

public class RatingManager {
    private final UltimatePixelmonRatingPlugin plugin; private final StorageYAML storage; private final TierManager tierManager;
    private final Map<java.util.UUID, PlayerProfile> cache=new ConcurrentHashMap<>();
    public RatingManager(UltimatePixelmonRatingPlugin plugin, StorageYAML storage, TierManager tierManager){ this.plugin=plugin; this.storage=storage; this.tierManager=tierManager;
        for (java.util.UUID u: storage.allUUIDs()){ PlayerProfile p=storage.load(u); if (p!=null) cache.put(u,p); } }
    public PlayerProfile getProfile(java.util.UUID uuid, String name){
        PlayerProfile p=cache.get(uuid); if (p==null){ p=new PlayerProfile(uuid, name); p.setElo(plugin.getConfig().getInt("rating.initial-elo",1200)); storage.save(p); cache.put(uuid,p); }
        else { if (name!=null && !name.isEmpty()) p.setLastKnownName(name); } return p;
    }
    public PlayerProfile byName(String name){
        Player on=Bukkit.getPlayerExact(name); if (on!=null) return getProfile(on.getUniqueId(), on.getName());
        OfflinePlayer off=Bukkit.getOfflinePlayer(name); if (off!=null && off.getUniqueId()!=null) return getProfile(off.getUniqueId(), off.getName());
        return null;
    }
    public void saveProfile(PlayerProfile p){ storage.save(p); cache.put(p.getUuid(),p); }
    public void recordResultNames(String w,String l, boolean draw){
        PlayerProfile A=byName(w), B=byName(l); if (A==null||B==null) return;
        EloCalculator.Result r=EloCalculator.calculate(plugin, A.getElo(), B.getElo(), draw? EloCalculator.Outcome.DRAW : EloCalculator.Outcome.WIN);
        if (draw){ A.addDraw(); B.addDraw(); } else { A.addWin(); B.addLoss(); }
        A.setElo(r.newA); B.setElo(r.newB); long now=System.currentTimeMillis(); A.setLastMatchAt(now); B.setLastMatchAt(now); saveProfile(A); saveProfile(B);
    }
    public java.util.Collection<PlayerProfile> getAllProfiles(){ return cache.values(); }
}
