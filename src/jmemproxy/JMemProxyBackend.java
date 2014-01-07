package jmemproxy;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import jmemproxy.common.Request;
import jmemproxy.hashing.HashFunction;
import jmemproxy.hashing.MD5Hash;
import jmemproxy.server.MemcachedNode;
import jmemproxy.server.VirtualHAServer;


public class JMemProxyBackend {	
	private SortedMap<Integer, VirtualHAServer> circle;
	private HashFunction                        hashfunction;
	
	//Params
	private int NUMOFREPLICATES;
	
	public JMemProxyBackend() {
		try {
			this.circle         = new TreeMap<Integer,  VirtualHAServer>();
			this.hashfunction   = new MD5Hash();
			this.NUMOFREPLICATES = 1;
			//Used for test.
			VirtualHAServer server = new VirtualHAServer(1234, "127.0.0.1");
			this.circle.put(this.hashfunction.hash(server.getServerID()), server);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private VirtualHAServer getServerByHashKey(Object key) {
		if (circle.isEmpty()) return null;
		
		int keyhash = hashfunction.hash(key);
		if (!circle.containsKey(keyhash)) {
			SortedMap<Integer, VirtualHAServer> tailmap = circle.tailMap(hashfunction.hash(key));
			if (tailmap.isEmpty()) {
				keyhash = (int) circle.firstKey();
			} else {
				keyhash = (int) tailmap.firstKey();
			}
		}
		
		return circle.get(keyhash);
	}
	
	public void addServer(VirtualHAServer server) {
		for (int i = 0; i < NUMOFREPLICATES; i++) {
			this.circle.put(this.hashfunction.hash(server.getServerID() + i), server);
		}
	}
	
	public void removeServer(MemcachedNode node) {
		for (int i = 0; i < NUMOFREPLICATES; i++) {
			this.circle.remove(this.hashfunction.hash(node.toString() + i));
		}
	}
	
	public void start() {
		Iterator<Entry<Integer, VirtualHAServer>> it = this.circle.entrySet().iterator();
		while(it.hasNext()) {
			it.next().getValue().run();
		}
	}
	
	public void processRequest(Request req) throws IOException {
		this.getServerByHashKey(req.getRequestString()).pushRequest(req);
	}
	

}
