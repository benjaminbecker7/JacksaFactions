package com.bmbecker.plugin.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.bmbecker.plugin.config.Keys;
import com.bmbecker.plugin.objects.ClaimedChunk;
import com.bmbecker.plugin.objects.Faction;

public class SQLUtilities {
	
	/*
	 * Events where all servers should be updated:
	 * - faction created
	 * - 
	 */
	
	private static final String TABLE_FACTIONS = "factions";
	private static final String TABLE_CLAIMED_CHUNKS = "claimed_chunks";
	private static final String TABLE_PLAYERS = "players";
	
	private static final String SQL_ADDR = Keys.SQL_ADDR;
	private static final String SQL_USER = Keys.SQL_USER;
	private static final String SQL_PW = Keys.SQL_PW;
	
	private static Connection con;
	
	
	//TODO: Might want to load data on connection init
	public static void initConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(SQL_ADDR, SQL_USER, SQL_PW);
			Bukkit.getServer().getConsoleSender().sendMessage("[JacksaFactions]: Connected to SQL Database.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//TODO: Might want to save data on connection close
	public static void closeConnection() {
		try {
			con.close();
			Bukkit.getServer().getConsoleSender().sendMessage("[JacksaFactions]: SQL Connection has been closed.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void createFaction(Faction faction) {
		try {
			Statement stmt = con.createStatement();
			
			if (faction.getName().contains("\"") || faction.getName().contains(";") || faction.getName().contains("'")) { // player tried to use SQL escape keys in their faction name
				return;
			}
			
			stmt.executeUpdate("INSERT INTO " + TABLE_FACTIONS + " (name, leader) VALUES ('" + faction.getName() + "', " + faction.getLeaderUUID() + ");");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addFactionMember(Faction faction, Player player) {
		try {
			Statement stmt = con.createStatement();
			
			stmt.executeUpdate("UPDATE " + TABLE_PLAYERS + " SET faction='" + faction.getName() + "' WHERE player_id=" + player.getUniqueId() + ";");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addClaimedChunk(Faction faction, ClaimedChunk chunk) {
		try {
			Statement stmt = con.createStatement();
			
			stmt.executeUpdate("INSERT INTO " + TABLE_CLAIMED_CHUNKS + " (world, x, z, faction) VALUES ('" + chunk.getWorld() + "', " + chunk.getX() + ", " + chunk.getZ() + ", '" + faction.getName() + "');");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	public static Faction getPlayerFaction(Player p) {
		UUID playerID = p.getUniqueId();
		
		
	}
	*/
}
