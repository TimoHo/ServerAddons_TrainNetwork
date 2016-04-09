package me.tmods.serveraddons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.tmods.serveraddons.trainnetwork.Cart;
import me.tmods.serveraddons.trainnetwork.NetworkListener;
import me.tmods.serveraddons.trainnetwork.Train;
import me.tmods.serverutils.Methods;
public class TrainNetwork extends JavaPlugin{
	File file = new File("plugins/TModsServerUtils","trains.yml");
	public FileConfiguration traincfg = YamlConfiguration.loadConfiguration(file);
	public static List<Train> trains = new ArrayList<Train>();
	public static Plugin getThis() {
		return Bukkit.getPluginManager().getPlugin("ServerAddons_TrainNetwork");
	}
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new NetworkListener(), this);
		if (traincfg.getConfigurationSection("trains") != null) {
			if (traincfg.getConfigurationSection("trains").getKeys(false).size() > 0) {
				for (String s:traincfg.getConfigurationSection("trains").getKeys(false)) {
					trains.add(Train.createFromConfig(traincfg, "trains." + s));
				}
			}
		}
		if (trains.size() > 0) {
			for (Train t:trains) {
				t.applyBreak();
			}
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				if (trains.size() > 0) {
					for (Train t:trains) {
						t.realign();
						if (t.getNextDepartureTime() <= 20) {
							t.depart();
						}
					}
				}
			}
		}, 10, 10);
	}
	
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		if (trains.size() > 0) {
			for (Train t:trains) {
				t.unload();
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You're not a player!");
			return true;
		}
		if (!sender.hasPermission("ServerAddons.train")) {
			sender.sendMessage(Methods.getLang("permdeny"));
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("getTime")) {
			sender.sendMessage("Time: " + ((Player) sender).getWorld().getTime());
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("addTrain")) {
			if (args.length != 2) {
				return false;
			}
			if (Integer.valueOf(args[0]) == null || Integer.valueOf(args[0]) < 1) {
				sender.sendMessage("Please specify a number of carts!");
				return true;
			}
			List<Cart> carts = new ArrayList<Cart>();
			for (int i = 0; i < Integer.valueOf(args[0]);i++) {
				carts.add(new Cart(((Player) sender).getWorld().spawn(((Player) sender).getLocation(), Minecart.class)));
			}
			Train t = new Train(carts,new HashMap<Location,Integer>(),Methods.getItemInHand((Player) sender),args[1],((Player) sender).getLocation());
			t.realign();
			t.toConfig(traincfg,"trains." + args[1]);
			try {
				traincfg.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			trains.add(t);
			sender.sendMessage("train created!");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("removeTrain")) {
			if (args.length != 1) {
				return false;
			}
			if (trains.size() > 0) {
				for (Train t:trains) {
					if (t.getName().equalsIgnoreCase(args[0])) {
						t.unload();
						trains.remove(t);
						traincfg.set("trains." + args[0], null);
						break;
					}
				}
				try {
					traincfg.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				sender.sendMessage("train removed!");
			} else {
				sender.sendMessage("there are no trains yet!");
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("addstation")) {
			if (args.length != 2) {
				return false;
			}
			for (int i = 0;i < trains.size();i++) {
				if (trains.get(i).getName().equalsIgnoreCase(args[0])) {
					trains.set(i, trains.get(i).addStation(((Player) sender).getLocation(), Integer.valueOf(args[1])));
				}
			}
			sender.sendMessage("done!");
			return true;
		}
		/*
		if (cmd.getName().equalsIgnoreCase("manageStations")) {
			if (args.length != 1) {
				return false;
			}
			Train t = null;
			for (int i = 0;i < trains.size();i++) {
				if (trains.get(i).getName().equalsIgnoreCase(args[0])) {
					t = trains.get(i);
					break;
				}
			}
			Inventory i = Bukkit.createInventory(null, 27, "Stations of: " + args[0]);
			for (Location l:t.getStations().keySet()) {
				ItemStack istack = new ItemStack(Material.TORCH);
				ItemMeta meta = istack.getItemMeta();
				meta.setDisplayName("Departure: " + t.getStations().get(l));
				List<String> lore = new ArrayList<String>();
				lore.add("X: " + l.getBlockX());
				lore.add("Y: " + l.getBlockY());
				lore.add("Z: " + l.getBlockZ());
				meta.setLore(lore);
				istack.setItemMeta(meta);
				i.addItem(istack);
			}
			((Player) sender).openInventory(i);
			return true;
		}
		*/
		if (cmd.getName().equalsIgnoreCase("organizeStations")) {
			if (args.length != 1) {
				return false;
			}
			for (int i = 0;i < trains.size();i++) {
				if (trains.get(i).getName().equalsIgnoreCase(args[0])) {
					Train t = trains.get(i);
					HashMap<Location,Integer> unsorted = t.getStations();
					HashMap<Location,Integer> sorted = new HashMap<Location,Integer>();
					for (int i1 = 0;i1 < t.getStations().size();i1++) {
						if (unsorted.size() > 0) {
							int min = 999999999;
							for (Integer dep:unsorted.values()) {
								if (dep < min) {
									min = dep;
								}
							}
							Location minloc = null;
							for (Location l:unsorted.keySet()) {
								if (unsorted.get(l).equals(min)) {
									minloc = l;
									break;
								}
							}
							unsorted.remove(minloc);
							sorted.put(minloc, min);
						} else {
							break;
						}
					}
					t = t.setStations(sorted);
					trains.set(i, t);
				}
			}
			sender.sendMessage("done!");
			return true;
		}
		return false;
	}
}
