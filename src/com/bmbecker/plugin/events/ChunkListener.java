package com.bmbecker.plugin.events;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bmbecker.plugin.objects.ClaimedChunk;
import com.bmbecker.plugin.utilities.FactionUtilities;

//TODO: Should check to see if block is in another faction's domain chunks.

public class ChunkListener implements Listener {
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		Player player = e.getPlayer();
		
		// Determine if block is claimed by a faction but not by player's faction.
		// It's a long conditional I know but for the sake of memory optimization 
		// it had to be done this way.
		if (inClaimedChunk(player, e.getBlock())) {
			e.setCancelled(true);
			player.sendMessage("This block is owned by another faction.");
		}
		
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		
		Player player = e.getPlayer();
		
		if (inClaimedChunk(player, e.getClickedBlock())) {
			e.setCancelled(true);
			player.sendMessage("This " + e.getClickedBlock() + " is owned by another faction.");
		}
	}
	
	
	/**
	 * Helper method that determines if player is interacting with a block that is 
	 * in another faction's claimed chunk
	 * @param p the player who is interacting with the block.
	 * @param b the block the player is interacting with.
	 * @return whether the player is interacting with a block that is in another faction's domain.
	 */
	private boolean inClaimedChunk(Player p, Block b) {
		
		// Create dummy ClaimedChunk for comparison
		Chunk blockChunk = b.getChunk();
		ClaimedChunk dummyChunk = new ClaimedChunk(blockChunk.getWorld().getName(), blockChunk.getX(), blockChunk.getZ());
		
		// Determine if block is claimed by a faction but not by player's faction.
		return FactionUtilities.claimedChunks.contains(dummyChunk) && 
			!FactionUtilities
				.factions
				.get(FactionUtilities.getFactionIndexByPlayer(p))
				.getDomain()
				.inDomain(b.getChunk());
		
	}
	
}
