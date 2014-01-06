package jmemproxy;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import jmemproxy.backend.MemcachedNode;
import jmemproxy.backend.VirtualHAServer;
import jmemproxy.hashing.Ketama;

public class JMemProxyConfig {
	

	public void initialParams() {
		
	}

	public JMemProxyClientRequestHandler getFrontend() {
		try {
			return new JMemProxyClientRequestHandler(11218, InetAddress.getLocalHost());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<VirtualHAServer> getServers() {
		List<VirtualHAServer> servers = new LinkedList<VirtualHAServer>();
		return servers;
	}

	public Ketama getHashfunction() {
		return new Ketama(1, null);
	}

	public JMemProxyClientRequestHandler getClientHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
