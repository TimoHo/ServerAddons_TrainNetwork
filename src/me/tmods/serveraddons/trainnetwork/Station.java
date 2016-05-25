package me.tmods.serveraddons.trainnetwork;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import me.tmods.api.Serializer;

public class Station {
	private Location loc;
	private Integer dep;
	private Vector dir;
	public Station(Location s, Integer i, Vector direction) {
		this.loc = s;
		this.dep = i;
		this.dir = direction;
	}

	public int getTime() {
		return dep;
	}

	public static Station deserialize(String s) {
		Location loc = Serializer.deserializeLocation(s.split("-=-")[0]);
		Integer dep = Integer.valueOf(s.split("-=-")[1]);
		Vector dir = Serializer.deserializeVector(s.split("-=-")[2]);
		return new Station(loc,dep,dir);
	}

	public String serialize() {
		String s = Serializer.serializeLocation(loc) + "-=-" + dep + "-=-" + Serializer.serializeVector(dir);
		return s;
	}

	public Location getLocation() {
		return loc;
	}
	public Vector getDirection() {
		return dir;
	}

}
