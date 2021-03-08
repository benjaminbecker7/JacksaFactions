package com.bmbecker.plugin.objects;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;

public class Faction {
	private String name;
	private UUID leaderUUID;
	private HashSet<UUID> members;
	
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
	 * @param joiner the player who is joining the party
	 */
	public void addMember(Player joiner) {
		members.add(joiner.getUniqueId());
	}
	
	public void removeMember(Player leaver) {
		members.remove(leaver.getUniqueId());
	}
	
	public void setLeader(Player newLeader) {
		UUID uuid = newLeader.getUniqueId();
		if(isInFaction(uuid)) {
			leaderUUID = uuid;
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
	
}
