package com.bmbecker.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bmbecker.plugin.JacksaFactions;
import com.bmbecker.plugin.objects.Faction;
import com.bmbecker.plugin.utilities.FactionUtilities;

// IDEA FOR REFACTOR: Have it so it gets UUID on execution and then make functions handle UUID

public class FactionCommands implements CommandExecutor {
	
	

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		
		// 
		if (cmd.getName().equalsIgnoreCase("/faction")) {
			if (args.length == 1) { // Faction command has one argument (create, leave)
				if (args[0].equalsIgnoreCase("leave")) { // Player wants to leave their faction
					if (!FactionUtilities.inFaction(player)) { // Player is not in a faction
						return true;
					}
					
					FactionUtilities.factions.get(FactionUtilities.getFactionIndex(player)).removeMember(player);
				}
			} else if (args.length == 2) { // Faction command has two arguments (invite [player])
				if (args[0].equalsIgnoreCase("create")) { // Player creates new faction
					if (FactionUtilities.inFaction(player)) { // Player is already in a faction
						return true;
					}
					
					FactionUtilities.factions.add(new Faction(player, args[1]));
					
				} else if (args[0].equalsIgnoreCase("invite")) { // Player invites other player to faction
					Player invitee = Bukkit.getPlayerExact(args[1]); // get player specified in invite command
					
					if(invitee == null) { // Player not found
						return true;
					}
					
					if (FactionUtilities.inFaction(invitee)) { // Player already in party
						return true;
					}
					
					// TODO: SEND PROMPT TO INVITEE
					
				}
			}
		}
		
		
		
		return true;
	}
	
	

}
