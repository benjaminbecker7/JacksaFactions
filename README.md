# JacksaFactions
#### A faction plugin made for the Jacksa Minecraft Server
---
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


