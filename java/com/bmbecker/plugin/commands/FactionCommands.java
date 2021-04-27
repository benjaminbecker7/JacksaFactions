package com.bmbecker.plugin.commands;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import com.bmbecker.plugin.guis.InviteGui;
import com.bmbecker.plugin.objects.FactionHome;
import com.bmbecker.plugin.utilities.Cache;
import com.bmbecker.plugin.utilities.ScoreboardUtilities;
import com.bmbecker.plugin.utilities.perms.PermSet;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.bmbecker.plugin.objects.ClaimedChunk;
import com.bmbecker.plugin.utilities.SQLUtilities;
import com.bmbecker.plugin.utilities.WorldGuardUtilities;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

public class FactionCommands implements CommandExecutor {

    private static JavaPlugin plugin;

    public FactionCommands(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // Overarching command
        if (cmd.getName().equalsIgnoreCase("faction") || cmd.getName().equalsIgnoreCase("f")) {
            switch(args.length) {
                case 0:
                    return sendCommandInfo(player);
                case 1:
                    switch(args[0].toLowerCase()) {
                        case "help":
                            return sendCommandInfo(player);
                        case "leave":
                            return leave(player);
                        case "map":
                            return map(player);
                        case "list":
                            return list(player);
                        case "claim":
                            return claim(player);
                        case "unclaim":
                            return unclaim(player);
                        case "accept":
                            return accept(player);
                        case "decline":
                            return decline(player);
                        case "here":
                            return here(player);
                        case "invitenear":
                            return inviteNear(player);
                        case "permissions":
                            return permissions(player);
                        case "sethome":
                            return setHome(player);
                        case "home":
                            return home(player);
                        default:
                            return sendArgsNotRecognized(player);
                    }
                case 2:
                    switch(args[0].toLowerCase()) {
                        case "create":
                            return create(player, args[1]);
                        case "appoint":
                            return appoint(player, args[1]);
                        case "kick":
                            return kick(player, args[1]);
                        case "invite":
                            return invite(player, args[1]);
                        case "get":
                            return get(player, args[1]);
                        default:
                            return sendArgsNotRecognized(player);
                    }
                case 4:
                    if (args[0].equalsIgnoreCase("permit")) {
                        return permit(player, args);
                    } else {
                        return sendArgsNotRecognized(player);
                    }
                default:
                    return sendArgsNotRecognized(player);
            }
        } else if (cmd.getName().equalsIgnoreCase("fadmin")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    return sendAdminCommandInfo(player);
                } else {
                    return sendAdminArgsNotRecognized(player);
                }
            } else if (args.length == 4) {
                switch(args[0].toLowerCase()) {
                    case "pvp":
                        return setPvpFlag(player, args);
                    case "claimable":
                        return setClaimableFlag(player, args);
                    default:
                        return sendAdminArgsNotRecognized(player);
                }
            } else {
                return sendAdminArgsNotRecognized(player);
            }
        }

        return true;
    }

    private boolean sendArgsNotRecognized(Player player) {
        player.sendMessage(ChatColor.RED + "Arguments not recognized for faction command. Run '/faction help' or '/f help' for list of available commands.");
        return true;
    }

    private boolean sendAdminArgsNotRecognized(Player player) {
        player.sendMessage(ChatColor.RED + "Arguments not recognized for fadmin command. Run '/fadmin help' for list of available commands.");
        return true;
    }

    private boolean sendCommandInfo(Player player) {

        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "JacksaFactions Plugin");
        player.sendMessage(ChatColor.GREEN + "Usage:");
        player.sendMessage("/faction <arg>");
        player.sendMessage("/f <arg>");
        player.sendMessage(ChatColor.GREEN + "Arguments:");

        player.sendMessage(ChatColor.AQUA + "help" + ChatColor.WHITE + ": view this screen again.");
        player.sendMessage(ChatColor.AQUA + "get <name>" + ChatColor.WHITE + ": gets the name of player <name>'s faction.");
        player.sendMessage(ChatColor.AQUA + "here" + ChatColor.WHITE + ": gets the name of the faction that has claimed the current chunk.");
        player.sendMessage(ChatColor.AQUA + "map" + ChatColor.WHITE + ": gets a 15x15 map of nearby chunks showing claims.");

        String faction = Cache.getFaction(player.getUniqueId());

        if (faction == null) {
            player.sendMessage(ChatColor.AQUA + "create <name>" + ChatColor.WHITE + ": create new faction with name <name>. Names can be a max of 8 characters.");
            player.sendMessage(ChatColor.AQUA + "accept" + ChatColor.WHITE + ": accept pending invite from faction with name <faction>.");
            player.sendMessage(ChatColor.AQUA + "decline" + ChatColor.WHITE + ": decline pending invite from faction with name <faction>.");
        } else {
            player.sendMessage(ChatColor.AQUA + "leave" + ChatColor.WHITE + ": leave current faction.");
            player.sendMessage(ChatColor.AQUA + "list" + ChatColor.WHITE + ": list players in current faction.");
            player.sendMessage(ChatColor.AQUA + "home" + ChatColor.WHITE + ": teleport to the faction's home.");

            if(Cache.getPerms(player.getUniqueId()).canClaim) {
                player.sendMessage(ChatColor.AQUA + "claim" + ChatColor.WHITE + ": claim the chunk you are in for your faction.");
                player.sendMessage(ChatColor.AQUA + "unclaim" + ChatColor.WHITE + ": remove the chunk you are in from your faction's domain.");
            }

            if(Cache.getPerms(player.getUniqueId()).canInvite) {
                player.sendMessage(ChatColor.AQUA + "invite <name>" + ChatColor.WHITE + ": invite player with name <name> to your faction.");
                player.sendMessage(ChatColor.AQUA + "invitenear" + ChatColor.WHITE + ": open menu with players within 10 blocks who you can invite.");
            }

            if (Cache.getPerms(player.getUniqueId()).isLeader) {
                player.sendMessage(ChatColor.AQUA + "appoint <name>" + ChatColor.WHITE + ": appoint member of faction with name <name> to be your faction's new leader.");
                player.sendMessage(ChatColor.AQUA + "kick <name>" + ChatColor.WHITE + ": kick member of faction with name <name> from your faction.");
                player.sendMessage(ChatColor.AQUA + "permit <claim/invite> <on/off> <name>" + ChatColor.WHITE + ": set claiming or inviting permissions for faction member with name <name>.");
                player.sendMessage(ChatColor.AQUA + "sethome" + ChatColor.WHITE + ": set the location of your faction's home.");

            }
        }

        return true;
    }

    private boolean sendAdminCommandInfo(Player player) {

        if (player.isOp()) {

            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Admin Commands for the JacksaFactions Plugin");
            player.sendMessage(ChatColor.GREEN + "Usage:");
            player.sendMessage("/fadmin <arg>");
            player.sendMessage(ChatColor.GREEN + "Arguments:");

            player.sendMessage(ChatColor.AQUA + "pvp <on/off> <world> <region>" + ChatColor.WHITE + ": Sets WorldGuard flag that allows for players from the same faction to hit each other in chunks in world <world> containing region with ID <region>.");
            player.sendMessage(ChatColor.AQUA + "claimable <on/off> <world> <region>" + ChatColor.WHITE + ": Sets WorldGuard flag that allows for players to claim land in chunks in world <world> containing region with ID <region>.");
        } else {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        }
        return true;
    }

    private boolean leave(Player player) {
        String faction = Cache.getFaction(player.getUniqueId());
        if (faction == null) { // Player is not in a faction
            player.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        Cache.putFaction(player.getUniqueId(), null);
        Cache.putPerms(player.getUniqueId(), new PermSet(false, false, false));
        ScoreboardUtilities.board.getTeam(faction).removeEntry(player.getName());

        player.sendMessage("You have left your faction.");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                UUID u = SQLUtilities.removeFactionMember(faction, player);

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    public void run() {
                        if (u != null) {
                            Cache.putPerms(u, new PermSet(true, true, true));
                            Bukkit.getPlayer(u).sendMessage("You have been appointed as the new leader of your faction!");
                        }
                    }
                });

            }
        });


        return true;
    }

    private boolean map(Player player) {
        player.sendMessage("==============MAP==============");
        Chunk playerChunk = player.getLocation().getChunk();
        char playerChar;
        switch(player.getFacing()) {
            case EAST:
            case EAST_NORTH_EAST:
            case EAST_SOUTH_EAST:
                playerChar = '>';
                break;
            case WEST:
            case WEST_NORTH_WEST:
            case WEST_SOUTH_WEST:
                playerChar = '<';
                break;
            case NORTH:
            case NORTH_EAST:
            case NORTH_NORTH_EAST:
            case NORTH_NORTH_WEST:
            case NORTH_WEST:
                playerChar = '^';
                break;
            case SOUTH:
            case SOUTH_EAST:
            case SOUTH_SOUTH_EAST:
            case SOUTH_SOUTH_WEST:
            case SOUTH_WEST:
                playerChar = 'V';
                break;
            default:
                playerChar = '@';
        }

        ClaimedChunk start = new ClaimedChunk(Bukkit.getServer().getName(), playerChunk.getWorld().getName(), playerChunk.getX() - 7, playerChunk.getZ() - 7);

        HashMap<String, Character> factionToChar = new HashMap<>();

        char emptySpace = 'o';
        String possibleChars = "1234567890QWERTYUIOPASDFGHJKLZXCBNM#$%&*{}[]+=~∑†¥πåß∂ƒ©∆¬…Ω≈ç√∫µ";
        int fCharIndex = 0;

        for (int i = 0; i < 15; i++) {
            String line = "";
            for(int j = 0; j < 15; j++) {
                ClaimedChunk mapChunk = new ClaimedChunk(start.getServer(), start.getWorld(), start.getX() + j, start.getZ() + i);
                String faction = Cache.get(mapChunk);
                if (i == 7 && j == 7) {
                    line += playerChar;
                } else if (faction == null) {
                    line += emptySpace;
                } else {
                    if (factionToChar.containsKey(faction)) {
                        line += factionToChar.get(faction);
                    } else {
                        if (fCharIndex >= possibleChars.length()) {
                            line += 'Ø';
                        } else {
                            factionToChar.put(faction, possibleChars.charAt(fCharIndex));
                            line += possibleChars.charAt(fCharIndex);
                            fCharIndex++;
                        }
                    }
                }
                line += ' ';
            }
            player.sendMessage(line);
        }

        player.sendMessage("===============================");
        player.sendMessage("KEY:");
        String key = "| You: " + playerChar + " | Wilderness: " + emptySpace + " | ";

        for (String f : factionToChar.keySet()) {
            key += f + ": " + factionToChar.get(f) + " | ";
        }

        player.sendMessage(key);

        return true;
    }

    private boolean list(Player player) {
        String faction = Cache.getFaction(player.getUniqueId());
        if (faction == null) { // Player is not in a faction
            player.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Members of your faction:");
        ArrayList<UUID> members = Cache.getFactionMembers(faction);
        for (UUID uuid : members) {
            Player member = Bukkit.getPlayer(uuid);
            if (Cache.getPerms(uuid).isLeader) {
                player.sendMessage(member.getName() + " [leader]");
            } else {
                player.sendMessage(member.getName());
            }
        }

        return true;
    }

    private boolean claim(Player player) {
        String faction = Cache.getFaction(player.getUniqueId());

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        if (!Cache.getPerms(player.getUniqueId()).canClaim) {
            player.sendMessage(ChatColor.RED + "You cannot claim land for your faction.");
            return true;
        }

        // CHECK IF PLAYER IS IN A FACTION_CLAIMABLE ZONE. IF NO, PLAYER CANNOT CLAIM
        Chunk currChunk = player.getLocation().getChunk();
        int bx = currChunk.getX() << 4;
        int bz = currChunk.getZ() << 4;
        BlockVector3 pt1 = BlockVector3.at(bx, 0, bz);
        BlockVector3 pt2 = BlockVector3.at(bx + 15, 256, bz + 15);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("ID", pt1, pt2);
        RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));

        ApplicableRegionSet regionSet = regions.getApplicableRegions(region);
        Iterator<ProtectedRegion> appRegions = regionSet.iterator();

        if (appRegions.hasNext() && appRegions.next().getFlag(WorldGuardUtilities.FACTION_CLAIMABLE) == StateFlag.State.DENY) {
            player.sendMessage(ChatColor.RED + "This chunk is in an unclaimable region.");
            return true;
        }

        ClaimedChunk dummyChunk = ClaimedChunk.parseClaimedChunk(currChunk);

        if (Cache.contains(dummyChunk)) {
            player.sendMessage(ChatColor.RED + "This chunk has already been claimed.");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                if (SQLUtilities.getNumClaims(faction) == SQLUtilities.getMaxClaims(faction)) {
                    player.sendMessage(ChatColor.RED + "Your faction has reached its max number of chunk claims. Add more members to increase the amount you can claim.");
                    return;
                }

                SQLUtilities.addClaimedChunk(faction, dummyChunk);

                Cache.put(dummyChunk, faction);

                player.sendMessage("You have claimed this chunk for your faction's territory.");

            }
        });

        return true;
    }

    private boolean unclaim(Player player) {


        String faction = Cache.getFaction(player.getUniqueId());

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        if (!Cache.getPerms(player.getUniqueId()).canClaim) {
            player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
            return true;
        }

        Chunk currChunk = player.getLocation().getChunk();

        ClaimedChunk dummyChunk = ClaimedChunk.parseClaimedChunk(currChunk);

        if(!Cache.contains(dummyChunk)) {
            player.sendMessage(ChatColor.RED + "This chunk has not been claimed.");
            return true;
        }

        if (!Cache.get(dummyChunk).equals(faction)) {
            player.sendMessage(ChatColor.RED + "Your faction does not own this chunk.");
            return true;
        }

        player.sendMessage("You have removed this chunk from your faction's territory.");

        Cache.remove(dummyChunk);


        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                SQLUtilities.removeClaimedChunk(faction, dummyChunk);
            }
        });

        return true;
    }

    private boolean accept(Player player) {

        String faction = Cache.getInvite(player.getUniqueId());

        if (Cache.getFaction(player.getUniqueId()) != null) { // Player is already in a faction
            player.sendMessage(ChatColor.RED + "You are already in a faction.");
            return true;
        }

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "Invite not found.");
            return true;
        }

        ScoreboardUtilities.board.getTeam(faction).addEntry(player.getName());

        Cache.putFaction(player.getUniqueId(), faction);
        Cache.putInvite(player.getUniqueId(), null);

        player.sendMessage(ChatColor.GREEN + "You have been added to the faction " + faction);


        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {

                SQLUtilities.addFactionMember(faction, player);

                SQLUtilities.removeInvite(player);

                UUID fLeader = SQLUtilities.getFactionLeader(faction);

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    public void run() {
                        Player leader = Bukkit.getPlayer(fLeader);
                        if(leader != null) {
                            leader.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " has accepted your faction invitation.");
                        }
                    }
                });
            }
        });

        return true;
    }

    private boolean decline(Player player) {

        String faction = Cache.getInvite(player.getUniqueId());

        if (Cache.getFaction(player.getUniqueId()) != null) { // Player is already in a faction
            player.sendMessage(ChatColor.RED + "You are already in a faction.");
            return true;
        }

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "Invite not found.");
            return true;
        }

        player.sendMessage("You have declined the invite from " + faction);

        Cache.putInvite(player.getUniqueId(), null);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                SQLUtilities.removeInvite(player);

                UUID fLeader = SQLUtilities.getFactionLeader(faction);

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    public void run() {

                        Player leader = Bukkit.getPlayer(fLeader);
                        if(leader != null) {
                            leader.sendMessage(player.getName() + " has declined your invitation");
                        }

                    }
                });


            }
        });

        return true;
    }

    private boolean here(Player player) {

        ClaimedChunk dummyChunk = ClaimedChunk.parseClaimedChunk(player.getLocation().getChunk());

        if (Cache.contains(dummyChunk)) {
            player.sendMessage("This chunk has been claimed by " + Cache.get(dummyChunk) + ".");
            return true;
        }
        String faction = Cache.get(dummyChunk);

        if (faction == null) {
            player.sendMessage("This chunk has not been claimed.");
            return true;
        }

        player.sendMessage("This chunk has been claimed by " + faction + ".");

        return true;
    }

    private boolean inviteNear(Player player) {

        if(!Cache.getPerms(player.getUniqueId()).canInvite) {
            player.sendMessage(ChatColor.RED + "You cannot invite players to your faction.");
            return true;
        }

        InviteGui gui = new InviteGui(plugin, player);
        plugin.getServer().getPluginManager().registerEvents(gui, plugin);
        gui.openInventory();

        return true;
    }

    private boolean permissions(Player player) {

        if(Cache.getFaction(player.getUniqueId()) == null) {
            player.sendMessage("You are not in a faction.");
            return true;
        }

        PermSet permissions = Cache.getPerms(player.getUniqueId());

        if(!permissions.canClaim && !permissions.canInvite) {
            player.sendMessage("You have no permissions in your faction.");
        } else {
            if (permissions.canClaim) {
                player.sendMessage("You can claim and unclaim land for your faction.");
            }
            if (permissions.canInvite) {
                player.sendMessage("You can invite players to your faction.");
            }
        }
        return true;
    }

    private boolean setHome(Player player) {
        String faction = Cache.getFaction(player.getUniqueId());

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        if (!Cache.getPerms(player.getUniqueId()).isLeader) {
            player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
            return true;
        }

        Location homeLoc = player.getLocation();

        ClaimedChunk dummyChunk = ClaimedChunk.parseClaimedChunk(homeLoc.getChunk());
        if (!Cache.contains(dummyChunk) || !Cache.get(dummyChunk).equals(faction)) {
            player.sendMessage("You cannot set your faction home outside of your faction's territory.");
            return true;
        }

        String server = Bukkit.getServer().getName();
        String world = homeLoc.getWorld().getName();
        int x = homeLoc.getBlockX();
        int y = homeLoc.getBlockY();
        int z = homeLoc.getBlockZ();

        player.sendMessage("You have set the home location of your faction.");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                SQLUtilities.setHome(faction, new FactionHome(server, world, x, y, z));
            }
        });

        return true;
    }

    private boolean home(Player player) {
        String faction = Cache.getFaction(player.getUniqueId());

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        String server = Bukkit.getServer().getName();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                FactionHome home = SQLUtilities.getHome(faction);

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    public void run() {

                        if(home == null) {
                            player.sendMessage(ChatColor.RED + "Your faction does not have a home set.");
                            return;
                        }

                        if (!home.getServer().equals(server)) {
                            ByteArrayOutputStream b = new ByteArrayOutputStream();
                            DataOutputStream out = new DataOutputStream(b);
                            try {
                                out.writeUTF("Connect");
                                out.writeUTF(home.getServer());
                            } catch (IOException e) {
                                e.printStackTrace();
                                return;
                            }
                            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                        }

                        player.teleport(new Location(Bukkit.getWorld(home.getWorld()), home.getX(), home.getY(), home.getZ()));
                    }
                });
            }
        });

        return true;
    }

    private boolean create(Player player, String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                switch (SQLUtilities.createFaction(name, player)) {
                    case SUCCESS:
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            public void run() {
                                Cache.putFaction(player.getUniqueId(), name);
                                Cache.putPerms(player.getUniqueId(), true, true, true);
                                ScoreboardUtilities.addTeam(name, player);
                                player.sendMessage(ChatColor.GREEN + "You have created a new faction with name " + name);
                            }
                        });
                        return;
                    case ALREADY_IN_FACTION:
                        player.sendMessage(ChatColor.RED + "You are already in a faction.");
                        return;
                    case NAME_INVALID:
                        player.sendMessage(ChatColor.RED + "Faction names can be no longer than 8 characters and cannot contain \"\"\", \";\", or \"'\".");
                        return;
                    case NAME_ALREADY_TAKEN:
                        player.sendMessage(ChatColor.RED + "Name has already been taken.");
                        return;
                    default:
                        player.sendMessage(ChatColor.RED + "SQL QUERY ERROR: Please notify server admins.");
                        return;
                }
            }
        });

        return true;
    }

    private boolean appoint(Player player, String appointeeName) {
        String faction = Cache.getFaction(player.getUniqueId());

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        if (!Cache.getPerms(player.getUniqueId()).isLeader) {
            player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
            return true;
        }

        Player appointee = Bukkit.getPlayer(appointeeName);

        if (appointee == null) {
            player.sendMessage(ChatColor.RED + "Player " + appointeeName + " not found.");
            return true;
        }

        if (!Cache.getFaction(player.getUniqueId()).equals(faction)) {
            player.sendMessage(ChatColor.RED + "Player " + appointeeName + " is not in your faction.");
            return true;
        }

        player.sendMessage("You have appointed " + appointeeName + " as the new leader of your faction.");

        appointee.sendMessage("You have been appointed to be the new leader of your factions.");

        Cache.putPerms(player.getUniqueId(), false, false, false);
        Cache.putPerms(appointee.getUniqueId(), true, true, true);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                SQLUtilities.setLeader(faction, player);
            }
        });

        return true;
    }

    private boolean kick(Player player, String removeeName) {
        String faction = Cache.getFaction(player.getUniqueId());

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        if (!Cache.getPerms(player.getUniqueId()).isLeader) {
            player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
            return true;
        }

        Player removee = Bukkit.getPlayerExact(removeeName);

        if (removee == null) {
            player.sendMessage(ChatColor.RED + "Player " + removeeName + " not found.");
            return true;
        }

        if (!Cache.contains(removee.getUniqueId()) || !Cache.getFaction(removee.getUniqueId()).equals(faction)) {
            player.sendMessage(ChatColor.RED + "Player " + removeeName + " is not in your faction.");
            return true;
        }

        ScoreboardUtilities.board.getTeam(faction).removeEntry(player.getName());
        Cache.putFaction(removee.getUniqueId(), null);
        Cache.putPerms(removee.getUniqueId(), false, false, false);

        player.sendMessage("Player " + removeeName + " has been removed from your faction.");

        removee.sendMessage("You have been removed from the faction " + faction);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                SQLUtilities.removeFactionMember(faction, removee);
            }
        });

        return true;
    }

    public static boolean invite(Player player, String inviteeName) {

        Player invitee = Bukkit.getPlayerExact(inviteeName); // get player specified in invite command

        if (invitee == null) { // Player not found
            player.sendMessage(ChatColor.RED + "Player " + inviteeName + " not found.");
            return true;
        }

        if (Cache.getFaction(invitee.getUniqueId()) != null) { // Player already in faction
            player.sendMessage(ChatColor.RED + "Player " + inviteeName + " is already in a faction.");
            return true;
        }

        String faction = Cache.getFaction(player.getUniqueId());

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        if (!Cache.getPerms(player.getUniqueId()).canInvite) {
            player.sendMessage(ChatColor.RED + "You are not permitted to invite players to your faction.");
            return true;
        }

        if (Cache.getInvite(invitee.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Player already has a pending invite from a faction.");
            return true;
        }

        TextComponent message1 = new TextComponent(ChatColor.BOLD + "" + ChatColor.GREEN + "You have been invited to join the faction " + faction + ". You may only have one pending invite at a time.");
        TextComponent message2 = new TextComponent(ChatColor.GREEN + "To accept this invitation, click this message or type '/factions accept' or '/f accept'");
        message2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f accept"));
        message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to accept invite!")));
        TextComponent message3 = new TextComponent(ChatColor.GREEN + "To decline this invitation, click this message or type '/factions decline' or '/f decline'");
        message3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f decline"));
        message3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to decline invite!")));

        invitee.spigot().sendMessage(message1);
        invitee.spigot().sendMessage(message2);
        invitee.spigot().sendMessage(message3);

        Cache.putInvite(invitee.getUniqueId(), faction);

        player.sendMessage("Invite sent.");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                SQLUtilities.addInvite(faction, invitee);
            }
        });

        return true;
    }

    private boolean get(Player player, String name) {

        Player target = Bukkit.getPlayerExact(name); // get player specified in invite command

        if (target == null) { // Player not found
            player.sendMessage(ChatColor.RED + "Player " + name + " not found.");
            return true;
        }
        String faction = Cache.getFaction(target.getUniqueId());

        if (faction == null) {
            player.sendMessage(name + " is not in a faction.");
            return true;
        }

        player.sendMessage(name + " is in faction with name " + faction + ".");

        return true;
    }

    private boolean permit(Player player, String[] args) {
        if (args[1].equalsIgnoreCase("claim")) {
            if(args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("off")) {

                String faction = Cache.getFaction(player.getUniqueId());

                if (faction == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a faction.");
                    return true;
                }

                if(!Cache.getPerms(player.getUniqueId()).isLeader) {
                    player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
                    return true;
                }

                Player permitee = Bukkit.getPlayerExact(args[3]);

                if (permitee == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                if (!Cache.contains(permitee.getUniqueId()) || !Cache.getFaction(permitee.getUniqueId()).equals(faction)) {
                    player.sendMessage(ChatColor.RED + "Player is not in your faction.");
                    return true;
                }

                Cache.putPerms(permitee.getUniqueId(), new PermSet(Cache.getPerms(permitee.getUniqueId()).isLeader, args[2].equalsIgnoreCase("on") ? true : false, Cache.getPerms(permitee.getUniqueId()).canInvite));

                permitee.sendMessage(ChatColor.GREEN + "Your faction permissions have changed. Enter '/faction permissions' or '/f permissions' to see changes.");

                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    public void run() {
                        SQLUtilities.setPerms(permitee, args[2].equalsIgnoreCase("on") ? true : false, Cache.getPerms(permitee.getUniqueId()).canInvite);
                    }
                });

            } else {
                return sendArgsNotRecognized(player);
            }
        } else if (args[1].equalsIgnoreCase("invite")) {
            if(args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("off")) {

                String faction = Cache.getFaction(player.getUniqueId());

                if (faction == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a faction.");
                    return true;
                }

                if(!Cache.getPerms(player.getUniqueId()).isLeader) {
                    player.sendMessage(ChatColor.RED + "You are not the leader of your faction.");
                    return true;
                }

                Player permitee = Bukkit.getPlayerExact(args[3]);

                if (permitee == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                if (!Cache.contains(permitee.getUniqueId()) || !Cache.getFaction(permitee.getUniqueId()).equals(faction)) {
                    player.sendMessage(ChatColor.RED + "Player is not in your faction.");
                    return true;
                }

                Cache.putPerms(permitee.getUniqueId(), new PermSet(Cache.getPerms(permitee.getUniqueId()).isLeader, Cache.getPerms(permitee.getUniqueId()).canClaim, args[2].equalsIgnoreCase("on") ? true : false));

                permitee.sendMessage(ChatColor.GREEN + "Your faction permissions have changed. Enter '/faction permissions' or '/f permissions' to see changes.");

                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    public void run() {
                        SQLUtilities.setPerms(permitee, Cache.getPerms(permitee.getUniqueId()).canClaim, args[2].equalsIgnoreCase("on") ? true : false);
                    }
                });

            } else {
                return sendArgsNotRecognized(player);
            }
        } else {
            return sendArgsNotRecognized(player);
        }

        return true;
    }

    private boolean setPvpFlag(Player player, String[] args) {
        if(!player.isOp()) { // verify player is op
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Non-Op player " + player.getName() + " tried to call an Op command.");
            player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
            return true;
        }

        if (args[1].equalsIgnoreCase("on")) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            World world = Bukkit.getWorld(args[2]);

            if (world == null) {
                player.sendMessage(ChatColor.RED + "Could not find world");
                return true;
            }

            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                player.sendMessage(ChatColor.RED + "Could not find world");
                return true;
            }

            ProtectedRegion region = regions.getRegion(args[3]);

            if (region == null) {
                player.sendMessage(ChatColor.RED + "Could not find region");
                return true;
            }

            region.setFlag(WorldGuardUtilities.FACTION_PVP, StateFlag.State.ALLOW);

            player.sendMessage("Enabling faction pvp in region " + args[3]);
        } else if (args[1].equalsIgnoreCase("off")) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            World world = Bukkit.getWorld(args[2]);

            if (world == null) {
                player.sendMessage(ChatColor.RED + "Could not find world");
                return true;
            }

            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                player.sendMessage(ChatColor.RED + "Could not find world");
                return true;
            }

            ProtectedRegion region = regions.getRegion(args[3]);

            if (region == null) {
                player.sendMessage(ChatColor.RED + "Could not find region");
                return true;
            }

            region.setFlag(WorldGuardUtilities.FACTION_PVP, StateFlag.State.DENY);

            player.sendMessage("Preventing faction pvp in region " + args[3]);
        }
        return true;
    }

    private boolean setClaimableFlag(Player player, String[] args) {
        if(!player.isOp()) { // verify player is op
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[JacksaFactions]: Non-Op player " + player.getName() + " tried to call an Op command.");
            player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
            return true;
        }

        if (args[1].equalsIgnoreCase("on")) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            World world = Bukkit.getWorld(args[2]);

            if (world == null) {
                player.sendMessage(ChatColor.RED + "Could not find world");
                return true;
            }

            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                player.sendMessage(ChatColor.RED + "Could not find world");
                return true;
            }

            ProtectedRegion region = regions.getRegion(args[3]);

            if (region == null) {
                player.sendMessage(ChatColor.RED + "Could not find region");
                return true;
            }

            region.setFlag(WorldGuardUtilities.FACTION_CLAIMABLE, StateFlag.State.ALLOW);

            player.sendMessage("Enabling faction claims in region " + args[3]);
        } else if (args[1].equalsIgnoreCase("off")) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            World world = Bukkit.getWorld(args[2]);

            if (world == null) {
                player.sendMessage(ChatColor.RED + "Could not find world");
                return true;
            }

            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) {
                player.sendMessage(ChatColor.RED + "Could not find world");
                return true;
            }

            ProtectedRegion region = regions.getRegion(args[3]);

            if (region == null) {
                player.sendMessage(ChatColor.RED + "Could not find region");
                return true;
            }

            region.setFlag(WorldGuardUtilities.FACTION_CLAIMABLE, StateFlag.State.DENY);

            player.sendMessage("Preventing faction claims in region " + args[3]);
        }

        return true;
    }



}