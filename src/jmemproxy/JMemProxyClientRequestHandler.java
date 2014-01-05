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
import java.util.Iterator;
import java.util.logging.Logger;

import jmemproxy.client.ClientRequest;

public class JMemProxyClientRequestHandler extends Thread {
	private static Selector selector;
	private static final Logger logger = Logger.getLogger(JMemProxyClientRequestHandler.class.getName());
	
	private int                     port;
	private InetAddress             host;
	
	private ServerSocketChannel     channel;
	private ByteBuffer              globalBuffer;
	
	public JMemProxyClientRequestHandler(int port, InetAddress host) throws IOException {
		JMemProxyClientRequestHandler.selector = Selector.open();
		
		this.port = port;
		this.host = host;
		
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		
		this.globalBuffer = ByteBuffer.allocate(1024);
	}
	
	//从客户连接中读取出数据并发送到后端对应的memcached节点。
	private void readAndDispatch(SelectionKey key) throws IOException {
		SocketChannel aChannel = (SocketChannel)key.channel();
		globalBuffer.clear();
		int num = aChannel.read(globalBuffer);
		if (num > 0) {
			globalBuffer.flip();
			byte[] buf = Arrays.copyOfRange(globalBuffer.array(), 0, num);
			JMemProxy.processRequest(new ClientRequest(aChannel, buf));
			System.out.println("read from: " + aChannel.socket().getRemoteSocketAddress() + 
							   ", message: " + new String(buf));
		}
		else {
			key.cancel();
		}
	}
	
	public void run() {
		logger.info("JMemProxy is running on port " + this.port);
		try {
			channel.socket().bind(new InetSocketAddress(port)); //or InetSocketAddress(host, port)
			channel.register(JMemProxyClientRequestHandler.selector, SelectionKey.OP_ACCEPT);
			while (true) {
				int count = JMemProxyClientRequestHandler.selector.select();
				if (count < 1) continue;
				
				Iterator<SelectionKey> iterator = JMemProxyClientRequestHandler.selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					
					if (!key.isValid()) continue;
					if (key.isAcceptable()) {
						SocketChannel ch = ((ServerSocketChannel)key.channel()).accept();
						ch.configureBlocking(false);
						ch.register(JMemProxyClientRequestHandler.selector, SelectionKey.OP_READ);
						logger.info("New connection from client: " + ch.getRemoteAddress().toString());
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
