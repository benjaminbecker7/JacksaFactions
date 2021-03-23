package com.bmbecker.plugin.utilities;

public enum QueryResult {

	SUCCESS,
	ERROR,
	ALREADY_IN_FACTION, // Player is already in a general faction
	NAME_INVALID,		// Name contains non-SQL-friendly characters
	NAME_ALREADY_TAKEN, // Faction name has already been taken
	NOT_IN_FACTION,		// Player is not in a general faction
	NOT_FACTION_LEADER, // Player is not the leader of their faction
	NOT_IN_OWN_FACTION, // Player is not in the caller's faction
	FACTION_NOT_FOUND,	// Player tried to invoke a faction that does not exist
	NO_INVITE_RECEIVED;	// Player has not received an invite

}
