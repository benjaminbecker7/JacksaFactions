package com.bmbecker.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bmbecker.plugin.utilities.WorldGuardUtilities;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class AdminCommands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		if(!player.isOp()) {
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Non-Op player " + player.getName() + " tried to call an Op command.");
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("factionOp")) { // faction op commands
			if (args.length == 5) {
				if (args[0].equalsIgnoreCase("WorldGuard")) { // factionOp commands with WorldGuard can be scaled
					if (args[1].equalsIgnoreCase("pvp")) { // Command: /factionOp WorldGuard pvp <worldname> <regionname> ~ disable interfaction nohit protections in region
						if (args[2].equalsIgnoreCase("on")) {
							RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
							RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(args[3])));
							
							if (regions == null) {
								player.sendMessage("Could not find world");
								return true;
							}
							
							regions.getRegion(args[4]).setFlag(WorldGuardUtilities.FACTION_PVP, StateFlag.State.ALLOW);
							
							player.sendMessage("Allowing faction pvp in region " + args[4]);
						} else if (args[2].equalsIgnoreCase("off")) {
							RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
							RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(args[3])));
							
							if (regions == null) {
								player.sendMessage("Could not find world");
								return true;
							}
							
							regions.getRegion(args[4]).setFlag(WorldGuardUtilities.FACTION_PVP, StateFlag.State.DENY);
							
							player.sendMessage("Preventing faction pvp in region " + args[4]);
						}
					}
				}
			}
		}
		
		
		return true;
	}

}
