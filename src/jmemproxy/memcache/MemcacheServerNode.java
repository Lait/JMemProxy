package jmemproxy.memcache;

public class MemcacheServerNode {
	private int port;
	private String ip;
	
	public MemcacheServerNode(int p, String ip) {
		this.port = p;
		this.ip = ip;
	}
}
