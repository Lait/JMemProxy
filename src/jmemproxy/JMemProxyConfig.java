package jmemproxy;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.memcache.MemcacheHandler;

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

	public List<MemcacheHandler> getMemcacheHandlers() {
		List<MemcacheHandler> handlers = new LinkedList<MemcacheHandler>();
		return handlers;
	}

	public Ketama getHashfunction() {
		return new Ketama(1, null);
	}
	
}
