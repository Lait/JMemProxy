package jmemproxy.memcache;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import jmemproxy.client.ClientRequest;

public class MemcacheServer {
	private SocketChannel channel;
	private Queue<ClientRequest> requests;
	
	private int port;
	private String ip;
	
	public int getPort() {
		return this.port;
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public MemcacheServer(int port, String ip, SocketChannel channel) {
		this.port     = port;
		this.ip       = ip;
		this.channel  = channel;
		this.requests = new LinkedList<ClientRequest>();
	}
	
	public SocketChannel getServerChannel() {
		return this.channel;
	}
	
	public int getRequestNum() {
		return this.requests.size();
	}
	
	public ClientRequest peekRequest() {
		return this.requests.peek();
	}
	
	public ClientRequest getAndRemoveFirstRequest() {
		return this.requests.poll();
	}
	
	public void pushRequest(ClientRequest req) {
		this.requests.add(req);
	}
}
