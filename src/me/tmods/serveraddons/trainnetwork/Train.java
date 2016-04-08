package me.tmods.serveraddons.trainnetwork;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.tmods.serverutils.Methods;

public class Train {
	private List<Cart> carts;
	private List<Location> stations;
	private ItemStack ticket;
	public Train(List<Cart> carts,List<Location> stations,ItemStack ticket) {
		this.carts = carts;
		this.stations = stations;
		this.ticket = ticket;
	}
	public void enter(Player p) {
		if (ticket.isSimilar(Methods.getItemInHand(p))) {
			for (int i = 0;i < carts.size();i++) {
				if (carts.get(i).isEmpty()) {
					carts.get(i).setPassenger(p);
				}
			}
		} else {
			p.sendMessage("this is the wrong ticket!");
		}
		p.sendMessage("the train is full!");
	}
	public Train addStation(Location s) {
		this.stations.add(s);
		return this;
	}
	public Train removeStation(Location s) {
		this.stations.remove(s);
		return this;
	}
	public Train setStations(List<Location> s) {
		this.stations = s;
		return this;
	}
	public void realign() {
		if (carts.size() > 0) {
			for (int i = 0;i < carts.size();i++) {
				Cart c = carts.get(i);
				if (i != 0) {
					c.getMinecart().teleport(carts.get(i-1).getMinecart().getLocation().add(carts.get(i).getMinecart().getLocation().getDirection().multiply(-1)));
				}
			}
		}
	}
	public void toConfig(FileConfiguration c,String path, Location start) {
		c.set(path + ".start", Serializer.serializeLocation(start));
		c.set(path + ".carts", this.carts.size());
		c.set(path + ".ticket", this.ticket);
		List<String> stations = new ArrayList<String>();
		if (this.stations.size() > 0) {
			for (Location l:this.stations) {
				stations.add(Serializer.serializeLocation(l));
			}
		}
		c.set(path + ".stations", stations);
	}
	public static Train createFromConfig(FileConfiguration c,String path) {
		Location loc = Serializer.deserializeLocation(c.getString(path + ".start"));
		Integer cartAmount = c.getInt(path + ".carts");
		ItemStack ticket = c.getItemStack(path + ".ticket");
		List<Location> stations = new ArrayList<Location>();
		Train t = new Train(carts, stations, ticket);
	}
}
