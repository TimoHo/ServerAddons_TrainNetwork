package me.tmods.serveraddons.trainnetwork;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Serializer {
	public static String serializeLocation(Location l) {
		String s = l.getWorld().getName() + ":::" + l.getX() + ":::" + l.getY() + ":::" + l.getZ() + ":::" + l.getPitch() + ":::" + l.getYaw();
		return s;
	}
	public static Location deserializeLocation(String s) {
		World world = Bukkit.getWorld(s.split(":::")[0]);
		Double x = Double.valueOf(s.split(":::")[1]);
		Double y = Double.valueOf(s.split(":::")[2]);
		Double z = Double.valueOf(s.split(":::")[3]);
		Float pitch = Float.valueOf(s.split(":::")[4]);
		Float yaw = Float.valueOf(s.split(":::")[5]);
		
		Location l = new Location(world,x,y,z);
		l.setPitch(pitch);
		l.setYaw(yaw);
		
		return l;
	}
}
