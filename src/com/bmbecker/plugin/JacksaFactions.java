package com.bmbecker.plugin;

import com.bmbecker.plugin.commands.FactionCommands;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class JacksaFactions extends JavaPlugin {
	
	// Enabled method
	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[JacksaFactions]: Plugin enabled");
		getCommand("faction").setExecutor(new FactionCommands());
		getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
	}
	
	// Disable method
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Plugin disabled");
	}
}
