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
import java.util.Map;

public class JMemProxy implements Runnable {
	private int port;
	private InetAddress host;
	private ServerSocketChannel channel;
	private Selector selector;
	private ByteBuffer buffer = ByteBuffer.allocate(1024);
	
	/**
	 * 若要保证所有信息都传回client, 这里可以把byte[]改为队列或其他容器
	 */
	private Map<SocketChannel, byte[]> messages = new HashMap<SocketChannel, byte[]>();
	
	public JMemProxy(InetAddress host, int port) throws Exception {
		this.host = host;
		this.port = port;
		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		selector = Selector.open();
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.println("JMemProxy running on port " + this.port);
		try {
			channel.socket().bind(new InetSocketAddress(port));
			channel.register(selector, SelectionKey.OP_ACCEPT);
			while (true) {
				int count = selector.select();
				if (count < 1) {
					continue;
				}
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					if (!key.isValid()) {
						continue;
					}
					if (key.isAcceptable()) {
						SocketChannel ch = ((ServerSocketChannel)key.channel()).accept();
						ch.configureBlocking(false);
						ch.register(selector, SelectionKey.OP_READ);
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
	
	/**
	 * 读操作
	 * @param key
	 * @throws IOException
	 */
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
	
	/**
	 * 写操作
	 * @param key
	 * @throws IOException
	 */
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
