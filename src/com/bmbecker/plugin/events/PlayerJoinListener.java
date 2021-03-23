package com.bmbecker.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.bmbecker.plugin.utilities.SQLUtilities;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player joiner = e.getPlayer();
		SQLUtilities.createPlayer(joiner);
		String faction = SQLUtilities.getInvite(joiner);
		if (faction != null) {
			joiner.sendMessage("You have been invited to join the faction " + faction + ". You may only have one pending invite at a time.");
			joiner.sendMessage("To accept this invitation, type /factions accept <faction>");
			joiner.sendMessage("To decline this invitation, type /factions decline <faction>");
		}
	}
}
