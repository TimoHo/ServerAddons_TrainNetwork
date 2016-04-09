package me.tmods.serveraddons.trainnetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.tmods.serveraddons.TrainNetwork;
import me.tmods.serverutils.Methods;

public class Train {
	private List<Cart> carts;
	private HashMap<Location,Integer> stations;
	private ItemStack ticket;
	private String name;
	private Location start;
	private boolean moving = false;
	public Train(List<Cart> carts,HashMap<Location,Integer> stations,ItemStack ticket,String name,Location start) {
		this.carts = carts;
		this.stations = stations;
		this.ticket = ticket;
		this.name = name;
		this.start = start;
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
	public Integer getNextDepartureTime() {
		int nextTime = 999999999;
		if (this.stations.size() > 0) {
			for (Location loc:this.stations.keySet()) {
				if ((this.stations.get(loc) - start.getWorld().getTime()) > 0) {
					if ((this.stations.get(loc) - start.getWorld().getTime()) < nextTime) {
						nextTime = (int) (this.stations.get(loc) - start.getWorld().getTime());
					}
				}
			}
			return nextTime;
		}
		return -1;
	}
	public World getWorld() {
		return start.getWorld();
	}
	public Location getStationOfDepTime(Integer departureTime) {
		if (stations.size() > 0) {
			for (Location l:stations.keySet()) {
				if (stations.get(l).equals(departureTime)) {
					return l;
				}
			}
		}
		return null;
	}
	public HashMap<Location,Integer> getStations() {
		return stations;
	}
	public Train addStation(Location s,Integer i) {
		this.stations.put(s,i);
		return this;
	}
	public Train removeStation(Location s) {
		this.stations.remove(s);
		return this;
	}
	public Train setStations(HashMap<Location,Integer> s) {
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
	public void toConfig(FileConfiguration c,String path) {
		c.set(path + ".start", Serializer.serializeLocation(this.start));
		c.set(path + ".carts", this.carts.size());
		c.set(path + ".ticket", this.ticket);
		c.set(path + ".name", this.name);
		c.set(path + ".stations", null);
		if (this.stations.size() > 0) {
			for (Location l:stations.keySet()) {
				c.set(path + ".stations." + Serializer.serializeLocation(l), stations.get(l));
			}
		}
	}
	public static Train createFromConfig(FileConfiguration c,String path) {
		Location loc = Serializer.deserializeLocation(c.getString(path + ".start"));
		Integer cartAmount = c.getInt(path + ".carts");
		ItemStack ticket = c.getItemStack(path + ".ticket");
		HashMap<Location,Integer> stations = new HashMap<Location,Integer>();
		String name = c.getString(path + ".name");
		if (c.getConfigurationSection(path + ".stations") != null) {
			if (c.getConfigurationSection(path + ".stations").getKeys(false).size() > 0) {
				for (String s:c.getConfigurationSection(path + ".stations").getKeys(false)) {
					stations.put(Serializer.deserializeLocation(s), c.getInt(path + ".stations." + s));
				}
			}
		}
		List<Cart> carts = new ArrayList<Cart>();
		for (int i = 0;i < cartAmount;i++) {
			carts.add(new Cart(loc.getWorld().spawn(loc, Minecart.class)));
		}
		Train t = new Train(carts, stations, ticket,name,loc);
		t.realign();
		return t;
	}
	public boolean exists() {
		for (Cart c:this.carts) {
			if (!c.exists()) {
				return false;
			}
		}
		return true;
	}
	public void unload() {
		for (Cart c:this.carts) {
			if (c.exists()) {
				c.unload();
			}
		}
	}
	public void respawn() {
		for (Cart c:carts) {
			c.reload(start);
		}
		realign();
	}
	public String getName() {
		return this.name;
	}
	public void applyBreak() {
		for (Cart c:carts) {
			c.getMinecart().setVelocity(new Vector(0,0,0));
		}
	}
	public void depart() {
		if (!moving) {
			moving = true;
			final Integer[] task = new Integer[1];
			task[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(TrainNetwork.getThis(), new Runnable() {
				@Override
				public void run() {
					Location nextstat = getNextStation();
					if (nextstat == null) {
						applyBreak();
						Bukkit.getScheduler().cancelTask(task[0]);
					}
					if (carts.get(0).getMinecart().getLocation().distance(nextstat) < 5) {
						applyBreak();
						Bukkit.getScheduler().cancelTask(task[0]);
					} else {
						if (exists()) {
							for (Cart c:carts) {
								c.boost();
							}
						} else {
							unload();
							respawn();
						}
					}
				}
			}, 10, 10);
		}
	}
	public Location getNextStation() {
		int nextTime = 999999999;
		int departureTime = -1;
		if (this.stations.size() > 0) {
			for (Location loc:this.stations.keySet()) {
				if ((this.stations.get(loc) - start.getWorld().getTime()) > 20) {
					if ((this.stations.get(loc) - start.getWorld().getTime()) < nextTime) {
						nextTime = (int) (this.stations.get(loc) - start.getWorld().getTime());
					}
				}
			}
			departureTime = nextTime;
		}
		if(departureTime == -1) {
			return null;
		}
		for (Location l:stations.keySet()) {
			if (stations.get(l).equals(departureTime)) {
				return l;
			}
		}
		return null;
	}
}
