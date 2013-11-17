package net.conriot.sona.mines;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class Commands implements CommandExecutor {
	private MineManager mines;
	
	public Commands(MineManager mines) {
		this.mines = mines;
	}
	
	@SuppressWarnings("deprecation")
	private void add(Player sender, String[] args) {
		// Make an array to hold the add configuration settings
		int[] params = new int[5];
		
		// Verify we have all the required/correct args
		if(args.length > 5) {
			try {
			for(int i = 1; i < 6; i++)
				params[i - 1] = Integer.parseInt(args[i]);
			} catch(NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Could not set player to add mode!");
				return;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Could not set player to add mode!");
			return;
		}
		
		// Switch the player in mine add mode
		this.mines.setAdder(sender.getName(), params);
		
		// Notify they are now in add mode
		sender.sendMessage(ChatColor.GREEN + "You are now adding regen blocks!");
		sender.sendMessage(ChatColor.GREEN + "(" + Material.getMaterial(params[0]).toString() + ":" + params[1] + ", MIN: " + params[2] + ", MAX: " + params[3] + ", REPLACE: " + params[4] + ")");
	}
	
	private void remove(Player sender) {
		// Switch the player in mine removal mode
		this.mines.setRemover(sender.getName());
		
		// Notify they are now in remove mode
		sender.sendMessage(ChatColor.GREEN + "You are now removing regen blocks!");
	}
	
	private void off(Player sender) {
		// Stop the player from adding or removing blocks
		this.mines.clearFromTasks(sender.getName());
		
		// Notify they are no longer adding or removing
		sender.sendMessage(ChatColor.GREEN + "You are no longer adding/removing regen blocks!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length > 0 && sender.isOp() && sender instanceof Player) {
			String subcommand = args[0].toLowerCase();
			switch(subcommand) {
			case "add":
				add((Player)sender, args);
				break;
			case "remove":
				remove((Player)sender);
				break;
			case "off":
				off((Player)sender);
				break;
			}
		}
		// Always return true to prevent default Bukkit messages
		return true;
	}
}
