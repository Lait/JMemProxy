package jmemproxy.memcache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MemcacheInteractor {
	public static final int MAXCONNECTION = 8;
	private String ip;
	private int port;
	private Selector selector;
	private Queue<ClientRequest> requests;
	private Map<SocketChannel, SocketChannel> usedConnections;
	private Queue<SocketChannel>  unusedConnections;
	private ByteBuffer buffer;
	
	public MemcacheInteractor(int port, String ip) throws IOException {
		this.selector = Selector.open();
		this.requests = new LinkedList<ClientRequest>();
		this.usedConnections = new HashMap<SocketChannel, SocketChannel>();
		this.unusedConnections = new LinkedList<SocketChannel>();
		this.buffer = ByteBuffer.allocate(1024);
	}
	
	private SocketChannel newConnection() throws IOException {
		SocketChannel newChannel = SocketChannel.open();
		newChannel.configureBlocking(false);
		newChannel.connect(new InetSocketAddress(this.ip, this.port));
		return newChannel;
	}
	
	private void addConnections() throws IOException {
		for (int i = 1; i <= (MAXCONNECTION - this.unusedConnections.size() - this.usedConnections.size()); i++) {
			this.unusedConnections.add(this.newConnection());
		}
	}
	
	public void pushRequest(ClientRequest req) throws IOException {
		if (this.unusedConnections.isEmpty()) {
			this.requests.add(req);
		}
		else {
			SocketChannel freeChannel = this.unusedConnections.poll();
			while (!freeChannel.isConnected()) {
				freeChannel.close();
				if (this.unusedConnections.isEmpty()) {
					this.requests.add(req);
					this.addConnections();
					return;
				}
				freeChannel = this.unusedConnections.poll();
			}
			this.buffer.clear();
			this.buffer.put(req.getRequestString());
			this.buffer.flip();
			freeChannel.write(this.buffer);
			this.usedConnections.put(freeChannel, req.getChannel());
		}
	}

	public void mainloop() {
		try {
			while (true) {
				int count = this.selector.select();
				if (count < 1) continue;
				
				Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					
					if (key.isValid() && key.isReadable()) {
						SocketChannel serverChannel = (SocketChannel)key.channel();
						SocketChannel clientChannel = this.usedConnections.get(serverChannel);
						
						if (serverChannel.isConnected()) {
							this.buffer.clear();
							serverChannel.read(this.buffer);
							this.buffer.flip();
							if (clientChannel.isConnected()) {
								clientChannel.write(this.buffer);
							} 
							else {
								clientChannel.close();
							}
							
							if (this.requests.isEmpty()) {
								this.usedConnections.remove(serverChannel);
								this.unusedConnections.add(serverChannel);
							}
							else {
								ClientRequest firstReq = this.requests.poll();
								this.buffer.clear();
								this.buffer.put(firstReq.getRequestString());
								this.buffer.flip();
								serverChannel.write(this.buffer);
								this.usedConnections.put(serverChannel, firstReq.getChannel());
							}
							
						}
						else {
							serverChannel.close();
							this.usedConnections.remove(serverChannel);
							System.out.printf("Connection to server(%s:%d) closed unexpectly!\n", this.ip, this.port);
						}
						
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
