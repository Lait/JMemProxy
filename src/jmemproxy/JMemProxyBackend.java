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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.consistenthashing.MD5Hash;
import jmemproxy.memcache.MemcacheServer;

public class JMemProxyBackend implements Runnable {	
	private static Selector  selector;
	private Map<SocketChannel, MemcacheServer> servers;
	private ByteBuffer buffer;
	
	private Ketama ketama;
	
	public JMemProxyBackend() throws IOException {
		this.servers = new HashMap<SocketChannel, MemcacheServer>();
		this.ketama  = new Ketama(new MD5Hash(), 1, null);
		this.buffer  = ByteBuffer.allocate(2048);
		
		JMemProxyBackend.selector = Selector.open();
		this.initialize();
	}
	
	private void initialize() throws IOException {
		SocketChannel channel = SocketChannel.open(new InetSocketAddress(1234));
		if (channel != null) {
			channel.configureBlocking(false);
			MemcacheServer node = new MemcacheServer(1234, "127.0.0.1", channel);
			this.servers.put(channel, node);
			this.ketama.addServer(node);
			System.out.println("Server node (" + node.getIp() + ":" + node.getPort() + ") Added.");
		}
	}
	
	private void initServers() {
		
	}

	public void run() {
		try {
			while (true) {
				int count = JMemProxyBackend.selector.select();
				if (count < 1) continue;
				
				Iterator<SelectionKey> iterator = JMemProxyBackend.selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					
					if (key.isValid() && key.isReadable()) {
						SocketChannel serverChannel = (SocketChannel)key.channel();
						MemcacheServer server = this.servers.get(serverChannel);
						
						this.buffer.clear();
						if (serverChannel.read(buffer) > 0) {
							this.buffer.flip();
							//Remove the first req and send back the response from memcache server.
							server.getAndRemoveFirstRequest().getChannel().write(buffer);
							
							//Send the message form the first unprocessed request to memcache server.
							sendFirstReqToMemServer(server);
						}
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void sendFirstReqToMemServer(MemcacheServer server) throws IOException {
		this.buffer.clear();
		this.buffer.put(server.peekRequest().getRequestString().getBytes());
		server.getServerChannel().write(buffer);
	}
}
