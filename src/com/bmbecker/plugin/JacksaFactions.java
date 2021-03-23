package com.bmbecker.plugin;

import com.bmbecker.plugin.commands.FactionCommands;
import com.bmbecker.plugin.events.ChunkListener;
import com.bmbecker.plugin.events.PlayerHitListener;
import com.bmbecker.plugin.events.PlayerJoinListener;
import com.bmbecker.plugin.utilities.WorldGuardUtilities;

import org.bukkit.plugin.java.JavaPlugin;

public class JacksaFactions extends JavaPlugin {
	
	@Override
	public void onLoad() {
		WorldGuardUtilities.createFlags(); // have to create FACTION_PVP flag before WorldGuard is enabled
	}
	
	// Enable method
	@Override
	public void onEnable() {
		getCommand("faction").setExecutor(new FactionCommands());
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerHitListener(), this);
		getServer().getPluginManager().registerEvents(new ChunkListener(), this);
		getServer().getConsoleSender().sendMessage("[JacksaFactions]: Plugin enabled");
	}
	
	// Disable method
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage("[JacksaFactions]: Plugin disabled");
	}
}
