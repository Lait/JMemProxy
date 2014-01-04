package jmemproxy;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.memcache.ClientRequest;
import jmemproxy.memcache.MemcacheHandler;
import jmemproxy.memcache.MemcacheInteractor;


public class JMemProxy {	
	private static JMemProxyClientRequestHandler frontend;
	private static JMemProxyConfig        		 config;
	private static List<MemcacheHandler>         handlers;
	private static Ketama                        ketama;
	
	public JMemProxy() throws Exception {
		JMemProxy.config   = new JMemProxyConfig();
		JMemProxy.config.initialParams();
		
		JMemProxy.frontend = JMemProxy.config.getFrontend();
		JMemProxy.handlers = JMemProxy.config.getMemcacheHandlers();
		JMemProxy.ketama   = JMemProxy.config.getHashfunction();
	}
	
	public void start() {
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
