package com.minkang.ultimate.pixelrating;

public final class ProtocolHook {
    private final UltimatePixelmonRatingPlugin plugin;
    public ProtocolHook(UltimatePixelmonRatingPlugin plugin){ this.plugin = plugin; }
    public boolean init(){
        plugin.getLogger().info("[UPR] ProtocolLib not on compile path; packet hook disabled (build without PLib).");
        return false;
    }
    public void shutdown(){}
}
