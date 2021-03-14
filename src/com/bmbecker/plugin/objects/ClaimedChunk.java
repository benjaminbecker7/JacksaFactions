package com.bmbecker.plugin.objects;

import java.util.UUID;

public class ClaimedChunk {

	private final UUID uuid;
	
	private String world; // world of chunk
	private int x, z; // X and Z coordinates of chunk
	
	public ClaimedChunk(UUID uuid, String world, int x, int z) {
		this.uuid = uuid;
		this.world = world;
		this.x = x;
		this.z = z;
	}
	
	public void setWorld(String world) {
		this.world = world;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setZ(int z) {
		this.z = z;
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
	
	public UUID getUUID() {
		return uuid;
	}
}
