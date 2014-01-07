package jmemproxy.server;

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
import java.util.logging.Logger;

import jmemproxy.common.Request;

public class MemcachedNode extends Thread {
	private static final int MAXCONNCOUNT = 8;
	private static final Logger logger = Logger.getLogger(MemcachedNode.class.getName());
	
	private String remoteIp;
	private int memcachedPort;
	
	private int    sshPort;
	private String sshPassword;
	
	private Selector selector;
	private Queue<Request> requests;
	private Map<SocketChannel, SocketChannel> busyConnections;
	private Queue<SocketChannel>  freeConnections;
	private ByteBuffer buffer;
	
	public MemcachedNode(int port, String ip) throws IOException {
		this.memcachedPort = port;
		this.remoteIp   = ip;
		this.selector = Selector.open();
		this.requests = new LinkedList<Request>();
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
	private SocketChannel createNewConnection() {
		try {
			SocketChannel newChannel = SocketChannel.open();
			newChannel.configureBlocking(false);
			if (newChannel.connect(new InetSocketAddress(this.remoteIp, this.memcachedPort)))
				return newChannel;
		} catch (IOException e) {
			logger.info(String.format("Could not create new connection to server(%s:%d).\n", 
								this.remoteIp, this.memcachedPort));
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
			this.freeConnections.add(this.createNewConnection());
		}
		
		return true;
	}
	
	public Boolean pushRequest(Request req) {
		synchronized(this.requests) {
			Boolean flag = this.requests.add(req);
			this.requests.notifyAll();
			return flag;
		}
	}
	
	//�������������ȡ��һ��δ�����������䵽һ�����е������з���
	private void processNextReq() {
		synchronized(this.requests) {
			try {
				if (!this.requests.isEmpty()) {
					if (!this.freeConnections.isEmpty()) {
						SocketChannel freeChannel = this.freeConnections.poll();
						Request req = this.requests.poll();
						while (!freeChannel.isConnected()) {
							freeChannel.close();
							if (this.freeConnections.isEmpty()) {
								this.requests.add(req);
								if (this.addConnections() == true) {
									this.processNextReq();
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
							this.processNextReq();
					}
				} 
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				this.requests.notifyAll();
			}
		}
	}
	
	public String getMemServerInfo() {
		return this.remoteIp + this.memcachedPort;
	}

	public void run() {
		try {
			while (true) {
				//ֱ��selector�����ܹ���Ӧ�����ӳ��ֲŽ��д���
				if (this.selector.select() > 0) {				
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
								logger.info("Connection to server(" + 
										serverChannel.getRemoteAddress().toString() +
										") closed while reading!");
								this.busyConnections.remove(serverChannel);
								key.channel().close();
								key.cancel();
							}
							
							//����Ѿ��������������ͻؿͻ��ˣ������ж���ͻ��˵����ӡ�
							if (numofbytereaded > 0) {
								try {
									clientChannel.write(this.buffer);
								} catch (IOException e) {
									e.printStackTrace();
									logger.info(String.format("Can't write to channel(%s)", 
											clientChannel.getRemoteAddress().toString()));
									clientChannel.close();
								}
							} else {
								clientChannel.close();
							}
						}
					}
				}
				this.processNextReq();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
