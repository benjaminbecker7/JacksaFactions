# JacksaFactions
#### A faction plugin made for the Jacksa Minecraft Server

---

#### The Faction Model:
##### Basic information:
* Every faction has a name. 
	* Names cannot be longer than 8 characters.
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
	* Members are not allowed to hit one another unless in regions flagged by ops with FACTION_PVP.
##### Domains:
* Leaders can claim chunks to be in the domain of their faction.
* One chunk cannot be owned by more than one faction.
* Only players in a faction can edit the land or interact with blocks in its domain.
* The number of chunks a faction can have in its domain is equal to the number of its members times 10.
* Leaders can also unclaim chunks if they want to remove a chunk from their domain.

---

#### Commands:
##### General Commands:
* `/faction create [name]`: Creates a new faction with the caller as the leader and the name as `[name]`.
* `/faction accept [faction]`: Accept invite from faction `[faction]`.
* `/faction decline [faction]`: Decline invite from faction `[faction]`.

##### Member Commands:
* `/faction leave`: Command for player to leave current faction. 
	* If caller is leader, appoints a random member as the new leader.
	* If caller is leader and there are no other members, it destroys the faction.
* `/faction list`: Command for player to list members of caller's current faction.

##### Leader Commands:
* `/faction invite [name]`: Invites player with name `[name]` to join the caller's faction. Callable by faction leader.
* `/faction kick [name]`: Kicks player with name `[name]` from faction.
* `/faction appoint [name]`: Appoints the player in faction with name `[name]` as the new leader.
* `/faction claim`: Claims the chunk the player is currently standing in to be in the faction's domain.
* `/faction unclaim`: Removes the chunk the player is currently standing in from the faction's domain.

##### Op Commands:
* `/faction pvp [on/off] [world] [region]`: Sets WorldGuard flags that allow for players from the same faction to hit each other in chunks in world `[world]` containing region with ID `[region]`.
* `/faction claimable [on/off] [world] [region]`: Sets WorldGuard flags that allow for players to claim land in chunks in world `[world]` containing region with ID `[region]`.

