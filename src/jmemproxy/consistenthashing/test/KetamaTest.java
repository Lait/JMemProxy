package jmemproxy.consistenthashing.test;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.consistenthashing.MD5Hash;
import jmemproxy.memcache.MemcacheServerNode;
import jmemproxy.memcache.ServerNode;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KetamaTest {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testKetama() {
		List<ServerNode> servers = new LinkedList<ServerNode>();
		Ketama ketama = new Ketama(new MD5Hash(), 1, servers);
		assertNotNull(ketama);
	}
	
	@Test
	public void testGetAvailableServers() {
		List<ServerNode> servers = new LinkedList<ServerNode>();
		Ketama ketama = new Ketama(new MD5Hash(), 1, servers);
		assertEquals(ketama.getAvailableServers().size(), 0);
	}

	@Test
	public void testGetServer() {
		List<ServerNode> servers = new LinkedList<ServerNode>();
		MD5Hash md5 = new MD5Hash();
		Ketama ketama = new Ketama(md5, 1, servers);
		MemcacheServerNode node1 = new MemcacheServerNode(1999, "localhost", null);
		MemcacheServerNode node2 = new MemcacheServerNode(2000, "localhost", null);
		ketama.addServer(node1);
		ketama.addServer(node2);
		int firstKey = ketama.getAvailableServers().firstKey();
		assertTrue( (firstKey == md5.hash(node1.toString() + 0)) || 
				    (firstKey == md5.hash(node2.toString() + 0))
				  );
	}

	@Test
	public void testAddServer() {
		List<ServerNode> servers = new LinkedList<ServerNode>();
		Ketama ketama = new Ketama(new MD5Hash(), 1, servers);
		ketama.addServer(new MemcacheServerNode(1999, "localhost", null));
		ketama.addServer(new MemcacheServerNode(2000, "localhost", null));
		assertEquals(ketama.getAvailableServers().size(), 2);
	}

}
