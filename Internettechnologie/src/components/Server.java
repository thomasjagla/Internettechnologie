package components;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;

public class Server {
	public static void main(String argv[]) throws IOException {

		// Decl
		final int SOCKET_PORT = 80;

		boolean running = true;

		ServerSocket acceptSocket = null;
		Socket clientSocket = null;

		File file = null;

		Thread[] threads = null;

		// Serversocket
		acceptSocket = new ServerSocket(SOCKET_PORT);

		// File
		file = new File("./src/miscellaneous/test.html");
		System.out.println("Loading File from: " + file.getAbsolutePath());

		// JSON FILE DATA
		JsonSerializer js = new JsonSerializer();
		js.addDouble("datasize", (double) file.getTotalSpace());
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		js.addString("last modified", sdf.format(file.lastModified()));
		System.out.println(js.getString());

		// Server stuff

		try {
			threads = new Thread[20];
			int index = 0;
			while (running) {
				System.out.print("Accepting Socket... ");
				clientSocket = acceptSocket.accept();
				System.out.println("Socket accepted: " + clientSocket.toString());

				while (true) {
					if (threads[index] == null) {
						break;
					}
					else if (threads[index].isAlive() == false) {
						break;
					}
					if (++index == threads.length)
						index = 0;
				}
				threads[index] = new Thread(new ServerThread(clientSocket));
				threads[index].run();
			}
		}
		finally {
			System.out.print("Server closing...");
			try {
				for (int i = 0; i < threads.length; i++) {
					threads[i].join();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (acceptSocket != null)
				acceptSocket.close();
			System.out.println("Successfull. \n");
		}
	}
}
