package jmemproxy;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.memcache.ClientRequest;
import jmemproxy.memcache.MemcacheHandler;
import jmemproxy.memcache.MemcacheInteractor;


public class JMemProxy {
	private int         port;
	private InetAddress host;
	
	private static JMemProxyHandler       frontend;
	private static JMemProxyConfig        config;
	private static List<MemcacheHandler>  handlers;
	private static Ketama                 ketama;
	
	public JMemProxy() throws Exception {
		this.host     = host;
		this.port     = port;

		JMemProxy.config   = new JMemProxyConfig();
		JMemProxy.config.initialParams();
		
		JMemProxy.frontend = JMemProxy.config.initialFrontend();
		JMemProxy.handlers = JMemProxy.config.initialMemcacheHandlers();
		JMemProxy.ketama   = JMemProxy.config.initialHashfunction();
	}
	
	public void start() {
		System.out.println("JMemProxy running on port " + this.port);
		JMemProxy.frontend.run();
		for (MemcacheHandler handler : JMemProxy.handlers) {
			handler.run();
		}
	}
	
	public static void processRequest(ClientRequest req) throws IOException {
		MemcacheInteractor interactor = JMemProxy.ketama.getServer("");
		interactor.pushRequest(req);
	}
	
	public static void main(String[] args) throws Exception {
		JMemProxy proxy = new JMemProxy();
		proxy.start();
	}
}
