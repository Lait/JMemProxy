package jmemproxy;

import java.net.InetAddress;
import java.util.List;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.memcache.MemcacheHandler;


public class JMemProxy {
	private int         port;
	private InetAddress host;
	
	private JMemProxyHandler       frontend;
	private JMemProxyConfig        config;
	private List<MemcacheHandler>  handlers;
	private Ketama                 ketama;
	
	public JMemProxy(InetAddress host, int port) throws Exception {
		this.host     = host;
		this.port     = port;

		this.config   = new JMemProxyConfig();
		this.config.initialParams();
		
		this.frontend = this.config.initialFrontend();
		this.handlers = this.config.initialMemcacheHandlers();
		this.ketama   = this.config.initialHashfunction();
	}
	
	public void start() {
		System.out.println("JMemProxy running on port " + this.port);
		this.frontend.run();
		for (MemcacheHandler handler : this.handlers) {
			handler.run();
		}
	}
	
	public static void main(String[] args) throws Exception {
		JMemProxy proxy = new JMemProxy(InetAddress.getLocalHost(), 11218);
		proxy.start();
	}
}
