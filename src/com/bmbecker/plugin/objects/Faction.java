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
	private Domain domain; // object tracks chunks owned by faction
	
	// REFACTOR: Could have function to determine if chunk is in faction's domain
	
	/**
	 * Creates a new faction with an initial player
	 * @param initial the player who creates the faction
	 */
	public Faction(Player initial, String name) {
		members = new HashSet<UUID>();
		
		UUID uuid = initial.getUniqueId();
		
		this.members = new HashSet<UUID>();
		members.add(uuid);
		leaderUUID = uuid;
		this.name = name;
		
		this.invitees = new HashSet<UUID>();
		initial.setDisplayName("[" + name + "]" + initial.getDisplayName());
		this.domain = new Domain();
	}
	
	/**
	 * Adds a new player to the faction
	 * Add faction name to player name
	 * @param joiner the player who is joining the faction
	 */
	public void addMember(Player joiner) {
		UUID joinerUUID = joiner.getUniqueId();
		if (!invitees.contains(joinerUUID)) {
			Bukkit.getConsoleSender().sendMessage("[JacksaFactions]: Faction " + name + " tried to add a member who was not invited.");
			return;
		}
		invitees.remove(joinerUUID);
		joiner.setDisplayName("[" + name + "]" + joiner.getDisplayName());
		members.add(joinerUUID);
	}
	
	public void removeMember(Player leaver) {
		leaver.setDisplayName(leaver.getDisplayName().substring(name.length() + 2));
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
	
	public Iterator<UUID> memberIterator() {
		return members.iterator();
	}
	
	public void addChunk(ClaimedChunk newChunk) {
		domain.addChunk(newChunk);
	}
	
	public boolean removeChunk(ClaimedChunk remChunk) {
		return domain.removeChunk(remChunk);
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public int getMaxChunks() {
		return members.size() * 10;
	}
	
	public int getNumChunks() {
		return domain.size();
	}
	
	public boolean hasMaxChunks() {
		return getNumChunks() == getMaxChunks();
	}
	
}
