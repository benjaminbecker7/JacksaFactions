package com.bmbecker.plugin.events;

import com.bmbecker.plugin.utilities.Cache;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bmbecker.plugin.objects.ClaimedChunk;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkListener implements Listener {

    private JavaPlugin plugin;

    public ChunkListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

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
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }

        Player player = e.getPlayer();

        // Determine if block is claimed by a faction but not by player's faction.
        // It's a long conditional I know but for the sake of memory optimization
        // it had to be done this way.
        if (inClaimedChunk(player, e.getBlock())) {
            e.setCancelled(true);
            player.sendMessage("This land is owned by another faction.");
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
            player.sendMessage("This block is owned by another faction.");
        }

    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent e) {
        if (e.getEntity() instanceof Player) {
            return;
        }

        Entity entity = e.getEntity();

        if (inClaimedChunk(entity, e.getBlock())) {
            e.setCancelled(true);
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

        if (b == null) {
            return false;
        }

        // Create dummy ClaimedChunk for comparison
        Chunk blockChunk = b.getChunk();
        ClaimedChunk dummyChunk = new ClaimedChunk(Bukkit.getServer().getName(), blockChunk.getWorld().getName(), blockChunk.getX(), blockChunk.getZ());

        if (Cache.contains(dummyChunk)) {
            // Determine if block is claimed by a faction but not by player's faction.
            String chunkFaction = Cache.get(dummyChunk);
            return chunkFaction != null && !chunkFaction.equals(Cache.getFaction(p.getUniqueId()));
        }

        return false;
    }

    private boolean inClaimedChunk(Entity e, Block b) {

        if (e instanceof Player) {
            return inClaimedChunk((Player) e, b);
        }

        if (b == null) {
            return false;
        }

        Chunk blockChunk = b.getChunk();
        ClaimedChunk dummyChunk = new ClaimedChunk(Bukkit.getServer().getName(), blockChunk.getWorld().getName(), blockChunk.getX(), blockChunk.getZ());

        return Cache.contains(dummyChunk);
    }

}
