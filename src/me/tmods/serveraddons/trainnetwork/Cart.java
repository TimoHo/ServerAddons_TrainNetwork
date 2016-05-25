package me.tmods.serveraddons.trainnetwork;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

public class Cart {
	private Minecart e;
	private Entity passenger;
	private Location lastTickLoc;
	public Cart(Entity e) throws IllegalArgumentException{
		if (!(e instanceof Minecart)) {
			throw new IllegalArgumentException("Entity must be a Minecart.");
		}
		this.e = (Minecart) e;
		this.e.setSlowWhenEmpty(false);
		if (this.e.getPassenger() != null) {
			this.e.eject();
		}
		lastTickLoc = e.getLocation();
	}
	public void setLastTickLoc(Location loc) {
		this.lastTickLoc = loc;
	}
	public Location getLastTickLoc() {
		return lastTickLoc;
	}
	public boolean exists() {
		if (this.e != null) {
			if (!this.e.isDead()) {
				return true;
			}
		}
		return false;
	}
	public void reload(Location l) {
		this.e = l.getWorld().spawn(l, Minecart.class);
	}
	public void unload() {
		this.e.remove();
		this.e = null;
	}
	public void boost(Location lastpos) {
		Vector v = new Vector(0, 0, 0);
		v.setX(this.e.getLocation().getX() - lastpos.getX());
		v.setY(this.e.getLocation().getY() - lastpos.getY());
		v.setZ(this.e.getLocation().getZ() - lastpos.getZ());
		if (v.getX() < 0) {
			v.setX(Math.min(v.getX(), -1));
		} else {
			v.setX(Math.max(v.getX(), 1));
		}
		if (v.getY() < 0) {
			v.setY(Math.min(v.getY(), -1));
		} else {
			v.setY(Math.max(v.getY(), 1));
		}
		if (v.getZ() < 0) {
			v.setZ(Math.min(v.getZ(), -1));
		} else {
			v.setZ(Math.max(v.getZ(), 1));
		}
		boost(v);
	}
	public void boost(Vector direction) {
		this.e.setVelocity(direction);
	}
	
	public boolean isEmpty() {
		if (this.passenger != null) {
			return true;
		} else {
			return false;
		}
	}
	public Entity getPassenger() {
		return this.passenger;
	}
	public void setPassenger(Entity e) {
		this.passenger = e;
		if (this.e.getPassenger() != null) {
			this.e.eject();
		}
		this.e.setPassenger(e);
	}
	public Minecart getMinecart() {
		return e;
	}
}
