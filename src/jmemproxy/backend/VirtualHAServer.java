package jmemproxy.backend;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jmemproxy.client.ClientRequest;

public class VirtualHAServer {
	private static final Logger logger = Logger.getLogger(VirtualHAServer.class.getName());
	
	private String serverID;
	private List<MemcachedNode> nodes;
	
	public VirtualHAServer(int port, String ip) throws IOException {
		this.nodes = new LinkedList<MemcachedNode>();
		this.nodes.add(new MemcachedNode(port, ip));
	}
	
	public Boolean pushRequest(ClientRequest req) {
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
