package jmemproxy;

/*
 * JMemProxyBackend.java
 * Description:
 * The back-end logic.
 * 
 * Author: Leon
 * Email : lleon.21.t@gmail.com
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.consistenthashing.MD5Hash;
import jmemproxy.memcache.MemcacheServerNode;
import jmemproxy.memcache.ServerNode;

public class JMemProxyBackend implements Runnable {	
	private static Selector  selector;
	private Map<SocketChannel, ServerNode> serverNodes;
	
	private Ketama ketama;
	
	public JMemProxyBackend() throws IOException {
		this.serverNodes = new TreeMap<SocketChannel, ServerNode>();
		this.ketama      = new Ketama(new MD5Hash(), 1, null);
		JMemProxyBackend.selector = Selector.open();
		
		this.initialize();
	}
	
	private void initialize() throws IOException {
		SocketChannel channel = SocketChannel.open(new InetSocketAddress(1234));
		if (channel != null) {
			channel.configureBlocking(false);
			MemcacheServerNode node = new MemcacheServerNode(1234, "127.0.0.1", channel);
			this.serverNodes.put(channel, node);
			this.ketama.addServer(node);
			System.out.println("Server node (" + node.getIp() + ":" + node.getPort() + ") Added.");
		}
	}
	
	//use for testing
	public int serverNum() {
		return this.serverNodes.size();
	}

	public void run() {
		
	}
}
