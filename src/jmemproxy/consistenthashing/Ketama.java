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
	private SortedMap<Integer, MemcacheInteractor> circle;
	private HashFunction hashfunction;
	private int numOfReplicates;
	
	public Ketama(int numOfReplicates, Collection<MemcacheInteractor> nodes) {
		this.hashfunction = new MD5Hash();
		this.numOfReplicates = numOfReplicates;
		this.circle = new TreeMap<Integer,  MemcacheInteractor>();
		
		if (nodes != null) {
			for (MemcacheInteractor node : nodes) {
				addServer(node);
			}
		}
	}
	
	public MemcacheInteractor getServer(Object key) {
		if (circle.isEmpty()) return null;
		
		int keyhash = this.hashfunction.hash(key);
		if (!circle.containsKey(keyhash)) {
			SortedMap<Integer, MemcacheInteractor> tailmap = circle.tailMap(this.hashfunction.hash(key));
			if (tailmap.isEmpty()) {
				keyhash = (int) circle.firstKey();
			} else {
				keyhash = (int) tailmap.firstKey();
			}
		}
		
		return circle.get(keyhash);
	}
	
	public final SortedMap<Integer, MemcacheInteractor> getAvailableServers() {
		return this.circle;
	}
	
	public void addServer(MemcacheInteractor node) {
		// Add virtual nodes to the circle , it there a better way?
		// This one is too simple, and not effective enough.
		// Some better approaches needed! XD
		for (int i = 0; i < this.numOfReplicates; i++) {
			int hash = this.hashfunction.hash(node.toString() + i);
			this.circle.put(hash, node);
		}
	}
	
	public void removeServer(MemcacheInteractor node) {
		int hash = 0;
		for (int i = 0; i < this.numOfReplicates; i++) {
			hash = this.hashfunction.hash(node.toString() + i);
			this.circle.remove(hash);
		}
	}

}
