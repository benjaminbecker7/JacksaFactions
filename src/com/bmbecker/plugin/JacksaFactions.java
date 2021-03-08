package com.bmbecker.plugin;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.bmbecker.plugin.objects.Faction;

public class JacksaFactions extends JavaPlugin {
	
	// Enabled method
	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[JacksaFactions]: Plugin enabled");
		getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
	}
	
	// Disable method
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Plugin disabled");
	}
}
