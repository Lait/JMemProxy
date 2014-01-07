package jmemproxy;

import java.io.IOException;
import java.net.InetAddress;

import jmemproxy.common.Request;

public class JMemProxy {
	private static JMemProxy instance = new JMemProxy();
	private static final String configurePath = "..\\conf\\jmemproxy.conf";
	private static final String dataPaht = "..\\data\\jmemproxy.db";
	
	private JMemProxyFrontend frontend;
	private JMemProxyBackend  backend;
	
	public static JMemProxy getInstance() {
		return instance;
	}
	
	private JMemProxy() {
		try {
			this.frontend = new JMemProxyFrontend(11218, InetAddress.getLocalHost());
			this.backend = new JMemProxyBackend();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dispatchRequest(Request req) {
		try {
			this.backend.processRequest(req);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		this.frontend.start();
		this.backend.start();
	}
	
	public static void main(String[] args) throws Exception {
		JMemProxy proxy = JMemProxy.getInstance();
		proxy.start();
	}


}
