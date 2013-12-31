package jmemproxy.consistenthashing;

/*
 * Ketama.java
 * Description:
 * The Ketama consistent hash function.
 * 
 * Author: Leon
 * Email : lleon.21.t@gmail.com
 */

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import jmemproxy.memcache.*;

public class Ketama {
	private SortedMap<Integer, MemcacheServer> circle;
	private HashFunction hashfunction;
	private int numOfReplicates;
	
	public Ketama(HashFunction hashfunction, int numOfReplicates, Collection<MemcacheServer> nodes) {
		this.hashfunction = hashfunction;
		this.numOfReplicates = numOfReplicates;
		this.circle = new TreeMap<Integer,  MemcacheServer>();
		
		if (nodes != null) {
			for (MemcacheServer node : nodes) {
				addServer(node);
			}
		}
	}
	
	public MemcacheServer getServer(Object key) {
		if (circle.isEmpty()) return null;
		
		int keyhash = this.hashfunction.hash(key);
		if (!circle.containsKey(keyhash)) {
			SortedMap<Integer, MemcacheServer> tailmap = circle.tailMap(this.hashfunction.hash(key));
			if (tailmap.isEmpty()) {
				keyhash = (int) circle.firstKey();
			} else {
				keyhash = (int) tailmap.firstKey();
			}
		}
		
		return circle.get(keyhash);
	}
	
	public final SortedMap<Integer, MemcacheServer> getAvailableServers() {
		return this.circle;
	}
	
	public void addServer(MemcacheServer node) {
		// Add virtual nodes to the circle , it there a better way?
		// This one is too simple, and not effective enough.
		// Some better approaches needed! XD
		for (int i = 0; i < this.numOfReplicates; i++) {
			int hash = this.hashfunction.hash(node.toString() + i);
			this.circle.put(hash, node);
		}
	}
	
	public void removeServer(MemcacheServer node) {
		int hash = 0;
		for (int i = 0; i < this.numOfReplicates; i++) {
			hash = this.hashfunction.hash(node.toString() + i);
			this.circle.remove(hash);
		}
	}

}
