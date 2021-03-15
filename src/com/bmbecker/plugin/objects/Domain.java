package com.bmbecker.plugin.objects;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;

public class Domain {
	private int size;
	
	private final Set<ClaimedChunk> chunks;
	
	public Domain() {
		this.chunks = new HashSet<>();
		this.size = 0;
	}
	
	public int size() {
		return size;
	}
	
	public void addChunk(ClaimedChunk chunk) {
		chunks.add(chunk);
		size ++;
	}
	
	public boolean removeChunk(ClaimedChunk chunk) {
		if(chunks.remove(chunk)) {
			size --;
			return true;
		} else {
			return false;
		}
	}
	
	public Set<ClaimedChunk> getChunks() {
		return chunks;
	}
	
	public boolean inDomain(Chunk chunk) {
		
		ClaimedChunk dummyChunk = ClaimedChunk.parseClaimedChunk(chunk);
		
		return chunks.contains(dummyChunk);
	}
}
