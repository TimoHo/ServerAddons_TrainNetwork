package me.tmods.serveraddons.trainnetwork;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

public class Cart {
	private Minecart e;
	private Entity passenger;
	public Cart(Entity e) throws IllegalArgumentException{
		if (!(e instanceof Minecart)) {
			throw new IllegalArgumentException("Entity must be a Minecart.");
		}
		this.e = (Minecart) e;
		this.e.setSlowWhenEmpty(false);
		if (this.e.getPassenger() != null) {
			this.e.eject();
		}
	}
	
	public void boost() {
		this.e.setVelocity(this.e.getLocation().getDirection().multiply(2));
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
