package jmemproxy;

/*
 * JMemProxyBackend.java
 * Description:
 * The back-end logic.
 * 
 * Author: Leon
 * Email : lleon.21.t@gmail.com
 */

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.List;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.consistenthashing.MD5Hash;
import jmemproxy.memcache.ServerNode;

public class JMemProxyBackend implements Runnable {
	private static Selector selector;
	
	private List<ServerNode> serverList;
	private List<ServerNode> backupServerList;
	private Ketama ketama;
	

	
	public JMemProxyBackend(List<ServerNode> serverList, List<ServerNode> backupServerList) throws IOException {
		this.serverList       = serverList;
		this.backupServerList = backupServerList;
		this.ketama           = new Ketama(new MD5Hash(), 1, this.serverList);
		JMemProxyBackend.selector = Selector.open();
		
		this.initialize();
	}
	
	private void initialize() {
		
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
}
