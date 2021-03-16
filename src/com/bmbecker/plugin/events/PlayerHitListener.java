package com.bmbecker.plugin.events;

import com.bmbecker.plugin.utilities.FactionUtilities;
import com.bmbecker.plugin.utilities.WorldGuardUtilities;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Iterator;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerHitListener implements Listener {
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
			return;
		}
		
		Player damager = (Player) e.getDamager();
		Player damagee = (Player) e.getEntity();
		
		// CHECK IF PLAYERS ARE IN A FACTION_PVP ZONE. IF YES, HITS WILL STILL REGISTER.
		Chunk currChunk = damager.getLocation().getChunk();
		int bx = currChunk.getX() << 4;
		int bz = currChunk.getZ() << 4;
		BlockVector3 pt1 = BlockVector3.at(bx, 0, bz);
		BlockVector3 pt2 = BlockVector3.at(bx + 15, 256, bz + 15);
		ProtectedCuboidRegion region = new ProtectedCuboidRegion("ID", pt1, pt2);
		RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(damager.getWorld()));
		
		ApplicableRegionSet regionSet = regions.getApplicableRegions(region);
		Iterator<ProtectedRegion> appRegions = regionSet.iterator();
		
		if (appRegions.hasNext() && appRegions.next().getFlag(WorldGuardUtilities.FACTION_PVP) == StateFlag.State.ALLOW) {
			return;
		}
		
		if (FactionUtilities.factions.get(FactionUtilities.getFactionIndexByPlayer(damager)).isInFaction(damagee)) {
			e.setCancelled(true);
		}
	}
}
