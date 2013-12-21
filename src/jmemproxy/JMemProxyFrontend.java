package jmemproxy;

/*
 * JMemProxyFrontend.java
 * Description:
 * The front-end logic.
 * 
 * Author: Leon
 * Email : lleon.21.t@gmail.com
 */

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
import java.util.Map;

public class JMemProxyFrontend implements Runnable {
	private static Selector selector;
	
	private int                 port;
	private InetAddress         host;
	
	private ServerSocketChannel channel;
	private ByteBuffer          buffer;
	
	private Map<SocketChannel, byte[]> messages;
	
	public JMemProxyFrontend(int p, InetAddress h) throws IOException {
		this.port = p;
		this.host = h;
		
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		
		JMemProxyFrontend.selector = Selector.open();
		this.buffer   = ByteBuffer.allocate(1024);
		
		messages = new HashMap<SocketChannel, byte[]>();
	}
	
	//Read from a client socket.
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
			//Change the interested event of this key.
			key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
			System.out.println("read from: " + aChannel.socket().getRemoteSocketAddress() + 
							   "; message: " + new String(buf));
		}
	}
	
	//Write to a client socket.
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
	
	public void run() {
		try {
			
			//Maybe useful in the future?
			//channel.socket().bind(new InetSocketAddress(host, port));
			
			channel.socket().bind(new InetSocketAddress(port));
			channel.register(JMemProxyFrontend.selector, SelectionKey.OP_ACCEPT);
			while (true) {
				int count = JMemProxyFrontend.selector.select();
				if (count < 1) {
					continue;
				}
				Iterator<SelectionKey> iterator = JMemProxyFrontend.selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					if (!key.isValid()) {
						continue;
					}
					if (key.isAcceptable()) {
						SocketChannel ch = ((ServerSocketChannel)key.channel()).accept();
						ch.configureBlocking(false);
						ch.register(JMemProxyFrontend.selector, SelectionKey.OP_READ);
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

}
