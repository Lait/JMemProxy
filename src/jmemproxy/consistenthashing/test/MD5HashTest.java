package jmemproxy.consistenthashing.test;

import static org.junit.Assert.*;
import jmemproxy.consistenthashing.MD5Hash;
import jmemproxy.memcache.MemcacheServerNode;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MD5HashTest {
	private MD5Hash md5 = new MD5Hash();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHashUniqueness() {
		int r1 = md5.hash("Hello I'm a key");
		int r2 = md5.hash("Hello I'm a key");
		assertEquals(r1, r2);
		
		r1 = md5.hash((new MemcacheServerNode(1999, "localhost", null)).toString());
		r2 = md5.hash((new MemcacheServerNode(1999, "localhost", null)).toString());
		assertNotEquals(r1, r2);
	}

}
