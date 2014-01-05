package jmemproxy.backend.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import jmemproxy.backend.*;
import jmemproxy.client.ClientRequest;

public class VirtualServerTest {

	@Test
	public void testVirtualServerSync() {
		try {
			VirtualServer server = new VirtualServer("127.0.0.1", 1234);
			server.getNode().start();
			System.out.println("Hello");
			assertTrue(server.pushRequest(new ClientRequest(null, null)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
