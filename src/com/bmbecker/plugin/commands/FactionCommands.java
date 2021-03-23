package com.bmbecker.plugin.commands;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.bmbecker.plugin.objects.ClaimedChunk;
import com.bmbecker.plugin.utilities.SQLUtilities;
import com.bmbecker.plugin.utilities.WorldGuardUtilities;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class FactionCommands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			
			return true;
			
		}
		
		Player player = (Player) sender;
		
		// Overarching command
		if (cmd.getName().equalsIgnoreCase("faction")) {
			switch(args.length) {
				case 0:
					return sendCommandInfo(player);
				case 1:
					switch(args[0].toLowerCase()) {
						case "leave":
							return leave(player);
						case "list":
							return list(player);
						case "claim":
							return claim(player);
						case "unclaim":
							return unclaim(player);
						case "accept":
							return accept(player);
						case "decline":
							return decline(player);
						default:
							return sendArgsNotRecognized(player);
					}
				case 2:
					switch(args[0].toLowerCase()) {
						case "create":
							return create(player, args[1]);
						case "appoint":
							return appoint(player, args[1]);
						case "kick":
							return kick(player, args[1]);
						case "invite":
							return invite(player, args[1]);
						case "get":
							return get(player, args[1]);
						default:
							return sendArgsNotRecognized(player);
					}
				case 4:
					switch(args[0].toLowerCase()) {
						case "pvp":
							return setPvpFlag(player, args);
						case "claimable":
							return setClaimableFlag(player, args);
						default:
							return sendArgsNotRecognized(player);
					}
				default:
					return sendArgsNotRecognized(player);
			}
		}
		
		return true;
	}
	
	private boolean sendArgsNotRecognized(Player player) {
		player.sendMessage(ChatColor.RED + "Arguments not recognized for '/faction' command. Run '/faction' for list of available commands.");
		return true;
	}
	
	private boolean sendCommandInfo(Player player) {
		player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "JacksaFactions Plugin");
		player.sendMessage(ChatColor.GREEN + "Usage:");
		player.sendMessage("/faction <arg>");
		player.sendMessage(ChatColor.GREEN + "Arguments:");
		
		String faction = SQLUtilities.getPlayerFaction(player);
		
		if (faction == null) {
			player.sendMessage(ChatColor.AQUA + "create <name>" + ChatColor.WHITE + ": create new faction with name <name>. Names can be a max of 8 characters.");
			player.sendMessage(ChatColor.AQUA + "accept <faction>" + ChatColor.WHITE + ": accept pending invite from faction with name <faction>.");
			player.sendMessage(ChatColor.AQUA + "decline <faction>" + ChatColor.WHITE + ": decline pending invite from faction with name <faction>.");
		} else {
			player.sendMessage(ChatColor.AQUA + "leave" + ChatColor.WHITE + ": leave current faction.");
			player.sendMessage(ChatColor.AQUA + "list" + ChatColor.WHITE + ": list players in current faction.");
			
			if (SQLUtilities.isFactionLeader(player)) {
				player.sendMessage(ChatColor.AQUA + "appoint <name>" + ChatColor.WHITE + ": appoint member of faction with name <name> to be your faction's new leader.");
				player.sendMessage(ChatColor.AQUA + "kick <name>" + ChatColor.WHITE + ": kick member of faction with name <name> from your faction.");
				player.sendMessage(ChatColor.AQUA + "invite <name>" + ChatColor.WHITE + ": invite player with name <name> to your faction.");
				player.sendMessage(ChatColor.AQUA + "claim" + ChatColor.WHITE + ": claim the chunk you are in for your faction.");
				player.sendMessage(ChatColor.AQUA + "unclaim" + ChatColor.WHITE + ": remove the chunk you are in from your faction's domain.");
			}
		}
		
		player.sendMessage(ChatColor.AQUA + "get <name>" + ChatColor.WHITE + ": gets the name of player <name>'s faction.");
		
		return true;
	}
	
	private boolean leave(Player player) {
		String faction = SQLUtilities.getPlayerFaction(player);
		
		if (faction == null) { // Player is not in a faction
			player.sendMessage(ChatColor.RED + "You are not in a faction.");
			return true;
		}
		
		SQLUtilities.removeFactionMember(faction, player);
		
		player.sendMessage("You have left your faction.");
		
		return true;
	}
	
	private boolean list(Player player) {
		String faction = SQLUtilities.getPlayerFaction(player);
		
		if (faction == null) { // Player is not in a faction
			player.sendMessage(ChatColor.RED + "You are not in a faction.");
			return true;
		}
							
		player.sendMessage(ChatColor.GREEN + "Members of your faction:");
		
		for (String name : SQLUtilities.getFactionMembers(faction)) {
			Player member = Bukkit.getPlayer(name);
			if (SQLUtilities.isFactionLeader(member)) {
				player.sendMessage(name + " [leader]");
			} else {
				player.sendMessage(name);
			}
		}
		
		return true;
	}
	
	private boolean claim(Player player) {
		String faction = SQLUtilities.getPlayerFaction(player);
		
		if (faction == null) {
			player.sendMessage(ChatColor.RED + "You are not in a faction.");
			return true;
		}
		
		if (!SQLUtilities.isFactionLeader(player)) {
			player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
			return true;
		}
		

		// CHECK IF PLAYER IS IN A FACTION_CLAIMABLE ZONE. IF NO, PLAYER CANNOT CLAIM
		Chunk currChunk = player.getLocation().getChunk();
		int bx = currChunk.getX() << 4;
		int bz = currChunk.getZ() << 4;
		BlockVector3 pt1 = BlockVector3.at(bx, 0, bz);
		BlockVector3 pt2 = BlockVector3.at(bx + 15, 256, bz + 15);
		ProtectedCuboidRegion region = new ProtectedCuboidRegion("ID", pt1, pt2);
		RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
		
		ApplicableRegionSet regionSet = regions.getApplicableRegions(region);
		Iterator<ProtectedRegion> appRegions = regionSet.iterator();
		
		if (appRegions.hasNext() && appRegions.next().getFlag(WorldGuardUtilities.FACTION_CLAIMABLE) == StateFlag.State.DENY) {
			player.sendMessage(ChatColor.RED + "This chunk is in an unclaimable region.");
			return true;
		}
		
		if (SQLUtilities.getNumClaims(faction) == SQLUtilities.getMaxClaims(faction)) {
			player.sendMessage(ChatColor.RED + "Your faction has reached its max number of chunk claims. Add more members to increase the amount you can claim.");
			return true;
		}
		
		
		ClaimedChunk dummyChunk = ClaimedChunk.parseClaimedChunk(currChunk);
		
		if (SQLUtilities.chunkClaimed(dummyChunk)) {
			player.sendMessage(ChatColor.RED + "This chunk has already been claimed.");
			return true;
		}
		
		SQLUtilities.addClaimedChunk(faction, dummyChunk);
		
		player.sendMessage("You have claimed chunk in world \"" + dummyChunk.getWorld() + "\" at position X: " + dummyChunk.getX() + ", Z: " + dummyChunk.getZ() + " for your faction.");
	
		return true;
	}
	
	private boolean unclaim(Player player) {
		String faction = SQLUtilities.getPlayerFaction(player);
		
		if (faction == null) {
			player.sendMessage(ChatColor.RED + "You are not in a faction.");
			return true;
		}
		
		if (!SQLUtilities.isFactionLeader(player)) {
			player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
			return true;
		}
		
		Chunk currChunk = player.getLocation().getChunk();
		
		ClaimedChunk dummyChunk = ClaimedChunk.parseClaimedChunk(currChunk);
		
		if (!SQLUtilities.chunkOwnedByFaction(faction, dummyChunk)) { 
			player.sendMessage(ChatColor.RED + "Your faction does not own this chunk.");
			return true;
		}
		
		SQLUtilities.removeClaimedChunk(faction, dummyChunk);
		
		player.sendMessage("You have removed chunk in world \"" + dummyChunk.getWorld() + "\" at position X: " + dummyChunk.getX() + ", Z: " + dummyChunk.getZ() + " from your faction.");
	
		return true;
	}
	
	private boolean accept(Player player) {
		String faction = SQLUtilities.getInvite(player);
		
		if (SQLUtilities.inFaction(player)) { // Player is already in a faction
			player.sendMessage(ChatColor.RED + "You are already in a faction.");
			return true;
		}

		if (faction == null) {
			player.sendMessage(ChatColor.RED + "Invite not found.");
			return true;
		}
		
		SQLUtilities.addFactionMember(faction, player);
		
		SQLUtilities.removeInvite(player);

		player.sendMessage(ChatColor.GREEN + "You have been added to the faction " + faction);
		Bukkit.getPlayer(SQLUtilities.getFactionLeader(faction)).sendMessage(ChatColor.GREEN + "Player " + player.getName() + " has accepted your faction invitation.");
		
		return true;
	}
	
	private boolean decline(Player player) {
		String faction = SQLUtilities.getInvite(player);
		
		if (SQLUtilities.inFaction(player)) { // Player is already in a faction
			player.sendMessage(ChatColor.RED + "You are already in a faction.");
			return true;
		}

		if (faction == null) {
			player.sendMessage(ChatColor.RED + "Invite not found.");
			return true;
		}
		
		SQLUtilities.removeInvite(player);

		player.sendMessage("You have declined the invite from " + faction);
		Bukkit.getPlayer(SQLUtilities.getFactionLeader(faction)).sendMessage(player.getName() + " has declined your invitation");
		
		return true;
	}
	
	private boolean create(Player player, String name) {
		switch(SQLUtilities.createFaction(name, player)) {
			case SUCCESS:
				player.sendMessage(ChatColor.GREEN + "You have created a new faction with name " + name);
				return true;
			case ALREADY_IN_FACTION:
				player.sendMessage(ChatColor.RED + "You are already in a faction.");
				return true;
			case NAME_INVALID:
				player.sendMessage(ChatColor.RED + "Faction names can be no longer than 8 characters and cannot contain \"\"\", \";\", or \"'\".");
				return true;
			case NAME_ALREADY_TAKEN:
				player.sendMessage(ChatColor.RED + "Name has already been taken.");
				return true;
			default:
				player.sendMessage(ChatColor.RED + "SQL QUERY ERROR: Please notify server admins.");
				return true;
		}
	}
	
	private boolean appoint(Player player, String appointeeName) {
		String faction = SQLUtilities.getPlayerFaction(player);
		
		if (faction == null) {
			player.sendMessage(ChatColor.RED + "You are not in a faction.");
			return true;
		}
		
		if (!SQLUtilities.isFactionLeader(player)) {
			player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
			return true;
		}
		
		Player appointee = Bukkit.getPlayerExact(appointeeName);
		
		if (appointee == null) {
			player.sendMessage(ChatColor.RED + "Player " + appointeeName + " not found.");
			return true;
		}
		
		if (!SQLUtilities.inFaction(faction, player)) {
			player.sendMessage(ChatColor.RED + "Player " + appointeeName + " is not in your faction.");
			return true;
		}
		
		SQLUtilities.setLeader(faction, player);
		
		player.sendMessage("You have appointed " + appointeeName + " as the new leader of your faction.");
		
		appointee.sendMessage("You have been appointed to be the new leader of your factions.");
		
		return true;
	}
	
	private boolean kick(Player player, String removeeName) {
		String faction = SQLUtilities.getPlayerFaction(player);
		
		if (faction == null) {
			player.sendMessage(ChatColor.RED + "You are not in a faction.");
			return true;
		}
		
		if (!SQLUtilities.isFactionLeader(player)) {
			player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
			return true;
		}
		
		Player removee = Bukkit.getPlayerExact(removeeName);
		
		if(removee == null) {
			player.sendMessage(ChatColor.RED + "Player " + removeeName + " not found.");
			return true;
		}
		
		if (!SQLUtilities.inFaction(faction, removee)) {
			player.sendMessage(ChatColor.RED + "Player " + removeeName + " is not in your faction.");
			return true;
		}
		
		SQLUtilities.removeFactionMember(faction, removee);
		
		player.sendMessage("Player " + removeeName + " has been removed from your faction.");
		
		removee.sendMessage("You have been removed from the faction " + faction);
		
		return true;
	}
	
	private boolean invite(Player player, String inviteeName) {
		Player invitee = Bukkit.getPlayerExact(inviteeName); // get player specified in invite command
		
		if(invitee == null) { // Player not found
			player.sendMessage(ChatColor.RED + "Player " + inviteeName + " not found.");
			return true;
		}
		
		if (SQLUtilities.inFaction(invitee)) { // Player already in faction
			player.sendMessage(ChatColor.RED + "Player " + inviteeName + " is already in a faction.");
			return true;
		}
		
		String faction = SQLUtilities.getPlayerFaction(player);

		if (faction == null) {
			player.sendMessage(ChatColor.RED + "You are not in a faction.");
			return true;
		} 
		
		// arraylist lookups by index are essentially on O(1) and maintains data integrity so that's why we're not using assignment
		if (!SQLUtilities.isFactionLeader(player)) { 
			player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
			return true;
		}
		
		if (SQLUtilities.hasInvite(invitee)) {
			player.sendMessage(ChatColor.RED + "Player already has a pending invite from a faction.");
			return true;
		}
		
		SQLUtilities.addInvite(faction, invitee);

		invitee.sendMessage("You have been invited to join the faction " + faction + ". You may only have one pending invite at a time.");
		invitee.sendMessage("To accept this invitation, type /factions accept <faction>");
		invitee.sendMessage("To decline this invitation, type /factions decline <faction>");

		player.sendMessage("Invite sent.");
		
		return true;
	}
	
	private boolean get(Player player, String name) {
		Player target = Bukkit.getPlayerExact(name); // get player specified in invite command
		
		if(target == null) { // Player not found
			player.sendMessage(ChatColor.RED + "Player " + name + " not found.");
			return true;
		}
		
		String faction = SQLUtilities.getPlayerFaction(target);

		if (faction == null) {
			player.sendMessage(name + " is not in a faction.");
			return true;
		}
		
		player.sendMessage(name + " is in faction with name " + faction + ".");
		
		return true;
	}
	
	private boolean setPvpFlag(Player player, String[] args) {
		if(!player.isOp()) { // verify player is op
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Non-Op player " + player.getName() + " tried to call an Op command.");
			player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
			return true;
		}
		
		if (args[1].equalsIgnoreCase("on")) {
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			
			World world = Bukkit.getWorld(args[2]);
			
			if (world == null) {
				player.sendMessage(ChatColor.RED + "Could not find world");
				return true;
			}
			
			RegionManager regions = container.get(BukkitAdapter.adapt(world));
			
			if (regions == null) {
				player.sendMessage(ChatColor.RED + "Could not find world");
				return true;
			}
			
			ProtectedRegion region = regions.getRegion(args[3]);
			
			if (region == null) {
				player.sendMessage(ChatColor.RED + "Could not find region");
				return true;
			}
			
			region.setFlag(WorldGuardUtilities.FACTION_PVP, StateFlag.State.ALLOW);
			
			player.sendMessage("Preventing faction pvp in region " + args[3]);
		} else if (args[1].equalsIgnoreCase("off")) {
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			
			World world = Bukkit.getWorld(args[2]);
			
			if (world == null) {
				player.sendMessage(ChatColor.RED + "Could not find world");
				return true;
			}
			
			RegionManager regions = container.get(BukkitAdapter.adapt(world));
			
			if (regions == null) {
				player.sendMessage(ChatColor.RED + "Could not find world");
				return true;
			}
			
			ProtectedRegion region = regions.getRegion(args[3]);
			
			if (region == null) {
				player.sendMessage(ChatColor.RED + "Could not find region");
				return true;
			}
			
			region.setFlag(WorldGuardUtilities.FACTION_PVP, StateFlag.State.DENY);
			
			player.sendMessage("Preventing faction pvp in region " + args[3]);
		}
		return true;
	}
	
	private boolean setClaimableFlag(Player player, String[] args) {
		if(!player.isOp()) { // verify player is op
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Non-Op player " + player.getName() + " tried to call an Op command.");
			player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
			return true;
		}
		
		if (args[1].equalsIgnoreCase("on")) {
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			
			World world = Bukkit.getWorld(args[2]);
			
			if (world == null) {
				player.sendMessage(ChatColor.RED + "Could not find world");
				return true;
			}
			
			RegionManager regions = container.get(BukkitAdapter.adapt(world));
			
			if (regions == null) {
				player.sendMessage(ChatColor.RED + "Could not find world");
				return true;
			}
			
			ProtectedRegion region = regions.getRegion(args[3]);
			
			if (region == null) {
				player.sendMessage(ChatColor.RED + "Could not find region");
				return true;
			}
			
			region.setFlag(WorldGuardUtilities.FACTION_CLAIMABLE, StateFlag.State.ALLOW);
			
			player.sendMessage("Preventing faction claims in region " + args[3]);
		} else if (args[1].equalsIgnoreCase("off")) {
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			
			World world = Bukkit.getWorld(args[2]);
			
			if (world == null) {
				player.sendMessage(ChatColor.RED + "Could not find world");
				return true;
			}
			
			RegionManager regions = container.get(BukkitAdapter.adapt(world));
			
			if (regions == null) {
				player.sendMessage(ChatColor.RED + "Could not find world");
				return true;
			}
			
			ProtectedRegion region = regions.getRegion(args[3]);
			
			if (region == null) {
				player.sendMessage(ChatColor.RED + "Could not find region");
				return true;
			}
			
			region.setFlag(WorldGuardUtilities.FACTION_CLAIMABLE, StateFlag.State.DENY);
			
			player.sendMessage("Preventing faction claims in region " + args[3]);
		}
		
		return true;
	}
	
	

}
