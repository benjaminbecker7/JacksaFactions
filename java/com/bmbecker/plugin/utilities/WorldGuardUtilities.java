package com.bmbecker.plugin.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class WorldGuardUtilities {

	public static StateFlag FACTION_PVP, FACTION_CLAIMABLE;
	public static void createFlags() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
		
		// Create FACTION_PVP flag
		try {
			StateFlag flag = new StateFlag("faction-pvp", true);
			registry.register(flag);
			FACTION_PVP = flag;
		} catch (FlagConflictException e) {
			Flag<?> existing = registry.get("faction-pvp");
			if (existing instanceof StateFlag) {
				FACTION_PVP = (StateFlag) existing;
			} else {
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Could not generate FACTION_PVP flag because there is already one with that name!");
			}
		}
		
		// Create FACTION_CLAIMABLE flag
		try {
			StateFlag flag = new StateFlag("faction-claimable", true);
			registry.register(flag);
			FACTION_CLAIMABLE = flag;
		} catch (FlagConflictException e) {
			Flag<?> existing = registry.get("faction-claimable");
			if (existing instanceof StateFlag) {
				FACTION_CLAIMABLE = (StateFlag) existing;
			} else {
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Could not generate FACTION_CLAIMABLE flag because there is already one with that name!");
			}
		}
	}
}
