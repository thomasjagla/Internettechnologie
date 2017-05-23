package components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestClient {
	public static void main(String[] args) throws IOException {
		Socket clientSocket = null;
		
		
		try {
			clientSocket = new Socket("localhost", 80);
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
			out.write("GET / HTTP/1.1");
			out.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			while(in.ready()) {
				System.out.println("Server-Antwort: " + in.readLine());
			}
			
		} finally {
			clientSocket.close();
		}
	}
}
