package jmemproxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jmemproxy.memcache.ServerNode;
import jmemproxy.memcache.MemcacheServerNode;
import jmemproxy.consistenthashing.Ketama;
import jmemproxy.consistenthashing.MD5Hash;


public class JMemProxy implements Runnable {
	private int port;
	private InetAddress host;
	private ServerSocketChannel channel;
	private Selector frontSelector;
	private Selector backSelector;
	private ByteBuffer buffer = ByteBuffer.allocate(1024);
	
	//Is it necessary to put a list here?
	private List<ServerNode> serverList;
	
	private Ketama ketama;
	
	private Map<SocketChannel, byte[]> messages = new HashMap<SocketChannel, byte[]>();
	
	public JMemProxy(InetAddress host, int port) throws Exception {
		this.host          = host;
		this.port          = port;
		
		this.channel       = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		
		this.frontSelector = Selector.open();
		this.backSelector  = null;
		
		//this.serverList    = new LinkedList<ServerNode>();
		this.ketama        = new Ketama(new MD5Hash(), 1, this.serverList);
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		System.out.println("JMemProxy running on port " + this.port);
		try {
			
			//Maybe useful in the future?
			//channel.socket().bind(new InetSocketAddress(host, port));
			
			channel.socket().bind(new InetSocketAddress(port));
			channel.register(frontSelector, SelectionKey.OP_ACCEPT);
			while (true) {
				int count = frontSelector.select();
				if (count < 1) {
					continue;
				}
				Iterator<SelectionKey> iterator = frontSelector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					if (!key.isValid()) {
						continue;
					}
					if (key.isAcceptable()) {
						SocketChannel ch = ((ServerSocketChannel)key.channel()).accept();
						ch.configureBlocking(false);
						ch.register(frontSelector, SelectionKey.OP_READ);
					} else if (key.isReadable()) {
						read(key);
					} else if (key.isWritable()) {
						write(key);
					}
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	//Read
	private void read(SelectionKey key) throws IOException {
		SocketChannel aChannel = (SocketChannel)key.channel();
		buffer.clear();
		int num = aChannel.read(buffer);
		if (num == -1) {
			key.cancel();
		} else if (num > 0) {
			buffer.flip();
			byte[] buf = Arrays.copyOfRange(buffer.array(), 0, num);
			messages.put(aChannel, buf);
			//将对应Channel注册写事件
			key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
			System.out.println("read from: " + aChannel.socket().getRemoteSocketAddress() + 
							   "; message: " + new String(buf));
		}
	}
	
	//Write
	private void write(SelectionKey key) throws IOException {
		SocketChannel aChannel = (SocketChannel)key.channel();
		byte[] buf = messages.get(aChannel);
		if (buf != null) {
			messages.remove(aChannel);
			key.interestOps(SelectionKey.OP_READ);
			buffer.clear();
			buffer.put(buf);
			buffer.flip();
			aChannel.write(buffer);
			System.out.println("write to: " + aChannel.socket().getRemoteSocketAddress() + 
							   "; message: " + new String(buf));
		}
	}
	
	public static void main(String[] args) throws Exception {
		JMemProxy server = new JMemProxy(InetAddress.getLocalHost(), 11218);
		server.run();
	}
}
