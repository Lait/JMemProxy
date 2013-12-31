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
	private static Selector         selector;
	private static JMemProxyBackend backend;
	
	private int                     port;
	private InetAddress             host;
	
	private ServerSocketChannel     channel;
	private ByteBuffer              globalBuffer;
	
	public JMemProxyFrontend(int port, InetAddress host, JMemProxyBackend backend) throws IOException {
		JMemProxyFrontend.selector = Selector.open();
		JMemProxyFrontend.backend  = backend;
		
		this.port = port;
		this.host = host;
		
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		
		this.globalBuffer = ByteBuffer.allocate(1024);
	}
	
	//Read from a client socket, and dispatch the request to the back end .
	private void readAndDispatch(SelectionKey key) throws IOException {
		SocketChannel aChannel = (SocketChannel)key.channel();
		globalBuffer.clear();
		int num = aChannel.read(globalBuffer);
		if (num > 0) {
			globalBuffer.flip();
			byte[] buf = Arrays.copyOfRange(globalBuffer.array(), 0, num);
			
			//key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
			System.out.println("read from: " + aChannel.socket().getRemoteSocketAddress() + 
							   "; message: " + new String(buf));
		}
		else {
			key.cancel();
		}
	}
	
	public void run() {
		try {
			channel.socket().bind(new InetSocketAddress(port)); //or InetSocketAddress(host, port)
			channel.register(JMemProxyFrontend.selector, SelectionKey.OP_ACCEPT);
			while (true) {
				int count = JMemProxyFrontend.selector.select();
				if (count < 1) continue;
				
				Iterator<SelectionKey> iterator = JMemProxyFrontend.selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					
					if (!key.isValid()) continue;
					if (key.isAcceptable()) {
						SocketChannel ch = ((ServerSocketChannel)key.channel()).accept();
						ch.configureBlocking(false);
						ch.register(JMemProxyFrontend.selector, SelectionKey.OP_READ);
					} else if (key.isReadable()) {
						readAndDispatch(key);
					}
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
