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

import jmemproxy.common.Request;

public class JMemProxyFrontend extends Thread {
	private static Selector selector;
	private static final Logger logger = Logger.getLogger(JMemProxyFrontend.class.getName());
	
	private int                     port;
	private InetAddress             host;
	
	private ServerSocketChannel     channel;
	private ByteBuffer              globalBuffer;
	
	public JMemProxyFrontend(int port, InetAddress host) throws IOException {
		JMemProxyFrontend.selector = Selector.open();
		
		this.port = port;
		this.host = host;
		
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		
		this.globalBuffer = ByteBuffer.allocate(1024);
	}
	
	private void readAndDispatchToBackend(SelectionKey key) throws IOException {
		SocketChannel aChannel = (SocketChannel)key.channel();
		globalBuffer.clear();
		try {
			int num = aChannel.read(globalBuffer);
			if (num > 0) {
				globalBuffer.flip();
				byte[] buf = Arrays.copyOfRange(globalBuffer.array(), 0, num);
				JMemProxy.getInstance().dispatchRequest(new Request(aChannel, buf));
				System.out.println("read from: " + aChannel.socket().getRemoteSocketAddress() + 
								   ", message: " + new String(buf));
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.info(String.format("Connection from client(%s) closed!\n", 
					aChannel.getRemoteAddress().toString()));
			aChannel.close();
		}
		

	}
	
	public void run() {
		logger.info("JMemProxy is running on port " + this.port);
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
						logger.info("New connection from client: " + ch.getRemoteAddress().toString());
					} else if (key.isReadable()) {
						readAndDispatchToBackend(key);
					}
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
