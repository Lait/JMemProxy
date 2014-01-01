package jmemproxy.memcache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MemcacheInteractor {
	private String ip;
	private int port;
	private Selector selector;
	private Queue<ClientRequest> requests;
	private Map<SocketChannel, SocketChannel> usedConnections;
	private List<SocketChannel>  unusedConnections;
	private ByteBuffer buffer;
	
	public MemcacheInteractor(int port, String ip) throws IOException {
		this.selector = Selector.open();
		this.requests = new LinkedList<ClientRequest>();
		this.usedConnections = new HashMap<SocketChannel, SocketChannel>();
		this.unusedConnections = new LinkedList<SocketChannel>();
		this.buffer = ByteBuffer.allocate(1024);
	}
	
	public void pushRequest(ClientRequest req) {
		this.requests.add(req);
	}

	public void mainloop() {
		// TODO Auto-generated method stub
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
								this.buffer.put(firstReq.getRequestString().getBytes("UTF-8"));
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
