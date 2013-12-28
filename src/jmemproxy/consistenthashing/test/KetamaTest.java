package jmemproxy.consistenthashing.test;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import jmemproxy.consistenthashing.Ketama;
import jmemproxy.consistenthashing.MD5Hash;
import jmemproxy.memcache.MemcacheServer;
import jmemproxy.memcache.MemcacheServer;
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
		List<MemcacheServer> servers = new LinkedList<MemcacheServer>();
		Ketama ketama = new Ketama(new MD5Hash(), 1, servers);
		assertNotNull(ketama);
	}
	
	@Test
	public void testGetAvailableServers() {
		List<MemcacheServer> servers = new LinkedList<MemcacheServer>();
		Ketama ketama = new Ketama(new MD5Hash(), 1, servers);
		assertEquals(ketama.getAvailableServers().size(), 0);
	}

	@Test
	public void testGetServer() {
		List<MemcacheServer> servers = new LinkedList<MemcacheServer>();
		MD5Hash md5 = new MD5Hash();
		Ketama ketama = new Ketama(md5, 1, servers);
		MemcacheServer node1 = new MemcacheServer(1999, "localhost", null);
		MemcacheServer node2 = new MemcacheServer(2000, "localhost", null);
		ketama.addServer(node1);
		ketama.addServer(node2);
		int firstKey = ketama.getAvailableServers().firstKey();
		assertTrue( (firstKey == md5.hash(node1.toString() + 0)) || 
				    (firstKey == md5.hash(node2.toString() + 0))
				  );
	}

	@Test
	public void testAddServer() {
		List<MemcacheServer> servers = new LinkedList<MemcacheServer>();
		Ketama ketama = new Ketama(new MD5Hash(), 1, servers);
		ketama.addServer(new MemcacheServer(1999, "localhost", null));
		ketama.addServer(new MemcacheServer(2000, "localhost", null));
		assertEquals(ketama.getAvailableServers().size(), 2);
	}

}
