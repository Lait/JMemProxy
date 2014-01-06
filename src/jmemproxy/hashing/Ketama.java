package jmemproxy.hashing;

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

import jmemproxy.backend.*;

public class Ketama {
	private SortedMap<Integer, VirtualHAServer> circle;
	private HashFunction hashfunction;
	private int numOfReplicates;
	
	public Ketama(int numOfReplicates, Collection<VirtualHAServer> servers) {
		this.hashfunction = new MD5Hash();
		this.numOfReplicates = numOfReplicates;
		this.circle = new TreeMap<Integer,  VirtualHAServer>();
		
		if (servers != null) {
			for (VirtualHAServer server : servers) {
				addServer(server);
			}
		}
	}
	
	public VirtualHAServer getServer(Object key) {
		if (circle.isEmpty()) return null;
		
		int keyhash = this.hashfunction.hash(key);
		if (!circle.containsKey(keyhash)) {
			SortedMap<Integer, VirtualHAServer> tailmap = circle.tailMap(this.hashfunction.hash(key));
			if (tailmap.isEmpty()) {
				keyhash = (int) circle.firstKey();
			} else {
				keyhash = (int) tailmap.firstKey();
			}
		}
		
		return circle.get(keyhash);
	}
	
	public final SortedMap<Integer, VirtualHAServer> getAvailableServers() {
		return this.circle;
	}
	
	public void addServer(VirtualHAServer server) {
		for (int i = 0; i < this.numOfReplicates; i++) {
			int hash = this.hashfunction.hash(server.getServerID() + i);
			this.circle.put(hash, server);
		}
	}
	
	public void removeServer(MemcachedNode node) {
		int hash = 0;
		for (int i = 0; i < this.numOfReplicates; i++) {
			hash = this.hashfunction.hash(node.toString() + i);
			this.circle.remove(hash);
		}
	}

}
