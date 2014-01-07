package jmemproxy.common;

import java.nio.channels.SocketChannel;

public class Request {
	private SocketChannel channel;
	private byte[] requestString;
	
	public Request(SocketChannel c, byte[] buf) {
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