package com.bmbecker.plugin.utilities;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.bmbecker.plugin.config.Keys;
import com.bmbecker.plugin.objects.ClaimedChunk;

import me.vagdedes.mysql.database.MySQL;

// REFACTOR: find more ways to use Query Result to make processes more readable
// Also reordering the helper methods could make code more readable as well.


// ###################SCHEMA#####################

// ================= factions ===================
// || name || leader || numclaims || maxclaims ||
// ==============================================

// ========== players ==========
// || id || faction || invite ||
// =============================

// ============= claimed_chunks =============
// || server || world || x || z || faction ||
// ==========================================

public class SQLUtilities {
	
	private static final String TABLE_FACTIONS = "factions";
	private static final String TABLE_CLAIMED_CHUNKS = "claimed_chunks";
	private static final String TABLE_PLAYERS = "players";
	
	private static final String SQL_ADDR = Keys.SQL_ADDR_DEV;
	private static final String SQL_PORT = Keys.SQL_PORT_DEV;
	private static final String SQL_DB = Keys.SQL_DB_DEV;
	private static final String SQL_USER = Keys.SQL_USER_DEV;
	private static final String SQL_PW = Keys.SQL_PW_DEV;	
	
	/**
	 * Initializes connection to the SQL server specified in the constants.
	 */
	public static void initConnection() {
		try {
			MySQL.setConnection(SQL_ADDR, SQL_USER, SQL_PW, SQL_DB, SQL_PORT);
			Bukkit.getServer().getConsoleSender().sendMessage("[JacksaFactions]: Connected to SQL Database.");
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getServer().getConsoleSender().sendMessage("[JacksaFactions]: Failed to connect to SQL Database.");
		}
	}
	
	/**
	 * Closes the connection to the SQL server currently connected.
	 */
	public static void closeConnection() {
		try {
			MySQL.disconnect();
			Bukkit.getServer().getConsoleSender().sendMessage("[JacksaFactions]: SQL Connection has been closed.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a player record in the database.
	 * @param p the new player to create in the database
	 */
	public static void createPlayer(Player p) {
		try {
			
			ResultSet r = MySQL.query("SELECT id FROM " + TABLE_PLAYERS + " WHERE id='" + p.getUniqueId() + "';");
			
			if (r != null && r.next()) {
				return;
			}
			
			MySQL.update("INSERT INTO " + TABLE_PLAYERS + " (id, faction, invite) VALUES ('" + p.getUniqueId() + "', NULL, NULL);");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the maximum number of chunk claims a faction can have
	 * @param faction the name of the faction that the function will look up
	 * @return the maximum number of claims the faction can have
	 */
	public static int getMaxClaims(String faction) {
		try {
			ResultSet r = MySQL.query("SELECT maxclaims FROM " + TABLE_FACTIONS + " WHERE name='" + faction + "';");
			if (r != null && r.next()) {
				return r.getInt(1);
			} else {
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Gets the number of chunk claims a faction currently have
	 * @param faction the name of the faction that the function will look up
	 * @return the number of claims the faction currently has
	 */
	public static int getNumClaims(String faction) {
		try {
			ResultSet r = MySQL.query("SELECT numclaims FROM " + TABLE_FACTIONS + " WHERE name='" + faction + "';");
			if (r != null && r.next()) {
				return r.getInt(1);
			} else {
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Creates a faction record in the database
	 * @param faction the name of the faction that is being created
	 * @param creator the player who is creating the faction, who will become the leader
	 * @return the result of the query, as specified in the QueryResult enum
	 */
	public static QueryResult createFaction(String faction, Player creator) {
		try {
			if (getPlayerFaction(creator) != null) {
				return QueryResult.ALREADY_IN_FACTION;
			}
			
			if (invalidName(faction)) { // player tried to use SQL escape keys in their faction name
				return QueryResult.NAME_INVALID;
			}
			
			if (!checkFactionNameAvailable(faction)) {
				return QueryResult.NAME_ALREADY_TAKEN;
			}
			
			MySQL.update("INSERT INTO " + TABLE_FACTIONS + " (name, leader, numclaims, maxclaims) VALUES ('" + faction + "', '" + creator.getUniqueId() + "', 0, 10);");
			MySQL.update("UPDATE " + TABLE_PLAYERS + " SET faction='" + faction + "' WHERE id='" + creator.getUniqueId() + "';");
			return QueryResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return QueryResult.ERROR;
		}
	}
	
	/**
	 * Helper function that checks if a string uses SQL escape keys that could lead to injection.
	 * @param name the name that is being examined
	 * @return a boolean that indicates whether the string uses illegal character
	 */
	public static boolean invalidName(String name) {
		return name.length() > 8 || name.contains("\"") || name.contains(";") || name.contains("'"); // player tried to use SQL escape keys in their faction name
	}
	
	public static boolean checkFactionNameAvailable(String faction) {
		try {
			ResultSet r = MySQL.query("SELECT name FROM " + TABLE_FACTIONS + " WHERE name='" + faction + "';");
			
			return r != null && !r.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean hasInvite(Player p) {
		try {
			ResultSet r = MySQL.query("SELECT invite FROM " + TABLE_PLAYERS + " WHERE id='" + p.getUniqueId() + "';");
			return r != null && r.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static String getInvite(Player p) {
		try {
			ResultSet r = MySQL.query("SELECT invite FROM " + TABLE_PLAYERS + " WHERE id='" + p.getUniqueId() + "';");
			if (r != null && r.next()) {
				return r.getString(1);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void addInvite(String faction, Player p) {
		try {
			MySQL.update("UPDATE " + TABLE_PLAYERS + " SET invite='" + faction + "' WHERE id='" + p.getUniqueId() + "';");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void removeInvite(Player p) {
		try {
			MySQL.update("UPDATE " + TABLE_PLAYERS + " SET invite=NULL WHERE id='" + p.getUniqueId() + "';");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addFactionMember(String faction, Player p) {
		try {
			MySQL.update("UPDATE " + TABLE_PLAYERS + " SET faction='" + faction + "' WHERE id='" + p.getUniqueId() + "';");
			MySQL.update("UPDATE " + TABLE_FACTIONS + " SET maxclaims=maxclaims+10 WHERE name='" + faction + "';"); // update numclaims in factions table
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void removeFactionMember(String faction, Player p) {
		try {
			
			MySQL.update("UPDATE " + TABLE_PLAYERS + " SET faction=NULL WHERE id='" + p.getUniqueId() + "';");
			
			MySQL.update("UPDATE " + TABLE_FACTIONS + " SET maxclaims=maxclaims-10 WHERE name='" + faction + "';"); // update numclaims in factions table
			
			ResultSet r = MySQL.query("SELECT numclaims, maxclaims FROM " + TABLE_FACTIONS + " WHERE name='" + faction + "';");
			if (r != null && !r.next()) {
				return;
			}
			
			int diff = r.getInt(1) - r.getInt(2);
			
			if (diff > 0) { // faction now has more claims than their maxclaims. remove claims until numclaims==maxclaims (diff == 0)
				MySQL.update("DELETE FROM " + TABLE_CLAIMED_CHUNKS + " WHERE faction='" + faction + "' LIMIT " + diff + ";");
				MySQL.update("UPDATE " + TABLE_FACTIONS + " SET numclaims=maxclaims WHERE name='" + faction + "';");
			}
			
			if (isFactionLeader(p)) { // handle if leader leaves their faction. Appoint next in line.
				r = MySQL.query("SELECT id FROM " + TABLE_PLAYERS + " WHERE faction='" + faction + "';");
				if (r != null && r.next()) {
					MySQL.update("UPDATE " + TABLE_FACTIONS + " SET leader='" + r.getString(1) + "' WHERE name='" + faction + "';");
					Bukkit.getPlayer(UUID.fromString(r.getString(1))).sendMessage("You have been appointed as the new leader of your faction!");
				} else {
					MySQL.update("DELETE FROM " + TABLE_FACTIONS + " WHERE name='" + faction + "';");
					MySQL.update("DELETE FROM " + TABLE_CLAIMED_CHUNKS + " WHERE faction='" + faction + "';");
					return;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static ArrayList<String> getFactionMembers(String faction) {
		try {
			ResultSet r = MySQL.query("SELECT id FROM " + TABLE_PLAYERS + " WHERE faction='" + faction + "';");
			
			ArrayList<String> ret = new ArrayList<String>();
			
			while (r != null && r.next()) {
				ret.add(Bukkit.getPlayer(UUID.fromString(r.getString(1))).getName());
			}
			
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean addClaimedChunk(String faction, ClaimedChunk chunk) {
		try {
			
			ResultSet r = MySQL.query("SELECT numclaims, maxclaims FROM " + TABLE_FACTIONS + " WHERE name='" + faction + "';");
			
			if (r != null && (!r.next() || r.getInt(1) == r.getInt(2))) { // faction does not exist or faction has reached max claims
				return false;
			}
			
			MySQL.update("INSERT INTO " + TABLE_CLAIMED_CHUNKS + " (server, world, x, z, faction) VALUES ('" + chunk.getServer() + "', '" + chunk.getWorld() + "', " + chunk.getX() + ", " + chunk.getZ() + ", '" + faction + "');");
			MySQL.update("UPDATE " + TABLE_FACTIONS + " SET numclaims=numclaims+1 WHERE name='" + faction + "';"); // update numclaims in factions table
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean removeClaimedChunk(String faction, ClaimedChunk chunk) {
		try {
			MySQL.update("DELETE FROM " + TABLE_CLAIMED_CHUNKS + " WHERE server='" + chunk.getServer() + "' AND world='" + chunk.getWorld() + "' AND x=" + chunk.getX() + " AND z=" + chunk.getZ() + " AND faction='" + faction + "');");
			MySQL.update("UPDATE " + TABLE_FACTIONS + " SET numclaims=numclaims-1 WHERE name='" + faction + "';"); // update numclaims in factions table
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean chunkClaimed(ClaimedChunk chunk) {
		try {
			ResultSet r = MySQL.query("SELECT * FROM " + TABLE_CLAIMED_CHUNKS + " WHERE server='" + chunk.getServer() + "' AND world='" + chunk.getWorld() + "' AND x=" + chunk.getX() + " AND z=" + chunk.getZ() + ";");
			return r != null && r.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean chunkOwnedByFaction(String faction, ClaimedChunk chunk) {
		try {
			ResultSet r = MySQL.query("SELECT faction FROM " + TABLE_CLAIMED_CHUNKS + " WHERE server='" + chunk.getServer() + "' AND world='" + chunk.getWorld() + "' AND x=" + chunk.getX() + " AND z=" + chunk.getZ() + ";");
			if (r != null && r.next()) {
				return r.getString(1).equals(faction);
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean isFactionLeader(Player p) {
		try {
			ResultSet r = MySQL.query("SELECT name FROM " + TABLE_FACTIONS + " WHERE leader='" + p.getUniqueId() + "';");
			
			return r.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean inFaction(Player p) {
		try {
			ResultSet r = MySQL.query("SELECT faction FROM " + TABLE_PLAYERS + " WHERE id='" + p.getUniqueId() + "';");
			
			return r.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean inFaction(String faction, Player p) {
		try {
			ResultSet r = MySQL.query("SELECT faction FROM " + TABLE_PLAYERS + " WHERE id='" + p.getUniqueId() + "';");
			
			if (r != null && r.next()) {
				return faction.equals(r.getString(1));
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void setLeader(String faction, Player p) {
		try {
			MySQL.update("UPDATE " + TABLE_FACTIONS + " SET leader='" + p.getUniqueId() + "' WHERE name='" + faction + "';");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static UUID getFactionLeader(String faction) {
		try {
			ResultSet r = MySQL.query("SELECT leader FROM " + TABLE_FACTIONS + " WHERE name='" + faction + "';");
			if (r != null && r.next()) { 
				return UUID.fromString(r.getString(1));
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getPlayerFaction(Player p) {
		try {
			ResultSet r = MySQL.query("SELECT faction FROM " + TABLE_PLAYERS + " WHERE id='" + p.getUniqueId() + "';");
			
			if (r != null && r.next()) {
				return r.getString(1);
			}
			
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getChunkFaction(ClaimedChunk c) {
		try {
			ResultSet r = MySQL.query("SELECT faction FROM " + TABLE_CLAIMED_CHUNKS + " WHERE server='" + c.getServer() + "' AND world='" + c.getWorld() + "' AND x=" + c.getX() + " AND z=" + c.getZ() + ";");
			
			if (r != null && r.next()) {
				return r.getString(1);
			}
			
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
