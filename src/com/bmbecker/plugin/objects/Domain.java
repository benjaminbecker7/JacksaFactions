package com.bmbecker.plugin.objects;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.plugin.java.JavaPlugin;

public class Domain {
	private int size;
	
	private final JavaPlugin javaPlugin;
	private final Set<ClaimedChunk> chunks;
	
	public Domain(JavaPlugin javaPlugin) {
		this.javaPlugin = javaPlugin;
		this.chunks = new HashSet<>();
		this.size = 0;
	}
	
	public int size() {
		return size;
	}
	
	public void addChunk(ClaimedChunk chunk) {
		chunks.add(chunk);
	}
	
	public Set<ClaimedChunk> getChunks() {
		return chunks;
	}
	
	public ClaimedChunk getChunk(Chunk chunk) {
		for (ClaimedChunk claimed : chunks) {
			Chunk chunkClaimed = javaPlugin.getServer().getWorld(claimed.getWorld()).getChunkAt(claimed.getX(), claimed.getZ());
			if (chunkClaimed.equals(chunk)) {
				return claimed;
			}
		}
		
		return null;
	}
}
