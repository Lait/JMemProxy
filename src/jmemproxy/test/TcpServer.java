package jmemproxy.test;

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

	@Override
	public void run() {
		String request = null;
		String response = "Hello i'm server";
		try {
			while(true) {
				request = this.read.readLine();
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
      try {
         srvr = new ServerSocket(1234);
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
