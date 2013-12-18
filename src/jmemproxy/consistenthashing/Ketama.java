package jmemproxy.consistenthashing;

/*
 * Ketama.java
 * Description:
 * 
 * 
 * Author: Leon
 * Email : lleon.21.t@gmail.com
 */

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import jmemproxy.memcache.*;

public class Ketama {
	private SortedMap<Integer, ServerNode> circle;
	private HashFunction hashfunction;
	private int numOfReplicates;
	
	public Ketama(HashFunction hashfunction, int numOfReplicates, Collection<ServerNode> nodes) {
		this.hashfunction = hashfunction;
		this.numOfReplicates = numOfReplicates;
		this.circle = new TreeMap<Integer,  ServerNode>();
		
		if (nodes != null) {
			for (ServerNode node : nodes) {
				addServer(node);
			}
		}
	}
	
	public ServerNode getServer(Object key) {
		if (circle.isEmpty()) return null;
		
		int keyhash = this.hashfunction.hash(key);
		if (!circle.containsKey(keyhash)) {
			SortedMap<Integer, ServerNode> tailmap = circle.tailMap(this.hashfunction.hash(key));
			if (tailmap.isEmpty()) {
				keyhash = (int) circle.firstKey();
			} else {
				keyhash = (int) tailmap.firstKey();
			}
		}
		
		return circle.get(keyhash);
	}
	
	public void addServer(ServerNode node) {
		//Add virtual nodes to the circle , it there a better way?
		for (int i = 0; i < this.numOfReplicates; i++) {
			int hash = this.hashfunction.hash(node.toString() + i);
			this.circle.put(hash, node);
		}
	}

}
