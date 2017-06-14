package me.jetp250.goapimpl.commands;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.jetp250.goapimpl.general.Village;
import me.jetp250.goapimpl.utilities.MathHelper;

public class VillageCommand implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <list|residents> OR /" + label + " tp <id|NEAREST>");
			return true;
		}
		final String sbcmd = args[0].toLowerCase();
		switch (sbcmd) {
			case "list": {
				final StringBuilder builder = new StringBuilder(ChatColor.GREEN + "Villages found:");
				final Village[] villages = Village.getVillages().toArray(new Village[Village.getVillages().size()]);
				Arrays.sort(villages, (o1, o2) -> o1.getId() - o2.getId());
				for (final Village village : villages) {
					builder.append("\n- \u00a7aVillage #" + village.getId() + " at " + village.getX() + ", " + village.getZ());
				}
				sender.sendMessage(builder.toString());
				return true;
			}
			case "residents": {
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "Usage: /" + label + " residents <id>");
					return true;
				}
				final int id = MathHelper.fastParseInt(args[1]) - 1;
				final Village village = Village.getById(id);
				if (village != null) {
					sender.sendMessage(ChatColor.GREEN + "Village #" + id + " (at " + village.getX() + ", " + village.getZ()
							+ ") has " + village.getVillagers().size() + " residents.");
				} else {
					sender.sendMessage(ChatColor.RED + "Village #" + id + " not found! Please choose a number between 1 and "
							+ (Village.getVillages().size() + 1));
				}
				return true;
			}
			case "tp": {
				if (!(sender instanceof Player)) {
					sender.sendMessage("You need to be a player to use this command!");
					return false;
				}
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "Usage: /" + label + " tp <id>");
					return true;
				}
				final Player player = (Player) sender;
				final Village village;
				final boolean nearest;
				if (nearest = args[1].equalsIgnoreCase("NEAREST")) {
					final Location ploc = player.getLocation();
					village = Village.getNearestVillage(ploc.getX(), ploc.getZ());
				} else {
					village = Village.getById(MathHelper.fastParseInt(args[1]) - 1);
				}
				if (village != null) {
					player.teleport(new Location(village.getWorld().getWorld(), village.getX(), village.getWorld().c(village.getX(), village.getZ()), village.getZ()));
					sender.sendMessage(ChatColor.GREEN + "Teleporting to village #" + village.getId() + " (" + village.getX()
							+ ", " + village.getWorld().c(village.getX(), village.getZ()) + ", " + village.getZ() + ")");
				} else if (nearest) {
					sender.sendMessage(ChatColor.RED + "No villages found :(");
				} else {
					sender.sendMessage(ChatColor.RED + "Village '" + args[1] + "' not found :(");
				}
			}
		}
		return true;
	}

}
