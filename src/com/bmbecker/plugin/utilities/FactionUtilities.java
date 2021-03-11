package com.bmbecker.plugin.utilities;

import java.util.ArrayList;
import org.bukkit.entity.Player;

import com.bmbecker.plugin.objects.Faction;

/**
 * Holds all methods that allow for the plugin manage factions
 * @author benjamin_becker
 *
 */
public class FactionUtilities {
	
	public static ArrayList<Faction> factions = new ArrayList<Faction>();
	
	/**
	 * Look up faction index by player UUID
	 * @param member
	 * @return
	 */
	public static int getFactionIndexByPlayer(Player player) {
		for (int i = 0; i < factions.size(); i++) {
			if (factions.get(i).isInFaction(player)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static boolean inFaction(Player player) {
		for (Faction faction : factions) {
			if (faction.isInFaction(player)) {
				return true;
			}
		}
		return false;
	}

	public static boolean nameAvailable(String name) {
		for (Faction f : factions) {
			if (f.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	public static int getFactionIndexByName(String name) {
		for (int i = 0; i < factions.size(); i++) {
			if (factions.get(i).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
}
