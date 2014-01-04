package jmemproxy.memcache;

/*
 * 
 */

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
import java.util.Map.Entry;
import java.util.Queue;

public class MemcacheInteractor {
	public static final int MAXCONNCOUNT = 8;
	private String ip;
	private int port;
	private Selector selector;
	private Queue<ClientRequest> requests;
	private Map<SocketChannel, SocketChannel> busyConnections;
	private Queue<SocketChannel>  freeConnections;
	private ByteBuffer buffer;
	
	public MemcacheInteractor(int port, String ip) throws IOException {
		this.selector = Selector.open();
		this.requests = new LinkedList<ClientRequest>();
		this.busyConnections = new HashMap<SocketChannel, SocketChannel>();
		this.freeConnections = new LinkedList<SocketChannel>();
		this.buffer = ByteBuffer.allocate(1024);
	}
	
	public void finalize() throws IOException {
		this.selector.close();
		Iterator<Entry<SocketChannel, SocketChannel>> it1 = this.busyConnections.entrySet().iterator();
		while(it1.hasNext()) {
			((SocketChannel)((Map.Entry<SocketChannel, SocketChannel>) it1.next()).getKey()).close();
		}
		
		Iterator<SocketChannel> it2 = this.freeConnections.iterator();
		while(it2.hasNext()) {
			((SocketChannel) it2.next()).close();
		}
	}
	
	//创建一个连接到Memcached节点的新连接，如果不能创建则返回null
	private SocketChannel newConnection() {
		try {
			SocketChannel newChannel = SocketChannel.open();
			newChannel.configureBlocking(false);
			if (newChannel.connect(new InetSocketAddress(this.ip, this.port)))
				return newChannel;
		} catch (IOException e) {
			System.out.printf("Could not create new connection to server(%s:%d).\n", 
								this.ip, this.port);
			e.printStackTrace();
		}
		return null;
	}
	
	//为连接池补充新的连接，如果超过最大连接数则不创建
	private Boolean addConnections() throws IOException {
		if (this.freeConnections.size() + this.busyConnections.size() == MAXCONNCOUNT) {
			return false;
		}
		
		for (int i = 1; i <= (MAXCONNCOUNT - this.freeConnections.size() - this.busyConnections.size()); i++) {
			this.freeConnections.add(this.newConnection());
		}
		
		return true;
	}
	
	//从请求队列里面取出一个未处理的请求分配到一个空闲的连接中发送
	private void processReq() throws IOException {
		if (!this.freeConnections.isEmpty()) {
			SocketChannel freeChannel = this.freeConnections.poll();
			ClientRequest req = this.requests.poll();
			while (!freeChannel.isConnected()) {
				freeChannel.close();
				if (this.freeConnections.isEmpty()) {
					this.requests.add(req);
					if (this.addConnections() == true) {
						this.processReq();
					}
					else return;
				}
				freeChannel = this.freeConnections.poll();
			}
			this.buffer.clear();
			this.buffer.put(req.getRequestString());
			this.buffer.flip();
			freeChannel.write(this.buffer);
			this.busyConnections.put(freeChannel, req.getChannel());
		}
		else if (this.busyConnections.size() < MAXCONNCOUNT) {
			if (this.addConnections() == true)
				this.processReq();
		}
	}
	
	public String getMemServerInfo() {
		return this.ip + this.port;
	}
	
	//添加新的请求到请求队列中
	public void pushRequest(ClientRequest req) throws IOException {
		synchronized(this.requests) {
			this.requests.add(req);
			this.requests.notifyAll();
		}
	}

	//主循环
	public void mainloop() throws IOException {
		while (true) {
			//直到selector中有能够响应的连接出现才进行处理
			if (this.selector.select() < 1) continue;
			
			//取出所有需要处理的连接
			Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				
				if (key.isValid() && key.isReadable()) {
					SocketChannel serverChannel = (SocketChannel)key.channel();
					SocketChannel clientChannel = this.busyConnections.get(serverChannel);
					
					int numofbytereaded = 0;
					
					//尝试从memcached连接中读取数据， 如果失败则注销连接。
					try {
						this.buffer.clear();
						numofbytereaded = serverChannel.read(this.buffer);
						this.buffer.flip();
					} catch (IOException e) {
						e.printStackTrace();
						System.out.printf("Connection to server(%s:%d)",this.ip, this.port);
						System.out.printf("closed while reading!Read failed!\n");
						this.busyConnections.remove(serverChannel);
						serverChannel.close();
					}
					
					//如果已经读完了数据则发送回客户端，否则中断与客户端的连接。
					if (numofbytereaded > 0) {
						try {
							clientChannel.write(this.buffer);
						} catch (IOException e) {
							e.printStackTrace();
							System.out.printf("Can't write to channel(%s)", 
									clientChannel.getRemoteAddress().toString());
							clientChannel.close();
						}
					} else {
						clientChannel.close();
					}
					
					//处理下一个请求
					this.processReq();
				}
			}
		}
	}
}
