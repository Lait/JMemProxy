package jmemproxy.client;

import java.nio.channels.SocketChannel;

public class ClientRequest {
	private SocketChannel channel;
	private String requestString;
	
	public ClientRequest(SocketChannel c, String r) {
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