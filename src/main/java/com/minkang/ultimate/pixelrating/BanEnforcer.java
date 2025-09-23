package com.minkang.ultimate.pixelrating;
import org.bukkit.Material;
public class BanEnforcer {
  private final UltimatePixelmonRatingPlugin plugin;
  private final java.util.Set<String> bannedPokemon = new java.util.HashSet<>();
  private final java.util.Set<Material> bannedItems = new java.util.HashSet<>();
  public BanEnforcer(UltimatePixelmonRatingPlugin plugin){ this.plugin=plugin; reload(); }
  public void reload(){
    bannedPokemon.clear(); bannedItems.clear();
    for (String s : plugin.getConfig().getStringList("bans.pokemon")) bannedPokemon.add(s.toLowerCase());
    for (String s : plugin.getConfig().getStringList("bans.items")){ try { bannedItems.add(Material.valueOf(s)); } catch (IllegalArgumentException ignored) {} }
  }
  public boolean isPokemonBanned(String name){ return name!=null && bannedPokemon.contains(name.toLowerCase()); }
  public boolean isItemBanned(Material m){ return m!=null && bannedItems.contains(m); }
  public String bannedPokemonCsv(){ return String.join(",", bannedPokemon); }
  public String bannedItemsCsv(){ return bannedItems.stream().map(Enum::name).collect(java.util.stream.Collectors.joining(",")); }
}