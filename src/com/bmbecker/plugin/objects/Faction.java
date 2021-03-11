package com.bmbecker.plugin.objects;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Faction {
	private String name;
	private UUID leaderUUID;
	private HashSet<UUID> members;
	private HashSet<UUID> invitees;
	
	/**
	 * Creates a new party with an initial player
	 * @param initial the player who creates the party
	 */
	public Faction(Player initial, String name) {
		members = new HashSet<UUID>();
		
		UUID uuid = initial.getUniqueId();
		members.add(uuid);
		leaderUUID = uuid;
		this.name = name;
	}
	
	/**
	 * Adds a new player to the party
	 * Add faction name to player name
	 * @param joiner the player who is joining the party
	 */
	public void addMember(Player joiner) {
		UUID joinerUUID = joiner.getUniqueId();
		if (!invitees.contains(joinerUUID)) {
			Bukkit.getConsoleSender().sendMessage("[JacksaFactions]: Faction " + name + " tried to add a member who was not invited.");
			return;
		}
		invitees.remove(joinerUUID);
		joiner.setCustomName(joiner.getCustomName() + " [" + name + "]");
		members.add(joinerUUID);
	}
	
	public void removeMember(Player leaver) {
		leaver.setCustomName(leaver.getCustomName().substring(0, leaver.getCustomName().length() - 3 - name.length()));
		members.remove(leaver.getUniqueId());
	}

	public void addInvitee(Player invitee) {
		invitees.add(invitee.getUniqueId());
	}
	
	public void setLeader(Player newLeader) {
		UUID uuid = newLeader.getUniqueId();
		if(isInFaction(uuid)) {
			leaderUUID = uuid;
		}
	}
	
	public void setLeaderUUID(UUID newLeaderUUID) {
		if(isInFaction(newLeaderUUID)) {
			leaderUUID = newLeaderUUID;
		}
	}
	
	public boolean isInFaction(Player player) {
		return members.contains(player.getUniqueId());
	}
	
	public boolean isInFaction(UUID uuid) {
		return members.contains(uuid);
	}
	
	public String getName() {
		return name;
	}
	
	public UUID getLeaderUUID() {
		return leaderUUID;
	}
	
	public boolean isLeader(Player player) {
		return leaderUUID.equals(player.getUniqueId());
	}

	public boolean isInvited(Player player) {
		return invitees.contains(player.getUniqueId());
	}
	
	public void removeInvite(Player player) {
		invitees.remove(player.getUniqueId());
	}
	
	public UUID getNextLeaderUUID() {
		Iterator<UUID> iter = members.iterator();
		while (iter.hasNext()) {
			UUID nxt = iter.next();
			if (nxt != leaderUUID) {
				return nxt;
			}
		}
		return null;
	}
	
}
