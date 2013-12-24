package jmemproxy.test;

import static org.junit.Assert.*;

import java.io.IOException;

import jmemproxy.JMemProxyBackend;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JMemProxyBackendTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInitialize() {
		try {
			JMemProxyBackend backend = new JMemProxyBackend();
			assertEquals(backend.serverNum(), 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
