package com.bmbecker.plugin.utilities;

public enum QueryResult {

	SUCCESS,
	ERROR,
	ALREADY_IN_FACTION, // Player is already in a general faction
	NAME_INVALID,		// Name contains non-SQL-friendly characters
	NAME_ALREADY_TAKEN, // Faction name has already been taken

}
