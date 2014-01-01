package jmemproxy;

import java.util.List;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.memcache.MemcacheHandler;

public class JMemProxyConfig {

	public void initialParams() {
		// TODO Auto-generated method stub
		
	}

	public JMemProxyHandler initialFrontend() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<MemcacheHandler> initialMemcacheHandlers() {
		// TODO Auto-generated method stub
		return null;
	}

	public Ketama initialHashfunction() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
