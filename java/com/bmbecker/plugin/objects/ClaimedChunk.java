package com.bmbecker.plugin.objects;


import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class ClaimedChunk {

    private final String server, world; // world of chunk
    private final int x, z; // X and Z coordinates of chunk

    /**
     * Basic constructor that by default serializes the
     * claimID field variable. If you don't want to serialize
     * the claimID field variable (i.e. don't want it to increase
     * the claimIDBase value
     * @param world
     * @param x
     * @param z
     */
    public ClaimedChunk(final String server, final String world, final int x, final int z) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public String getServer() {
        return server;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public static ClaimedChunk parseClaimedChunk(Chunk chunk) {
        return new ClaimedChunk(Bukkit.getServer().getName(), chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, world, x, z);
    }

    @Override
    public boolean equals(Object other) {

        ClaimedChunk otherChunk;

        if (this == other) {
            return true;
        } else if (other instanceof Chunk) {
            otherChunk = parseClaimedChunk((Chunk) other);
        } else if (other instanceof ClaimedChunk) {
            otherChunk = (ClaimedChunk) other;
        } else {
            return false;
        }

        return server.equals(otherChunk.server) && world.equals(otherChunk.world) && x == otherChunk.x && z == otherChunk.z;
    }

    @Override
    public String toString() {
        return "ClaimedChunk [server=" + server + ", world=" + world + ", x=" + x + ", z=" + z + "]";
    }
}
