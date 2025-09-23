package com.minkang.ultimate.pixelrating;
import org.bukkit.Bukkit; import org.bukkit.Location; import org.bukkit.World; import org.bukkit.configuration.ConfigurationSection;
public class Arena {
  private final String id; private boolean enabled=false; private Endpoint p1,p2;
  public Arena(String id){this.id=id;}
  public String getId(){return id;} public boolean isEnabled(){return enabled;} public void setEnabled(boolean e){enabled=e;}
  public Endpoint getP1(){return p1;} public Endpoint getP2(){return p2;}
  public void setP1(Location l){p1=Endpoint.of(l);} public void setP2(Location l){p2=Endpoint.of(l);}
  public static class Endpoint{ public final String world; public final double x,y,z; public final float yaw,pitch;
    private Endpoint(String w,double x,double y,double z,float yaw,float pitch){this.world=w;this.x=x;this.y=y;this.z=z;this.yaw=yaw;this.pitch=pitch;}
    public static Endpoint of(Location l){ if(l==null||l.getWorld()==null) return null; return new Endpoint(l.getWorld().getName(),l.getX(),l.getY(),l.getZ(),l.getYaw(),l.getPitch()); }
    public Location toLocation(){ World w=Bukkit.getWorld(world); if(w==null) return null; return new Location(w,x,y,z,yaw,pitch); } }
  public void toConfig(ConfigurationSection s){ s.set("enabled",enabled);
    if(p1!=null){ ConfigurationSection a=s.createSection("p1"); a.set("world",p1.world); a.set("x",p1.x); a.set("y",p1.y); a.set("z",p1.z); a.set("yaw",p1.yaw); a.set("pitch",p1.pitch); }
    if(p2!=null){ ConfigurationSection a=s.createSection("p2"); a.set("world",p2.world); a.set("x",p2.x); a.set("y",p2.y); a.set("z",p2.z); a.set("yaw",p2.yaw); a.set("pitch",p2.pitch); } }
  public static Arena fromConfig(String id, ConfigurationSection s){ Arena a=new Arena(id); a.enabled=s.getBoolean("enabled",false);
    if(s.isConfigurationSection("p1")){ ConfigurationSection c=s.getConfigurationSection("p1"); if(c.getString("world")!=null) a.p1=new Endpoint(c.getString("world"),c.getDouble("x"),c.getDouble("y"),c.getDouble("z"),(float)c.getDouble("yaw"),(float)c.getDouble("pitch")); }
    if(s.isConfigurationSection("p2")){ ConfigurationSection c=s.getConfigurationSection("p2"); if(c.getString("world")!=null) a.p2=new Endpoint(c.getString("world"),c.getDouble("x"),c.getDouble("y"),c.getDouble("z"),(float)c.getDouble("yaw"),(float)c.getDouble("pitch")); }
    return a; }
}