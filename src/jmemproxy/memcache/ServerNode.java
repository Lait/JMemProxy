package jmemproxy.memcache;

public abstract class ServerNode {
	protected int port;
	protected String ip;
	
	public int getPort() {
		return this.port;
	}
	
	public String getIp() {
		return this.ip;
	}
}
