package com.bmbecker.plugin.utilities;

import com.bmbecker.plugin.objects.ClaimedChunk;
import com.bmbecker.plugin.objects.FactionHome;
import com.bmbecker.plugin.utilities.perms.PermSet;

import java.util.*;

public class Cache {

    protected static HashMap<UUID, String> playersToFactions = new HashMap<>();
    protected static HashMap<UUID, String> playersToInvites = new HashMap<>();
    protected static HashMap<UUID, PermSet> playersToPerms = new HashMap<>();
    protected static HashMap<ClaimedChunk, String> chunks = new HashMap<>();

    //TODO: MAKE SURE THAT CHUNKS IS TRACKING CLAIM DELETIONS IN TABLES WITH ARRAYLIST IN CHUNKTOFACTION. POSSIBLY LOAD IN
    //TODO: FROM SQL TO HASHMAP W/ ARRAYLIST AS WELL?
    //TODO: ALSO NEEDS TO DELETE OR MOVE HOME IF CHUNK DELETED, OR MAYBE JUST IGNORE FACTION HOME UNTIL ALL CHUNKS GONE

    public static void load(HashMap<UUID, String> playersToFactions, HashMap<UUID, String> playersToInvites, HashMap<UUID, PermSet> playersToPerms, HashMap<ClaimedChunk, String> chunks) {
        if (playersToFactions != null) {
            Cache.playersToFactions = playersToFactions;
        }

        if (playersToInvites != null) {
            Cache.playersToInvites = playersToInvites;
        }

        if (playersToPerms != null) {
            Cache.playersToPerms = playersToPerms;
        }

        if (chunks != null) {
            Cache.chunks = chunks;
        }
    }

    public static void putFaction(UUID playerUUID, String faction) {
        playersToFactions.put(playerUUID, faction);
    }

    public static void putInvite(UUID playerUUID, String faction) {
        playersToInvites.put(playerUUID, faction);
    }

    public static void putPerms(UUID playerUUID, PermSet perms) {
        playersToPerms.put(playerUUID, perms);
    }

    public static void putPerms(UUID playerUUID, boolean isLeader, boolean canClaim, boolean canInvite) {
        playersToPerms.put(playerUUID, new PermSet(isLeader, canClaim, canInvite));
    }

    public static void put(ClaimedChunk chunk, String faction) {
        chunks.put(chunk, faction);
    }

    public static void remove(ClaimedChunk chunk) {
        chunks.remove(chunk);
    }

    public static boolean contains(UUID playerUUID) {
        return playersToFactions.containsKey(playerUUID);
    }

    public static boolean contains(ClaimedChunk chunk) {
        return chunks.containsKey(chunk);
    }

    public static String getFaction(UUID playerUUID) {
        return playersToFactions.get(playerUUID);
    }

    public static String getInvite(UUID playerUUID) {
        return playersToInvites.get(playerUUID);
    }

    public static PermSet getPerms(UUID playerUUID) {
        return playersToPerms.get(playerUUID);
    }

    public static HashMap<UUID, PermSet> getAllPerms() {
        return playersToPerms;
    }

    public static HashMap<ClaimedChunk, String> getAllChunks() {
        return chunks;
    }

    public static String get(ClaimedChunk chunk) {
        return chunks.get(chunk);
    }

    public static ArrayList<UUID> getFactionMembers(String faction) {
        ArrayList<UUID> members = new ArrayList<>();
        for (UUID u : playersToFactions.keySet()) {
            String currFaction = playersToFactions.get(u);
            if (currFaction != null && currFaction.equals(faction)) {
                members.add(u);
            }
        }
        return members;
    }
}
