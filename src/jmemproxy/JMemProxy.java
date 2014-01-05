package jmemproxy;

import java.io.IOException;
import java.util.List;

import jmemproxy.backend.VirtualServer;
import jmemproxy.client.ClientRequest;
import jmemproxy.hashing.Ketama;


public class JMemProxy {	
	private static JMemProxyClientRequestHandler frontend;
	private static JMemProxyConfig        		 config;
	private static List<VirtualServer>           servers;
	private static Ketama                        ketama;
	
	public JMemProxy() throws Exception {
		JMemProxy.config   = new JMemProxyConfig();
		JMemProxy.config.initialParams();
		
		JMemProxy.frontend = JMemProxy.config.getFrontend();
		JMemProxy.servers  = JMemProxy.config.getServers();
		JMemProxy.ketama   = JMemProxy.config.getHashfunction();
	}
	
	public void start() {
		JMemProxy.frontend.start();
		for (VirtualServer server : JMemProxy.servers) {
			server.run();
		}
	}
	
	public static void processRequest(ClientRequest req) throws IOException {
		VirtualServer server = JMemProxy.ketama.getServer(req.getRequestString());
		server.pushRequest(req);
	}
	
	public static void main(String[] args) throws Exception {
		JMemProxy proxy = new JMemProxy();
		proxy.start();
	}
}
