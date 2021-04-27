package com.bmbecker.plugin.utilities;

import java.sql.*;
import java.util.*;

import com.bmbecker.plugin.objects.FactionHome;
import com.bmbecker.plugin.utilities.perms.PermSet;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import com.bmbecker.plugin.objects.ClaimedChunk;
import org.bukkit.plugin.java.JavaPlugin;

// REFACTOR: find more ways to use Query Result to make processes more readable
// Also reordering the helper methods could make code more readable as well.


// ###################SCHEMA#####################

// ================= factions ===================
// || name || leader || numclaims || maxclaims ||
// ==============================================

// ========== players ===============================================
// || id || faction || invite || isLeader || canInvite || canClaim ||
// ==================================================================

// ============= claimed_chunks =============
// || server || world || x || z || faction ||
// ==========================================

public class SQLUtilities {
	
	private static final String TABLE_FACTIONS = "factions";
	private static final String TABLE_CLAIMED_CHUNKS = "claimed_chunks";
	private static final String TABLE_PLAYERS = "players";

	public static JavaPlugin plugin;

	/**
	 * Initializes connection to the SQL server specified in the constants.
	 */
	public static Connection openConnection() {
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("IP-address") + ":" + plugin.getConfig().getString("port") + "/" + plugin.getConfig().getString("database"), plugin.getConfig().getString("username"), plugin.getConfig().getString("password"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Closes the connection to the SQL server currently connected.
	 */
	public static void closeConnection(Connection con) {
		try {
			if (!con.isClosed()) {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void closeConnection(Connection con, PreparedStatement ... ps) {
		try {
			for (PreparedStatement p : ps) {
				if (!p.isClosed()) {
					p.close();
				}
			}

			if (!con.isClosed()) {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void closeConnection(PreparedStatement ... ps) {
		try {
			for (PreparedStatement p : ps) {
				if (!p.isClosed()) {
					p.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a player record in the database.
	 * @param p the new player to create in the database
	 */
	public static void createPlayer(final Player p) {
		Connection con = openConnection();

		try {
			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT id FROM " + TABLE_PLAYERS + " WHERE id=?;");

			ps.setString(1, p.getUniqueId().toString());

			ResultSet r = ps.executeQuery();

			if (r != null && r.next()) {
				closeConnection(con, ps);
				return;
			}

			ps = con.prepareStatement("INSERT INTO " + TABLE_PLAYERS + " (id, faction, invite, canClaim, canInvite) VALUES (?, NULL, NULL, false, false);");
			ps.setString(1, p.getUniqueId().toString());

			ps.executeUpdate();
			closeConnection(con, ps);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection(con);
		}

	}

	public static HashMap<UUID, String> getPlayerFactions() {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + TABLE_PLAYERS + ";");

			ResultSet r = ps.executeQuery();

			HashMap<UUID, String> ret = new HashMap<>();

			while (r.next()) {
				ret.put(UUID.fromString(r.getString(1)), r.getString(2));
			}
			closeConnection(con, ps);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}

	public static HashMap<UUID, String> getInvites() {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + TABLE_PLAYERS + ";");

			ResultSet r = ps.executeQuery();

			HashMap<UUID, String> ret = new HashMap<>();

			while (r.next()) {
				ret.put(UUID.fromString(r.getString(1)), r.getString(3));
			}
			closeConnection(con, ps);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}

	public static HashMap<UUID, PermSet> getPerms() {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement qPS1 = con.prepareStatement("SELECT * FROM " + TABLE_PLAYERS + ";");

			ResultSet r = qPS1.executeQuery();

			HashMap<UUID, PermSet> ret = new HashMap<>();

			while (r.next()) {
				UUID uuid = UUID.fromString(r.getString(1));
				boolean canClaim = r.getBoolean(4);
				boolean canInvite = r.getBoolean(5);
				PreparedStatement qPS2 = con.prepareStatement("SELECT * FROM " + TABLE_FACTIONS + " WHERE leader=?");
				qPS2.setString(1, uuid.toString());
				ResultSet r2 = qPS2.executeQuery();
				boolean isLeader = (r2 != null && r2.next());

				ret.put(uuid, new PermSet(isLeader, canClaim, canInvite));
				closeConnection(qPS2);
			}
			closeConnection(con, qPS1);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}

	public static void setPerms(Player p, boolean canClaim, boolean canInvite) {
		Connection con = openConnection();
		try {
			assert con != null;
			PreparedStatement ps = con.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET canClaim=?, canInvite=? WHERE id=?;");
			ps.setBoolean(1, canClaim);
			ps.setBoolean(2, canInvite);
			ps.setString(3, p.getUniqueId().toString());

			ps.executeUpdate();

			closeConnection(con, ps);
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
		}
	}

	public static HashMap<String, FactionHome> getHomes() {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement qPS1 = con.prepareStatement("SELECT name, homeServer, homeWorld, homeX, homeY, homeZ FROM " + TABLE_FACTIONS + ";");

			ResultSet r = qPS1.executeQuery();

			HashMap<String, FactionHome> ret = new HashMap<>();

			while (r.next()) {
				if (r.getString(2) == null) {
					ret.put(r.getString(1), null);
				} else {
					ret.put(r.getString(1), new FactionHome(r.getString(2), r.getString(3), r.getInt(4), r.getInt(5), r.getInt(6)));
				}
			}
			closeConnection(con, qPS1);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}

	public static void setHome(String faction, FactionHome home) {
		Connection con = openConnection();
		try {
			assert con != null;
			PreparedStatement uPS1 = con.prepareStatement("UPDATE " + TABLE_FACTIONS + " SET homeServer=?, homeWorld=?, homeX=?, homeY=?, homeZ=? WHERE name=?;");
			uPS1.setString(1, home.getServer());
			uPS1.setString(2, home.getWorld());
			uPS1.setInt(3, home.getX());
			uPS1.setInt(4, home.getY());
			uPS1.setInt(5, home.getZ());
			uPS1.setString(6, faction);

			uPS1.executeUpdate();
			closeConnection(con, uPS1);
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
		}
	}

	public static FactionHome getHome(String faction) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement qPS1 = con.prepareStatement("SELECT homeServer, homeWorld, homeX, homeY, homeZ FROM " + TABLE_FACTIONS + " WHERE name=?;");
			qPS1.setString(1, faction);

			ResultSet r = qPS1.executeQuery();

			if(r != null && r.next()) {
				if (r.getString(1) == null) {
					closeConnection(con, qPS1);
					return null;
				} else {
					String server = r.getString(1);
					String world = r.getString(2);
					int x = r.getInt(3);
					int y = r.getInt(4);
					int z = r.getInt(5);

					closeConnection(con, qPS1);
					return new FactionHome(server, world, x, y, z);
				}
			} else {
				closeConnection(con, qPS1);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}
	
	/**
	 * Gets the maximum number of chunk claims a faction can have
	 * @param faction the name of the faction that the function will look up
	 * @return the maximum number of claims the faction can have
	 */
	public static int getMaxClaims(final String faction) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT maxclaims FROM " + TABLE_FACTIONS + " WHERE name=?;");


			ps.setString(1, faction);

			ResultSet r = ps.executeQuery();

			if (r != null && r.next()) {
				int ret = r.getInt(1);
				closeConnection(con, ps);
				return ret;
			} else {
				closeConnection(con, ps);
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			closeConnection(con);
		}
	}
	
	/**
	 * Gets the number of chunk claims a faction currently have
	 * @param faction the name of the faction that the function will look up
	 * @return the number of claims the faction currently has
	 */
	public static int getNumClaims(final String faction) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT numclaims FROM " + TABLE_FACTIONS + " WHERE name=?;");
			ps.setString(1, faction);

			ResultSet r = ps.executeQuery();

			if (r != null && r.next()) {
				int ret = r.getInt(1);
				closeConnection(con, ps);
				return ret;
			} else {
				closeConnection(con, ps);
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return -1;
		} finally {
			closeConnection(con);
		}
	}
	
	/**
	 * Creates a faction record in the database
	 * @param faction the name of the faction that is being created
	 * @param creator the player who is creating the faction, who will become the leader
	 * @return the result of the query, as specified in the QueryResult enum
	 */
	public static QueryResult createFaction(final String faction, final Player creator) {
		Connection con = openConnection();
		try {

			if (getPlayerFaction(creator) != null) {
				closeConnection(con);
				return QueryResult.ALREADY_IN_FACTION;
			}
			
			if (invalidName(faction)) { // player tried to use SQL escape keys in their faction name
				closeConnection(con);
				return QueryResult.NAME_INVALID;
			}
			
			if (!checkFactionNameAvailable(faction)) {
				closeConnection(con);
				return QueryResult.NAME_ALREADY_TAKEN;
			}

			assert con != null;
			PreparedStatement uPS1 = con.prepareStatement("INSERT INTO " + TABLE_FACTIONS + " (name, leader, numclaims, maxclaims) VALUES (?, ?, 0, 10);");

			uPS1.setString(1, faction);
			uPS1.setString(2, creator.getUniqueId().toString());

			PreparedStatement uPS2 = con.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET faction=?, canClaim=true, canInvite=true WHERE id=?;");

			uPS2.setString(1, faction);
			uPS2.setString(2, creator.getUniqueId().toString());

			uPS1.executeUpdate();
			uPS2.executeUpdate();

			closeConnection(con, uPS1, uPS2);
			return QueryResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return QueryResult.ERROR;
		} finally {
			closeConnection(con);
		}
	}
	
	/**
	 * Helper function that checks if a string uses SQL escape keys that could lead to injection.
	 * @param name the name that is being examined
	 * @return a boolean that indicates whether the string uses illegal character
	 */
	public static boolean invalidName(String name) {
		return name.length() > 8 || !StringUtils.isAlphanumeric(name); // player tried to use SQL escape keys in their faction name
	}
	
	public static boolean checkFactionNameAvailable(String faction) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT name FROM " + TABLE_FACTIONS + " WHERE name=?;");
			ps.setString(1, faction);
			ResultSet r = ps.executeQuery();
			boolean ret = (r != null && !r.next());
			closeConnection(con, ps);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return false;
		} finally {
			closeConnection(con);
		}
	}
	
	public static boolean hasInvite(Player p) {
		return getPlayerInvite(p) != null;
	}

	public static String getPlayerInvite(Player p) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT invite FROM " + TABLE_PLAYERS + " WHERE id=?;");
			ps.setString(1, p.getUniqueId().toString());

			ResultSet r = ps.executeQuery();

			if (r != null && r.next()) {
				String ret = r.getString(1);
				closeConnection(con, ps);
				return ret;
			}
			closeConnection(con, ps);

			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeConnection(con);
		}
	}
	
	public static void addInvite(String faction, Player p) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET invite=? WHERE id=?;");
			ps.setString(1, faction);
			ps.setString(2, p.getUniqueId().toString());
			ps.executeUpdate();
			closeConnection(ps);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection(con);
		}
	}
	
	public static void removeInvite(Player p) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET invite=NULL WHERE id=?;");
			ps.setString(1, p.getUniqueId().toString());
			ps.executeUpdate();
			closeConnection(ps);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection(con);
		}
	}
	
	public static void addFactionMember(String faction, Player p) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement uPS1 = con.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET faction=? WHERE id=?;");
			uPS1.setString(1, faction);
			uPS1.setString(2, p.getUniqueId().toString());

			removeInvite(p);

			PreparedStatement uPS2 = con.prepareStatement("UPDATE " + TABLE_FACTIONS + " SET maxclaims=maxclaims+10 WHERE name=?;");
			uPS2.setString(1, faction);


			uPS1.executeUpdate();
			uPS2.executeUpdate();

			closeConnection(uPS1, uPS2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection(con);
		}
	}

	/**
	 *
	 * @param faction
	 * @param p
	 * @return UUID of new leader if there is one, otherwise null
	 */
	public static UUID removeFactionMember(String faction, Player p) {
		Connection con = openConnection();
		try {

			Objects.requireNonNull(ScoreboardUtilities.board.getTeam(faction)).removeEntry(p.getName());
			assert con != null;
			PreparedStatement uPS1 = con.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET faction=NULL WHERE id=?;");
			uPS1.setString(1, p.getUniqueId().toString());

			PreparedStatement uPS2 = con.prepareStatement("UPDATE " + TABLE_FACTIONS + " SET maxclaims=maxclaims-10 WHERE name=?;");
			uPS2.setString(1, faction);
			
			uPS1.executeUpdate();
			uPS2.executeUpdate();

			PreparedStatement qPS1 = con.prepareStatement("SELECT numclaims, maxclaims FROM " + TABLE_FACTIONS + " WHERE name=?;");
			qPS1.setString(1, faction);

			ResultSet r = qPS1.executeQuery();
			if (r != null && !r.next()) {
				closeConnection(con, uPS1, uPS2, qPS1);
				return null;
			}

			assert r != null;
			int diff = r.getInt(1) - r.getInt(2);
			
			if (diff > 0) { // faction now has more claims than their maxclaims. remove claims until numclaims==maxclaims (diff == 0)

				qPS1 = con.prepareStatement("SELECT homeServer, homeWorld, homeX, homeZ FROM " + TABLE_FACTIONS + " WHERE name=?;");
				qPS1.setString(1, faction);

				r = qPS1.executeQuery();


				String server = null;
				String world = null;
				Integer x = null;
				Integer z = null;

				if (r != null && r.next()) {
					server = r.getString(1);
					world = r.getString(2);
					x = r.getInt(3);
					z = r.getInt(4);
				}

				uPS1 = con.prepareStatement("DELETE FROM " + TABLE_CLAIMED_CHUNKS + " WHERE faction=? AND NOT (server=? AND world=? AND (x*16)<? AND ((x*16)+15)>? AND (z*16)<? AND ((z*16)+15)>?) LIMIT " + diff + ";");
				uPS1.setString(1, faction);
				uPS1.setString(2, server);
				uPS1.setString(3, world);
				uPS1.setInt(4, x);
				uPS1.setInt(5, x);
				uPS1.setInt(6, z);
				uPS1.setInt(7, z);

				uPS2 = con.prepareStatement("UPDATE " + TABLE_FACTIONS + " SET numclaims=maxclaims WHERE name=?;");
				uPS2.setString(1, faction);

				uPS1.executeUpdate();
				uPS2.executeUpdate();

				Cache.chunks = getClaimedChunks();
			}
			
			if (isFactionLeader(p)) { // handle if leader leaves their faction. Appoint next in line.

				qPS1 = con.prepareStatement("SELECT id FROM " + TABLE_PLAYERS + " WHERE faction=?;");
				qPS1.setString(1, faction);
				r = qPS1.executeQuery();

				if (r != null && r.next()) {
					uPS1 = con.prepareStatement("UPDATE " + TABLE_FACTIONS + " SET leader=? WHERE name=?;");
					uPS1.setString(1, r.getString(1));
					uPS1.setString(2, faction);
					uPS1.executeUpdate();

					UUID u = UUID.fromString(r.getString(1));
					closeConnection(con, uPS1, uPS2, qPS1);
					return u;
				} else {

					Objects.requireNonNull(ScoreboardUtilities.board.getTeam(faction)).unregister();

					uPS1 = con.prepareStatement("DELETE FROM " + TABLE_CLAIMED_CHUNKS + " WHERE faction=?;");
					uPS1.setString(1, faction);

					uPS2 = con.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET invite=NULL WHERE invite=?;");
					uPS2.setString(1, faction);

					PreparedStatement uPS3 = con.prepareStatement("DELETE FROM " + TABLE_FACTIONS + " WHERE name=?;");
					uPS3.setString(1, faction);

					uPS1.executeUpdate();
					uPS2.executeUpdate();
					uPS3.executeUpdate();

					closeConnection(uPS3);
				}
			}

			closeConnection(con, qPS1, uPS1, uPS2);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection(con);
			return null;
		}
	}
	
	public static ArrayList<UUID> getFactionMembers(String faction) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT id FROM " + TABLE_PLAYERS + " WHERE faction=?;");
			ps.setString(1, faction);

			ResultSet r = ps.executeQuery();
			
			ArrayList<UUID> ret = new ArrayList<>();
			
			while (r != null && r.next()) {
				ret.add(UUID.fromString(r.getString(1)));
			}
			closeConnection(con, ps);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}

	public static ArrayList<String> getFactions() {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT name FROM " + TABLE_FACTIONS + ";");

			ResultSet r = ps.executeQuery();

			ArrayList<String> ret = new ArrayList<>();

			while (r != null && r.next()) {
				ret.add(r.getString(1));
			}

			closeConnection(con, ps);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}
	
	public static void addClaimedChunk(String faction, ClaimedChunk chunk) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement qPS1 = con.prepareStatement("SELECT numclaims, maxclaims FROM " + TABLE_FACTIONS + " WHERE name=?;");
			qPS1.setString(1, faction);
			
			ResultSet r = qPS1.executeQuery();
			
			if (r != null && (!r.next() || r.getInt(1) == r.getInt(2))) { // faction does not exist or faction has reached max claims
				closeConnection(con, qPS1);
				return;
			}

			PreparedStatement uPS1 = con.prepareStatement("INSERT INTO " + TABLE_CLAIMED_CHUNKS + " (server, world, x, z, faction) VALUES (?, '" + chunk.getWorld() + "', " + chunk.getX() + ", " + chunk.getZ() + ", '" + faction + "');");
			uPS1.setString(1, chunk.getServer());

			PreparedStatement uPS2 = con.prepareStatement("UPDATE " + TABLE_FACTIONS + " SET numclaims=numclaims+1 WHERE name=?;");
			uPS2.setString(1, faction); // update numclaims in factions table

			uPS1.executeUpdate();
			uPS2.executeUpdate();

			closeConnection(qPS1, uPS1, uPS2);
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
		}
	}
	
	public static void removeClaimedChunk(String faction, ClaimedChunk chunk) {
		Connection con = openConnection();
		try {
			assert con != null;
			PreparedStatement uPS1 = con.prepareStatement("DELETE FROM " + TABLE_CLAIMED_CHUNKS + " WHERE server=? AND world='" + chunk.getWorld() + "' AND x=" + chunk.getX() + " AND z=" + chunk.getZ() + " AND faction='" + faction + "';");
			uPS1.setString(1, chunk.getServer());

			PreparedStatement uPS2 = con.prepareStatement("UPDATE " + TABLE_FACTIONS + " SET numclaims=numclaims-1 WHERE name=?;");
			uPS2.setString(1, faction);

			uPS1.executeUpdate();
			uPS2.executeUpdate(); // update numclaims in factions table

			closeConnection(uPS1, uPS2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection(con);
		}
	}

	public static HashMap<ClaimedChunk, String> getClaimedChunks() {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + TABLE_CLAIMED_CHUNKS + " WHERE server=?;");
			ps.setString(1, plugin.getServer().getName());

			ResultSet r = ps.executeQuery();

			HashMap<ClaimedChunk, String> ret = new HashMap<>();

			while (r.next()) {
				ret.put(new ClaimedChunk(r.getString(1), r.getString(2), r.getInt(3), r.getInt(4)), r.getString(5));
			}
			closeConnection(con, ps);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}
	
	public static boolean chunkClaimed(ClaimedChunk chunk) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT * FROM " + TABLE_CLAIMED_CHUNKS + " WHERE server=? AND world=? AND x=" + chunk.getX() + " AND z=" + chunk.getZ() + ";");
			ps.setString(1, chunk.getServer());
			ps.setString(2, chunk.getWorld());

			ResultSet r = ps.executeQuery();

			boolean ret = (r != null && r.next());
			closeConnection(con, ps);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return false;
		}
	}
	
	public static boolean chunkOwnedByFaction(String faction, ClaimedChunk chunk) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT faction FROM " + TABLE_CLAIMED_CHUNKS + " WHERE server=? AND world=? AND x=" + chunk.getX() + " AND z=" + chunk.getZ() + ";");
			ps.setString(1, chunk.getServer());
			ps.setString(2, chunk.getWorld());

			ResultSet r = ps.executeQuery();
			if (r != null && r.next()) {
				boolean ret = r.getString(1).equals(faction);
				closeConnection(con, ps);
				return ret;
			} else {
				closeConnection(con, ps);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return false;
		}
	}
	
	public static boolean isFactionLeader(Player p) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT name FROM " + TABLE_FACTIONS + " WHERE leader=?;");
			ps.setString(1, p.getUniqueId().toString());

			ResultSet r = ps.executeQuery();
			boolean ret = r.next();
			closeConnection(con, ps);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return false;
		}
	}
	
	public static boolean inFaction(Player p) {
		return getPlayerFaction(p) != null;
	}
	
	public static boolean inFaction(String faction, Player p) {
		return faction.equals(getPlayerFaction(p));
	}
	
	public static void setLeader(String faction, Player p) {
		Connection con = openConnection();
		try {

			assert con != null;

			PreparedStatement qPS1 = con.prepareStatement("SELECT leader FROM " + TABLE_FACTIONS + " WHERE name=?");
			qPS1.setString(1, faction);

			ResultSet rs = qPS1.executeQuery();

			if (rs != null && rs.next()) {
				PreparedStatement uPS1 = con.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET canClaim=false, canInvite=false WHERE id=?");
				uPS1.setString(1, rs.getString(1));
				uPS1.executeUpdate();
				closeConnection(uPS1);
			}

			PreparedStatement uPS1 = con.prepareStatement("UPDATE " + TABLE_FACTIONS + " SET leader=? WHERE name=?;");
			uPS1.setString(1, p.getUniqueId().toString());
			uPS1.setString(2, faction);

			PreparedStatement uPS2 = con.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET canClaim=true, canInvite=true WHERE id=?;");
			uPS2.setString(1, p.getUniqueId().toString());

			uPS1.executeUpdate();
			uPS2.executeUpdate();

			closeConnection(con, uPS1, uPS2);
		} catch (Exception e) {
			closeConnection(con);
			e.printStackTrace();
		}
	}

	public static UUID getFactionLeader(String faction) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT leader FROM " + TABLE_FACTIONS + " WHERE name=?;");
			ps.setString(1, faction);

			ResultSet r = ps.executeQuery();
			if (r != null && r.next()) {
				UUID ret = UUID.fromString(r.getString(1));
				closeConnection(con, ps);
				return ret;
			} else {
				closeConnection(con, ps);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}
	
	public static String getPlayerFaction(Player p) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT faction FROM " + TABLE_PLAYERS + " WHERE id=?;");
			ps.setString(1, p.getUniqueId().toString());

			ResultSet r = ps.executeQuery();
			
			if (r != null && r.next()) {
				String ret = r.getString(1);
				closeConnection(con, ps);
				return ret;
			}

			closeConnection(con, ps);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}
	
	public static String getChunkFaction(ClaimedChunk c) {
		Connection con = openConnection();
		try {

			assert con != null;
			PreparedStatement ps = con.prepareStatement("SELECT faction FROM " + TABLE_CLAIMED_CHUNKS + " WHERE server=? AND world=? AND x=" + c.getX() + " AND z=" + c.getZ() + ";");
			ps.setString(1, c.getServer());
			ps.setString(2, c.getWorld());

			ResultSet r = ps.executeQuery();
			
			if (r != null && r.next()) {
				String ret = r.getString(1);
				closeConnection(con, ps);
				return ret;
			}

			closeConnection(con, ps);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection(con);
			return null;
		}
	}
}
