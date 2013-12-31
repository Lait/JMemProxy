package jmemproxy;

import java.net.InetAddress;


public class JMemProxy {
	private int         port;
	private InetAddress host;
	
	private JMemProxyBackend  backend;
	private JMemProxyFrontend frontend;
	
	public JMemProxy(InetAddress host, int port) throws Exception {
		this.host     = host;
		this.port     = port;
		
		this.backend  = new JMemProxyBackend();
		this.frontend = new JMemProxyFrontend(this.port, this.host, this.backend);	
	}
	
	public void start() {
		System.out.println("JMemProxy running on port " + this.port);
		this.frontend.run();
		this.backend.run();
	}
	
	public static void main(String[] args) throws Exception {
		JMemProxy proxy = new JMemProxy(InetAddress.getLocalHost(), 11218);
		proxy.start();
	}
}
