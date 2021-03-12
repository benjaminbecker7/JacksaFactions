package com.bmbecker.plugin.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class WorldGuardUtilities {

	public static StateFlag FACTION_PVP;
	public static void createFlag() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
		try {
			StateFlag flag = new StateFlag("faction-pvp", true);
			registry.register(flag);
			FACTION_PVP = flag;
		} catch (FlagConflictException e) {
			Flag<?> existing = registry.get("faction-pvp");
			if (existing instanceof StateFlag) {
				FACTION_PVP = (StateFlag) existing;
			} else {
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Could not generate faction pvp flag because there is already one with that name!");
			}
		}
	}
}
