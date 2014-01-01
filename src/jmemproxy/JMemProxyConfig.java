package jmemproxy;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.memcache.MemcacheHandler;

public class JMemProxyConfig {

	public void initialParams() {
		
	}

	public JMemProxyHandler initialFrontend() {
		try {
			return new JMemProxyHandler(11218, InetAddress.getLocalHost());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<MemcacheHandler> initialMemcacheHandlers() {
		List<MemcacheHandler> handlers = new LinkedList<MemcacheHandler>();
		return handlers;
	}

	public Ketama initialHashfunction() {
		return new Ketama(1, null);
	}
	
}
