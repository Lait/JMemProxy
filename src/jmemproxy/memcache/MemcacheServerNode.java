package jmemproxy.memcache;

import java.nio.channels.SocketChannel;

public class MemcacheServerNode extends ServerNode{
	private SocketChannel channel;
	
	public MemcacheServerNode(int port, String ip, SocketChannel channel) {
		this.port    = port;
		this.ip      = ip;
		this.channel = channel;
	}
	

}
