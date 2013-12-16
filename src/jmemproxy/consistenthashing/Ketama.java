package jmemproxy.consistenthashing;

/*
 * Ketama.java
 * Description:
 * 
 * 
 * Author: Leon
 * Email : lleon.21.t@gmail.com
 */

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import jmemproxy.memcache.*;

public class Ketama<T> {
	private List<ServerNode> nodes;
	private SortedMap<T, ServerNode> circle;
	
	public Ketama() {
		this.nodes  = new LinkedList<ServerNode>();
		this.circle = new TreeMap<T,  ServerNode>();
	}
	
	public ServerNode getServer(T key) {
		return null;
	}

}
