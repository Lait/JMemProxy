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
	
	//����һ�����ӵ�Memcached�ڵ�������ӣ�������ܴ����򷵻�null
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
	
	//Ϊ���ӳز����µ����ӣ������������������򲻴���
	private Boolean addConnections() throws IOException {
		if (this.freeConnections.size() + this.busyConnections.size() == MAXCONNCOUNT) {
			return false;
		}
		
		for (int i = 1; i <= (MAXCONNCOUNT - this.freeConnections.size() - this.busyConnections.size()); i++) {
			this.freeConnections.add(this.newConnection());
		}
		
		return true;
	}
	
	//�������������ȡ��һ��δ�����������䵽һ�����е������з���
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
	
	//����µ��������������
	public void pushRequest(ClientRequest req) throws IOException {
		synchronized(this.requests) {
			this.requests.add(req);
			this.requests.notifyAll();
		}
	}

	//��ѭ��
	public void mainloop() throws IOException {
		while (true) {
			//ֱ��selector�����ܹ���Ӧ�����ӳ��ֲŽ��д���
			if (this.selector.select() < 1) continue;
			
			//ȡ��������Ҫ���������
			Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				
				if (key.isValid() && key.isReadable()) {
					SocketChannel serverChannel = (SocketChannel)key.channel();
					SocketChannel clientChannel = this.busyConnections.get(serverChannel);
					
					int numofbytereaded = 0;
					
					//���Դ�memcached�����ж�ȡ���ݣ� ���ʧ����ע�����ӡ�
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
					
					//����Ѿ��������������ͻؿͻ��ˣ������ж���ͻ��˵����ӡ�
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
					
					//������һ������
					this.processReq();
				}
			}
		}
	}
}
