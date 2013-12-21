package jmemproxy.test;

/*
 * TcpClient.java
 * Description:
 * A simple TcpClient for integration testing.
 * 
 * Author: Leon
 * Email : lleon.21.t@gmail.com
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TcpClient {
	public static void main(String argv[]) throws IOException {
		String request;
		String response;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket clientSocket = null;
		try {
			clientSocket = new Socket("127.0.0.1", 11218);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			while (true) {
				request = inFromUser.readLine();
				outToServer.writeBytes(request + '\n');
				response = inFromServer.readLine();
				System.out.println("FROM SERVER: " + response);
			}
		} finally {
			if (clientSocket != null)
				clientSocket.close();
		}

	 }

}
