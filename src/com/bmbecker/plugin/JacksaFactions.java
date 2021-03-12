package com.bmbecker.plugin;

import com.bmbecker.plugin.commands.FactionCommands;
import com.bmbecker.plugin.events.PlayerHit;
import com.bmbecker.plugin.utilities.WorldGuardUtilities;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class JacksaFactions extends JavaPlugin {
	
	@Override
	public void onLoad() {
		WorldGuardUtilities.createFlag(); // have to create FACTION_PVP flag before WorldGuard is enabled
	}
	
	// Enabled method
	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[JacksaFactions]: Plugin enabled");
		getCommand("faction").setExecutor(new FactionCommands());
		getServer().getPluginManager().registerEvents(new PlayerHit(), this);
	}
	
	// Disable method
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Plugin disabled");
	}
}
