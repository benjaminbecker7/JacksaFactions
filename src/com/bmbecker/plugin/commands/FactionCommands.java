package com.bmbecker.plugin.commands;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bmbecker.plugin.objects.Faction;
import com.bmbecker.plugin.utilities.FactionUtilities;
import com.bmbecker.plugin.utilities.WorldGuardUtilities;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class FactionCommands implements CommandExecutor {
	
	//TODO: Add claim command that user can use to add chunks to their domain. checks if chunk is in claimedChunks hashset

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		
		// 
		if (cmd.getName().equalsIgnoreCase("faction")) {
			if (args.length == 0) {

				player.sendMessage(ChatColor.GREEN + "JacksaFactions Plugin");
				player.sendMessage(ChatColor.GREEN + "Usage:");
				player.sendMessage("/faction <arg>");
				player.sendMessage(ChatColor.GREEN + "Arguments:");
				
				int factionidx = FactionUtilities.getFactionIndexByPlayer(player);
				
				if (factionidx == -1) {
					player.sendMessage("create <name>: create new faction with name <name>.");
					player.sendMessage("accept <faction>: accept pending invite from faction with name <faction>.");
					player.sendMessage("decline <faction>: decline pending invite from faction with name <faction>.");
				} else {
					player.sendMessage("leave: leave current faction.");
					player.sendMessage("list: list players in current faction.");
					
					if (FactionUtilities.factions.get(factionidx).isLeader(player)) {
						player.sendMessage("appoint <name>: appoint member of faction with name <name> to be your faction's new leader.");
						player.sendMessage("kick <name>: kick member of faction with name <name> from your faction.");
						player.sendMessage("invite <name>: invite player with name <name> to your faction.");
					}
				}

			} else if (args.length == 1) { // Faction command has one argument (leave)
				if (args[0].equalsIgnoreCase("leave")) { // Player wants to leave their faction
					
					int factionidx = FactionUtilities.getFactionIndexByPlayer(player);
					
					if (factionidx == -1) { // Player is not in a faction
						player.sendMessage("You are not in a faction.");
						return true;
					}
					
					if (FactionUtilities.factions.get(factionidx).isLeader(player)) { // if player leaving is the leader, we have to make a new one
						UUID nextLeaderUUID = FactionUtilities.factions.get(factionidx).getNextLeaderUUID();

						FactionUtilities.factions.get(factionidx).setLeaderUUID(nextLeaderUUID);
						FactionUtilities.factions.get(factionidx).removeMember(player);
						
						if (nextLeaderUUID == null) { // if there are no more members in line for leader, destroy the faction
							FactionUtilities.factions.remove(factionidx);
						}
						
						player.sendMessage("You have left your faction.");
						
					} else {
						FactionUtilities.factions.get(factionidx).removeMember(player);
						player.sendMessage("You have left your faction.");
					}
				} else if (args[0].equalsIgnoreCase("list")) {
					int factionidx = FactionUtilities.getFactionIndexByPlayer(player);
					
					if (factionidx == -1) { // Player is not in a faction
						player.sendMessage("You are not in a faction.");
						return true;
					}
					
					Iterator<UUID> memberIterator = FactionUtilities.factions.get(factionidx).memberIterator();
					
					player.sendMessage(ChatColor.GREEN + "Members of your faction:");
					
					while (memberIterator.hasNext()) {
						Player member = Bukkit.getPlayer(memberIterator.next());
						if (FactionUtilities.factions.get(factionidx).isLeader(member)) {
							player.sendMessage(member.getName() + " [leader]");
						} else {
							player.sendMessage(member.getName());
						}
					}
				}
			} else if (args.length == 2) { // Faction command has two arguments (invite [player])
				if (args[0].equalsIgnoreCase("create")) { // Player creates new faction
					
					if (FactionUtilities.inFaction(player)) { // Player is already in a faction
						player.sendMessage("You are already in a faction.");
						return true;
					}
					
					if (args[1].length() > 8) {
						player.sendMessage("Faction names cannot be longer than 8 characters.");
						return true;
					}

					if(!FactionUtilities.nameAvailable(args[1])) {
						player.sendMessage("Faction name " + args[1] + " has already been taken.");
						return true;
					}
					
					FactionUtilities.factions.add(new Faction(player, args[1]));
					player.sendMessage("You have created a new faction with name " + args[1]);
					
				} else if (args[0].equalsIgnoreCase("appoint")) {
					int factionidx = FactionUtilities.getFactionIndexByPlayer(player);
					
					if (factionidx == -1) {
						player.sendMessage("You are not in a faction.");
						return true;
					}
					
					if (!FactionUtilities.factions.get(factionidx).isLeader(player)) {
						player.sendMessage("You are not the leader of your faction.");
						return true;
					}
					
					Player appointee = Bukkit.getPlayerExact(args[1]);
					
					if (appointee == null) {
						player.sendMessage("Player " + args[1] + " not found.");
						return true;
					}
					
					if (!FactionUtilities.factions.get(factionidx).isInFaction(appointee)) {
						player.sendMessage("Player " + args[1] + " is not in your faction.");
						return true;
					}
					
					FactionUtilities.factions.get(factionidx).setLeader(appointee);
					
					player.sendMessage("You have appointed " + args[1] + " as the new leader of your faction.");
					
					appointee.sendMessage("You have been appointed to be the new leader of your factions.");
				} else if (args[0].equalsIgnoreCase("kick")) {
					int factionidx = FactionUtilities.getFactionIndexByPlayer(player);
					
					if (factionidx == -1) {
						player.sendMessage("You are not in a faction.");
						return true;
					}
					
					if (!FactionUtilities.factions.get(factionidx).isLeader(player)) {
						player.sendMessage("You are not the leader of your faction.");
						return true;
					}
					
					Player removee = Bukkit.getPlayerExact(args[1]);
					
					if(removee == null) {
						player.sendMessage("Player " + args[1] + " not found.");
						return true;
					}
					
					if (!FactionUtilities.factions.get(factionidx).isInFaction(removee)) {
						player.sendMessage("Player " + args[1] + " is not in your faction.");
						return true;
					}
					
					FactionUtilities.factions.get(factionidx).removeMember(removee);
					
					player.sendMessage("Player " + args[1] + " has been removed from your faction.");
					
					removee.sendMessage("You have been removed from the faction " + FactionUtilities.factions.get(factionidx).getName());
					
				} else if (args[0].equalsIgnoreCase("invite")) { // Player invites other player to faction
					Player invitee = Bukkit.getPlayerExact(args[1]); // get player specified in invite command
					
					if(invitee == null) { // Player not found
						player.sendMessage("Player " + args[1] + " not found.");
						return true;
					}
					
					if (FactionUtilities.inFaction(invitee)) { // Player already in faction
						player.sendMessage("Player " + args[1] + " is already in a faction.");
						return true;
					}
					
					int factionidx = FactionUtilities.getFactionIndexByPlayer(player);

					if (factionidx == -1) {
						player.sendMessage("You are not in a faction.");
						return true;
					} 
					
					// arraylist lookups by index are essentially on O(1) and maintains data integrity so that's why we're not using assignment
					if (!FactionUtilities.factions.get(factionidx).isLeader(player)) { 
						player.sendMessage("You are not the leader of your faction.");
						return true;
					}
						
					FactionUtilities.factions.get(factionidx).addInvitee(invitee);

					invitee.sendMessage("You have been invited to join the faction " + FactionUtilities.factions.get(factionidx).getName());
					invitee.sendMessage("To accept this invitation, type /factions accept <faction>");
					invitee.sendMessage("To decline this invitation, type /factions decline <faction>");

					player.sendMessage("Invite sent.");
				} else if (args[0].equalsIgnoreCase("accept")) {
					
					int factionidx = FactionUtilities.getFactionIndexByName(args[1]);
					
					if (factionidx != -1) { // Player is already in a faction
						player.sendMessage("You are already in a faction.");
						return true;
					}
					
					

					if (factionidx == -1) {
						player.sendMessage("Faction not found.");
						return true;
					}

					if (!FactionUtilities.factions.get(factionidx).isInvited(player)) {
						player.sendMessage("You have not received an invite from " + args[1]);
						return true;
					}

					player.sendMessage("You have been added to the faction " + FactionUtilities.factions.get(factionidx).getName());
					Bukkit.getPlayer(FactionUtilities.factions.get(factionidx).getLeaderUUID()).sendMessage("Player " + player.getName() + " has accepted your faction invitation.");
				} else if (args[0].equalsIgnoreCase("decline")) {
					
					if (FactionUtilities.inFaction(player)) { // Player is already in a faction
						player.sendMessage("You are already in a faction.");
						return true;
					}
					
					int factionidx = FactionUtilities.getFactionIndexByName(args[1]);
					
					if (factionidx == -1) {
						player.sendMessage("Faction not found.");
						return true;
					}
					
					if (!FactionUtilities.factions.get(factionidx).isInvited(player)) {
						player.sendMessage("You have not received an invite from " + args[1]);
						return true;
					}
					
					FactionUtilities.factions.get(factionidx).removeInvite(player);
					
					player.sendMessage("You have declined the invite from " + FactionUtilities.factions.get(factionidx).getName());
					Bukkit.getPlayer(FactionUtilities.factions.get(factionidx).getLeaderUUID()).sendMessage(player.getName() + " has declined your invitation");
					
				}
			} else if (args.length == 4) {
				
				if (args[0].equalsIgnoreCase("pvp")) { // Command: /factionOp WorldGuard pvp <worldname> <regionname> ~ disable interfaction nohit protections in region
					
					if(!player.isOp()) { // verify player is op
						Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Non-Op player " + player.getName() + " tried to call an Op command.");
						return true;
					}
					
					if (args[1].equalsIgnoreCase("on")) {
						RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
						
						World world = Bukkit.getWorld(args[2]);
						
						if (world == null) {
							player.sendMessage("Could not find world");
							return true;
						}
						
						RegionManager regions = container.get(BukkitAdapter.adapt(world));
						
						if (regions == null) {
							player.sendMessage("Could not find world");
							return true;
						}
						
						ProtectedRegion region = regions.getRegion(args[3]);
						
						if (region == null) {
							player.sendMessage("Could not find region");
							return true;
						}
						
						region.setFlag(WorldGuardUtilities.FACTION_PVP, StateFlag.State.ALLOW);
						
						player.sendMessage("Preventing faction pvp in region " + args[3]);
					} else if (args[1].equalsIgnoreCase("off")) {
						RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
						
						World world = Bukkit.getWorld(args[2]);
						
						if (world == null) {
							player.sendMessage("Could not find world");
							return true;
						}
						
						RegionManager regions = container.get(BukkitAdapter.adapt(world));
						
						if (regions == null) {
							player.sendMessage("Could not find world");
							return true;
						}
						
						ProtectedRegion region = regions.getRegion(args[3]);
						
						if (region == null) {
							player.sendMessage("Could not find region");
							return true;
						}
						
						region.setFlag(WorldGuardUtilities.FACTION_PVP, StateFlag.State.DENY);
						
						player.sendMessage("Preventing faction pvp in region " + args[3]);
					}
				}
			}
		}
		
		
		
		return true;
	}
	
	

}
