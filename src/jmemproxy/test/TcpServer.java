package jmemproxy.test;

/*
 * TcpServer.java
 * Description:
 * A multiThreaded TcpServer for testing.
 * May act as a fake Memcache server.
 * 
 * Author: Leon
 * Email : lleon.21.t@gmail.com
 */

import java.io.*;
import java.net.*;

class ServerThread implements Runnable {
	
	private Socket s;
	private BufferedReader read = null;
	private PrintStream write = null;
	
	public ServerThread(Socket socket) {
		this.s = socket;
		try {
			this.read = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.write = new PrintStream(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		String request = null;
		String response = "Hello i'm server on " + s.getInetAddress().toString();
		StringBuffer sb = new StringBuffer();
		try {
			while(true) {
				request = this.read.readLine();
				sb.setLength(0);
				sb.append("From ");
				sb.append(this.s.getRemoteSocketAddress().toString());
				sb.append(":");
				sb.append(request);
				System.out.println(sb.toString());
				this.write.write(response.getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (!this.s.isClosed()) this.s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}

public class TcpServer {
	
	public static void main(String args[]) throws IOException {
		ServerSocket srvr = null;
		Socket skt = null;
		int port = 1234;
		
		if (args.length != 0) 
			port = Integer.parseInt(args[0]);
		
		try {
			srvr = new ServerSocket(port);
			System.out.println("Server running on port " + port);
			while (true) {
				skt = srvr.accept();
				new ServerThread(skt).run();
			}

		} catch(Exception e) {
			System.out.print("Whoops! It didn't work!\n");
		} finally {
			if (srvr != null) srvr.close();
			if (skt != null) skt.close();
		}
	}
}
