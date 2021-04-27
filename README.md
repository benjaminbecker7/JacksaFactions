# JacksaFactions
#### A multiserver faction plugin made for the Jacksa Minecraft Server

---

#### The Faction Model:
##### Basic information:
* Every faction has a name. 
	* Names cannot be longer than 8 characters.
	* Names cannot contain non-alphanumeric characters.
	* Names are appended to front of each member's name.
	* Names cannot be changed after faction is created.
* Every faction has a leader.
	* When a faction is created, the leader is the person who created it.
	* Leaders can invite or kick other members, and appoint a member as the new leader.
	* Leaders can claim chunks for their faction.
	* If the leader leaves their faction, another member will be appointed as the new leader.
* Every faction has members.
	* Members can build and destroy within their faction's domain.
	* Members can also leave their faction and list the names of the members of their faction.
	* Members are not allowed to hit one another unless in regions flagged by ops with `FACTION_PVP`.
* Faction leaders can set permissions for faction members within their factions.
  * If a leader turns on the `claim` permission for a faction member, that member can claim and unclaim land for their faction.
  * If a leader turns on the `invite` permission for a faction member, that member can invite people to their faction.
##### Domains:
* Leaders can claim chunks to be in the domain of their faction.
* One chunk cannot be owned by more than one faction.
* Only players in a faction can edit the land or interact with blocks in its domain.
* The number of chunks a faction can have in its domain is equal to the number of its members times 10.
* Leaders can also unclaim chunks if they want to remove a chunk from their domain.
* Leaders can set the faction home location in their domain. When the plugin removes claimed chunks if members leave the faction, the chunk that contains the fa tion home location will be the last one to be removed.

---

#### Commands:

Alias: can use either `/faction <arg(s)>` or `/f <arg(s)>` to execute the general commands.

##### General Commands:
* `/faction help`: Displays the argument formats and descriptions of commands in the plugin.
* `/faction create [name]`: Creates a new faction with the caller as the leader and the name as `[name]`.
* `/faction accept`: Accept invite from faction currently inviting the player.
* `/faction decline`: Decline invite from faction currently inviting the player.

##### Member Commands:
* `/faction leave`: Command for player to leave current faction. 
	* If caller is leader, appoints a random member as the new leader.
	* If caller is leader and there are no other members, it destroys the faction.
	* When a player leaves, the maximum number of claims for their faction is subtracted by 10. If the faction currently owns more claims than the new maximum, then claims are deleted until the number matches the new maximum.
* `/faction list`: Command for player to list members of caller's current faction.
* `/faction home`: Command for player to teleport to the home location set by the leader. If player is in a different server, BungeeCord moves the player to the home location's server.

##### Leader Commands:
* `/faction kick [name]`: Kicks player with name `[name]` from faction.
* `/faction appoint [name]`: Appoints the player in faction with name `[name]` as the new leader.
* `/faction permit [claim/invite] [on/off] [name]`: Sets ability for faction member with name `[name]` to claim/unclaim land or invite players to the faction.
* `/faction sethome`: Sets the home location for the faction. Home location must be in one of the faction's chunks.

##### Permitted Commands (Leaders are automatically given full permissions):
* For players with `claim` permission:
	* `/faction claim`: Claims the chunk the player is currently standing in to be in the faction's domain.
	* `/faction unclaim`: Removes the chunk the player is currently standing in from the faction's domain.
* For players with `invite` permission:
	* `/faction invite [name]`: Invites player with name `[name]` to join the caller's faction. Callable by faction leader.
	* `/faction invitenear`: Opens up a GUI menu of players not in factions within 10 meter radius.

##### Op Commands (note that the base command for Op commands are `/fadmin`):
* `/fadmin pvp [on/off] [world] [region]`: Sets WorldGuard flags that allow for players from the same faction to hit each other in chunks in world `[world]` containing region with ID `[region]`.
* `/fadmin claimable [on/off] [world] [region]`: Sets WorldGuard flags that allow for players to claim land in chunks in world `[world]` containing region with ID `[region]`.

---

#### SQL Server Specifications:

This plugin requires a MySQL database to maintain faction data across servers.

The servers using this plugin should specify the SQL connection details in the `config.yml` file generated when the plugin is run for the first time.

Furthermore, the SQL Database must be configured to have the following tables in its database:

**factions**
| name       | leader      | numclaims | maxclaims | homeServer  | homeWorld   | homeX    | homeY    | homeZ    |
|------------|-------------|-----------|-----------|-------------|-------------|----------|----------|----------|
| VARCHAR(8) | VARCHAR(36) | INT(255)  | INT(255)  | VARCHAR(50) | VARCHAR(50) | INT(255) | INT(255) | INT(255) |

**players**
| id          | faction    | invite     | canInvite | canClaim |
|-------------|------------|------------|-----------|----------|
| VARCHAR(36) | VARCHAR(8) | VARCHAR(8) | BOOLEAN   | BOOLEAN  |

**claimed_chunks**
| server      | world       | x        | z        | faction    |
|-------------|-------------|----------|----------|------------|
| VARCHAR(50) | VARCHAR(50) | INT(255) | INT(255) | VARCHAR(8) |
