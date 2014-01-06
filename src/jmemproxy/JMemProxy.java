package jmemproxy;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import jmemproxy.backend.VirtualHAServer;
import jmemproxy.client.ClientRequest;
import jmemproxy.hashing.Ketama;


public class JMemProxy {	
	private static JMemProxyClientRequestHandler clientHandler;
	private static final int port = 11218;
	
	private static JMemProxyConfig        		 config;
	private static List<VirtualHAServer>         servers;
	private static Ketama                        ketama;
	
	public JMemProxy() throws Exception {

		clientHandler = new JMemProxyClientRequestHandler(port, InetAddress.getLocalHost());
		servers       = new LinkedList<VirtualHAServer>();
		servers.add(new VirtualHAServer(1234, "127.0.0.1"));
		ketama        = new Ketama(1, null);
	}
	
	public void start() {
		clientHandler.start();
		for (VirtualHAServer server : JMemProxy.servers) {
			server.run();
		}
	}
	
	public static void processRequest(ClientRequest req) throws IOException {
		VirtualHAServer server = ketama.getServer(req.getRequestString());
		server.pushRequest(req);
	}
	
	public static void main(String[] args) throws Exception {
		JMemProxy proxy = new JMemProxy();
		proxy.start();
	}
}
