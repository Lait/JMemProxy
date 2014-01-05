package jmemproxy.client;

import java.nio.channels.SocketChannel;

public class ClientRequest {
	private SocketChannel channel;
	private byte[] requestString;
	
	public ClientRequest(SocketChannel c, byte[] buf) {
		this.channel = c;
		this.requestString = buf;
	}
	
	public SocketChannel getChannel() {
		return this.channel;
	}
	
	public byte[] getRequestString() {
		return this.requestString;
	}
}