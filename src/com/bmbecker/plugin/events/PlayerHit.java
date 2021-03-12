package com.bmbecker.plugin.events;

import com.bmbecker.plugin.utilities.FactionUtilities;
import com.bmbecker.plugin.utilities.WorldGuardUtilities;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerHit implements Listener {
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
			return;
		}
		
		Player damager = (Player) e.getDamager();
		Player damagee = (Player) e.getEntity();
		
		// CHECK IF PLAYERS ARE IN A FACTION_PVP ZONE. IF YES, HITS WILL STILL REGISTER.
		RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(damager.getWorld()));
		BlockVector3 loc = BukkitAdapter.asBlockVector(damager.getLocation());
		
		ApplicableRegionSet regionSet = regions.getApplicableRegions(loc);
		@SuppressWarnings("unchecked")
		ArrayList<ProtectedRegion> appRegions = (ArrayList<ProtectedRegion>) regionSet.iterator();
		
		if (appRegions.size() == 1 && appRegions.get(0).getFlag(WorldGuardUtilities.FACTION_PVP) == StateFlag.State.DENY) {
			return;
		}
		
		if (FactionUtilities.factions.get(FactionUtilities.getFactionIndexByPlayer(damager)).isInFaction(damagee)) {
			e.setCancelled(true);
		}
	}
}
