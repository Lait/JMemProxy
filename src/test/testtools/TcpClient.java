package test.testtools;

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
import java.nio.CharBuffer;

public class TcpClient {
	public static void main(String argv[]) throws IOException {
		String request;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		String response;
		String temp;
		Socket clientSocket = null;
		try {
			clientSocket = new Socket("127.0.0.1", 11212);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			while (true) {
				request = inFromUser.readLine();
				request += "\r\n";
				if ( !(request.equals("version") || request.equals("flush_all")) ) {
					request += inFromUser.readLine();
					request += "\r\n";
				}

				outToServer.writeBytes(request);
				response = inFromServer.readLine();
				System.out.println("FROM SERVER: " + response);
			}
		} finally {
			if (clientSocket != null)
				clientSocket.close();
		}

	 }

}
