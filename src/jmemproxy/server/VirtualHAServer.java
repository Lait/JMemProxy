package jmemproxy.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Random;

import jmemproxy.common.Request;

public class VirtualHAServer {
	private static final Logger logger = Logger.getLogger(VirtualHAServer.class.getName());
	
	private String serverID;
	private List<MemcachedNode> nodes;
	
	private String getRandomString() {
	    String base = "abcdefghijklmnopqrstuvwxyz0123456789";   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < base.length(); i++) {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();
	}
	
	public VirtualHAServer(int port, String ip) throws IOException {
		this.nodes = new LinkedList<MemcachedNode>();
		this.nodes.add(new MemcachedNode(port, ip));
		this.serverID = this.getRandomString();
	}
	
	public Boolean pushRequest(Request req) {
		return true;
	}
	
	public void run() {
		for (MemcachedNode node : nodes) {
			node.start();
		}
	}

	public void setServerID(String id) {
		this.serverID = id;
	}
	
	public String getServerID() {
		return this.serverID;
	}
	
	public List<MemcachedNode> getNodes() {
		return this.nodes;
	}
}
