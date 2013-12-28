package jmemproxy.memcache;

import java.nio.channels.SocketChannel;

class Request {
	private SocketChannel channel;
	private String requestString;
	
	public Request(SocketChannel c, String r) {
		this.channel = c;
		this.requestString = r;
	}
	
	public SocketChannel getChannel() {
		return this.channel;
	}
	
	public String getRequestString() {
		return this.requestString;
	}
}