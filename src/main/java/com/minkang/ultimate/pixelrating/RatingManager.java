
package com.minkang.ultimate.pixelrating;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RatingManager {
    public static class Change {
        public final UUID wId; public final String wName; public final int oldW; public final int newW; public final int deltaW;
        public final UUID lId; public final String lName; public final int oldL; public final int newL; public final int deltaL;
        public final boolean draw;
        public Change(UUID wId, String wName, int oldW, int newW, UUID lId, String lName, int oldL, int newL, boolean draw){
            this.wId=wId; this.wName=wName; this.oldW=oldW; this.newW=newW; this.deltaW=newW-oldW;
            this.lId=lId; this.lName=lName; this.oldL=oldL; this.newL=newL; this.deltaL=newL-oldL; this.draw=draw;
        }
    }

    private final UltimatePixelmonRatingPlugin plugin;
    private final StorageYAML storage;
    private final TierManager tierManager;
    private final Map<UUID, PlayerProfile> cache = new ConcurrentHashMap<>();

    public RatingManager(UltimatePixelmonRatingPlugin plugin, StorageYAML storage, TierManager tierManager){
        this.plugin=plugin; this.storage=storage; this.tierManager=tierManager;
        for (UUID u : storage.allUUIDs()) {
            PlayerProfile p = storage.load(u);
            if (p != null) cache.put(u, p);
        }
    }

    public PlayerProfile getProfile(UUID uuid, String name){
        PlayerProfile p = cache.get(uuid);
        if (p == null) {
            p = new PlayerProfile(uuid, name);
            p.setElo(plugin.getConfig().getInt("rating.initial-elo", 1200));
            storage.save(p);
            cache.put(uuid, p);
        } else if (name != null && !name.isEmpty()) {
            p.setLastKnownName(name);
        }
        return p;
    }

    public PlayerProfile byName(String name){
        Player on = Bukkit.getPlayerExact(name);
        if (on != null) return getProfile(on.getUniqueId(), on.getName());
        OfflinePlayer off = Bukkit.getOfflinePlayer(name);
        if (off != null && off.getUniqueId() != null) return getProfile(off.getUniqueId(), off.getName());
        return null;
    }

    public void saveProfile(PlayerProfile p){ storage.save(p); cache.put(p.getUuid(), p); }

    public Change recordResult(Player winner, Player loser, boolean draw){
        PlayerProfile wp = getProfile(winner.getUniqueId(), winner.getName());
        PlayerProfile lp = getProfile(loser.getUniqueId(), loser.getName());
        int a0 = wp.getElo(), b0 = lp.getElo();
        EloCalculator.Result r = draw
                ? EloCalculator.calculate(plugin, a0, b0, EloCalculator.Outcome.DRAW)
                : EloCalculator.calculate(plugin, a0, b0, EloCalculator.Outcome.WIN);
        if (draw){ wp.addDraw(); lp.addDraw(); } else { wp.addWin(); lp.addLoss(); }
        wp.setElo(r.newA); lp.setElo(r.newB);
        long now=System.currentTimeMillis(); wp.setLastMatchAt(now); lp.setLastMatchAt(now);
        saveProfile(wp); saveProfile(lp);
        Change change = new Change(wp.getUuid(), wp.getLastKnownName(), a0, r.newA, lp.getUuid(), lp.getLastKnownName(), b0, r.newB, draw);
        afterChange(change);
        return change;
    }

    public Change recordResultNames(String winner, String loser, boolean draw){
        PlayerProfile wp = byName(winner);
        PlayerProfile lp = byName(loser);
        if (wp == null || lp == null) return null;
        int a0 = wp.getElo(), b0 = lp.getElo();
        EloCalculator.Result r = draw
                ? EloCalculator.calculate(plugin, a0, b0, EloCalculator.Outcome.DRAW)
                : EloCalculator.calculate(plugin, a0, b0, EloCalculator.Outcome.WIN);
        if (draw){ wp.addDraw(); lp.addDraw(); } else { wp.addWin(); lp.addLoss(); }
        wp.setElo(r.newA); lp.setElo(r.newB);
        long now=System.currentTimeMillis(); wp.setLastMatchAt(now); lp.setLastMatchAt(now);
        saveProfile(wp); saveProfile(lp);
        Change change = new Change(wp.getUuid(), wp.getLastKnownName(), a0, r.newA, lp.getUuid(), lp.getLastKnownName(), b0, r.newB, draw);
        afterChange(change);
        return change;
    }

    private void afterChange(Change c){
        // Messages
        boolean bc = plugin.getConfig().getBoolean("ui.result-broadcast-enable", true);
        String wMsg = plugin.getConfig().getString("ui.result-winner");
        String lMsg = plugin.getConfig().getString("ui.result-loser");
        String d1Msg = plugin.getConfig().getString("ui.result-draw-p1");
        String d2Msg = plugin.getConfig().getString("ui.result-draw-p2");
        String bcMsg = plugin.getConfig().getString("ui.result-broadcast");

        if (c.draw) {
            Player p1 = Bukkit.getPlayer(c.wId);
            Player p2 = Bukkit.getPlayer(c.lId);
            if (p1 != null && d1Msg != null) p1.sendMessage(Util.color(d1Msg
                    .replace("{p1}", c.wName).replace("{delta1}", String.valueOf(c.deltaW))
                    .replace("{old1}", String.valueOf(c.oldW)).replace("{new1}", String.valueOf(c.newW))));
            if (p2 != null && d2Msg != null) p2.sendMessage(Util.color(d2Msg
                    .replace("{p2}", c.lName).replace("{delta2}", String.valueOf(c.deltaL))
                    .replace("{old2}", String.valueOf(c.oldL)).replace("{new2}", String.valueOf(c.newL))));
        } else {
            Player w = Bukkit.getPlayer(c.wId);
            Player l = Bukkit.getPlayer(c.lId);
            if (w != null && wMsg != null) w.sendMessage(Util.color(wMsg
                    .replace("{winner}", c.wName).replace("{deltaW}", String.valueOf(c.deltaW))
                    .replace("{oldW}", String.valueOf(c.oldW)).replace("{newW}", String.valueOf(c.newW))));
            if (l != null && lMsg != null) l.sendMessage(Util.color(lMsg
                    .replace("{loser}", c.lName).replace("{deltaL}", String.valueOf(c.deltaL))
                    .replace("{oldL}", String.valueOf(c.oldL)).replace("{newL}", String.valueOf(c.newL))));
            if (bc && bcMsg != null) {
                Bukkit.broadcastMessage(Util.color(bcMsg
                        .replace("{winner}", c.wName).replace("{loser}", c.lName)
                        .replace("{oldW}", String.valueOf(c.oldW)).replace("{newW}", String.valueOf(c.newW))
                        .replace("{oldL}", String.valueOf(c.oldL)).replace("{newL}", String.valueOf(c.newL))));
            }
        }

        // Return to original
        plugin.sessions().completeAndReturn(c.wId, c.lId);

        // Clear battle state
        BattleState.clear(c.wId, c.lId);

        // End commands
        for (String raw : plugin.getConfig().getStringList("match.on-match-ended-commands")) {
            String cmd = raw.replace("{winner}", c.wName).replace("{loser}", c.lName)
                    .replace("{oldW}", String.valueOf(c.oldW)).replace("{newW}", String.valueOf(c.newW))
                    .replace("{oldL}", String.valueOf(c.oldL)).replace("{newL}", String.valueOf(c.newL))
                    .replace("{deltaW}", String.valueOf(c.deltaW)).replace("{deltaL}", String.valueOf(c.deltaL));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Util.color(cmd));
        }
    }

    public Collection<PlayerProfile> getAllProfiles(){ return cache.values(); }


    public void clearBattleStateByNames(String a, String b){
        org.bukkit.entity.Player pa = org.bukkit.Bukkit.getPlayerExact(a);
        org.bukkit.entity.Player pb = org.bukkit.Bukkit.getPlayerExact(b);
        if (pa!=null || pb!=null){
            java.util.List<java.util.UUID> ids = new java.util.ArrayList<>();
            if (pa!=null) ids.add(pa.getUniqueId());
            if (pb!=null) ids.add(pb.getUniqueId());
            if (!ids.isEmpty()) BattleState.clear(ids.toArray(new java.util.UUID[0]));
        }
    }
}
