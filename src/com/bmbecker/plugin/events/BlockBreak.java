package com.bmbecker.plugin.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.bmbecker.plugin.objects.ClaimedChunk;
import com.bmbecker.plugin.utilities.FactionUtilities;

//TODO: Should check to see if block is in another faction's domain chunks.

public class BlockBreak implements Listener {
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		Player player = e.getPlayer();
		
		Chunk blockChunk = e.getBlock().getChunk();
		ClaimedChunk dummyChunk = new ClaimedChunk(blockChunk.getWorld().getName(), blockChunk.getX(), blockChunk.getZ());
		
		
		// Determine if block is claimed by a faction but not by player's faction.
		// It's a long conditional I know but for the sake of memory optimization 
		// it had to be done this way.
		if (FactionUtilities.claimedChunks.contains(dummyChunk) && 
			!FactionUtilities
				.factions
				.get(FactionUtilities.getFactionIndexByPlayer(player))
				.getDomain()
				.inDomain(e.getBlock().getChunk())
		) {
			e.setCancelled(true);
			player.sendMessage("This block is owned by another faction.");
		}
		
	}
	
}
