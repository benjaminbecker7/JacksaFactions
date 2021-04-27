package com.bmbecker.plugin.objects;

public class FactionHome {

    private final String server;
    private final String world;
    private final int x, y, z;

    public FactionHome(String server, String world, int x, int y, int z) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
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

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

}
