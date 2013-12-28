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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.consistenthashing.MD5Hash;
import jmemproxy.memcache.ClientRequest;
import jmemproxy.memcache.MemcacheServer;

public class JMemProxyBackend implements Runnable {	
	private static Selector  selector;
	private Map<String, MemcacheServer> servers;
	private ByteBuffer buffer;
	
	private Ketama ketama;
	
	public JMemProxyBackend() throws IOException {
		this.servers = new TreeMap<String, MemcacheServer>();
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
			this.servers.put(channel.toString(), node);
			this.ketama.addServer(node);
			System.out.println("Server node (" + node.getIp() + ":" + node.getPort() + ") Added.");
		}
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
					
					if (!key.isValid()) continue;
					if (key.isReadable()) {
						SocketChannel serverChannel = (SocketChannel)key.channel();
						MemcacheServer server = this.servers.get(serverChannel.toString());
						this.buffer.clear();
						int num = serverChannel.read(buffer);
						if (num > 0) {
							ClientRequest req = server.pollRequest();
							this.buffer.flip();
							req.getChannel().write(buffer);
							
							//Get the first un processed request.
							req = server.peekRequest();
							this.buffer.clear();
							this.buffer.put(req.getRequestString().getBytes());
							serverChannel.write(buffer);
						}
					}
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
