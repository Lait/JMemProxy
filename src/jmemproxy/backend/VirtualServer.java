package jmemproxy.backend;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import jmemproxy.client.ClientRequest;

public class VirtualServer {
	private String serverID;
	private MemcachedNode node;
	private Queue<ClientRequest> requests;
	private Logger logger = Logger.getLogger(VirtualServer.class.getName());
	
	public VirtualServer(String ip, int port) throws IOException {
		this.requests = new LinkedList<ClientRequest>();
		this.node = new MemcachedNode(port, ip, requests);
	}
	
	public Boolean pushRequest(ClientRequest req) {
		synchronized(this.requests) {
			logger.info("Adding new request to server:" + this.node.getMemServerInfo() + "\n" );
			Boolean flag = this.requests.add(req);
			this.requests.notifyAll();
			return flag;
		}
	}
	
	public Queue<ClientRequest> getRequests(){
		return this.requests;
	}
	
	public void run() {
		this.node.start();
	}

	public void setServerID(String id) {
		this.serverID = id;
	}
	
	public String getServerID(String id) {
		return this.serverID;
	}
	
	public MemcachedNode getNode() {
		return this.node;
	}
}
