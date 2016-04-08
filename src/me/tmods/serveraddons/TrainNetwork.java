package me.tmods.serveraddons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.tmods.serveraddons.trainnetwork.Cart;
import me.tmods.serveraddons.trainnetwork.NetworkListener;
import me.tmods.serveraddons.trainnetwork.Train;
import me.tmods.serverutils.Methods;
public class TrainNetwork extends JavaPlugin{
	File file = new File("plugins/TModsServerUtils","trains.yml");
	public FileConfiguration trains = YamlConfiguration.loadConfiguration(file);
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new NetworkListener(), this);
	}
	
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("addTrain")) {
			if (!sender.hasPermission("ServerAddons.train")) {
				sender.sendMessage(Methods.getLang("permdeny"));
				return true;
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage("You're not a player!");
				return true;
			}
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
			Train t = new Train(carts,new ArrayList<Location>(),Methods.getItemInHand((Player) sender));
			t.realign();
			t.toConfig(trains,"trains." + args[1],((Player) sender).getLocation());
			try {
				trains.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			sender.sendMessage("train created!");
			return true;
		}
		return false;
	}
}
