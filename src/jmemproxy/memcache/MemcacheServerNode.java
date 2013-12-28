package jmemproxy.memcache;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MemcacheServerNode extends ServerNode{
	private SocketChannel channel;
	private SocketChannel CurrentClientChannel;
	private Queue<ClientRequest> requests;
	
	private int port;
	private String ip;
	
	public int getPort() {
		return this.port;
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public MemcacheServerNode(int port, String ip, SocketChannel channel) {
		this.port     = port;
		this.ip       = ip;
		this.channel  = channel;
		this.requests = new LinkedList<ClientRequest>();
	}
	
	public SocketChannel getServerChannel() {
		return this.channel;
	}
	
	public SocketChannel getCurrentClientChannel() {
		return this.CurrentClientChannel;
	}
	
	public void setCurrentSocketChannel(SocketChannel channel) {
		this.CurrentClientChannel = channel;
	}
	
	public int getRequestNum() {
		return this.requests.size();
	}
	
	public void pollRequest() {
		this.requests.poll();
	}
	
	public void pushRequest(ClientRequest req) {
		this.requests.add(req);
	}
}
