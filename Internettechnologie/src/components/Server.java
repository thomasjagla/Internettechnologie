package components;
import java.io.*;
import java.net.*;


public class Server{
	public static void main(String argv[]) throws IOException {
		
		//Decl
		final int SOCKET_PORT = 80;
		boolean running = true;
		ServerSocket acceptSocket = null;
		Socket clientSocket = null;
		OutputStream out = null;
		byte[] bytes = null;
		File file = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		long fileLength = 0;
		
		//Serversocket
		acceptSocket = new ServerSocket(SOCKET_PORT);

		//File
		file = new File("./src/miscellaneous/test.html");
		System.out.println("Loading File from: "+file.getAbsolutePath());
		bytes = new byte[(int)file.length()];
		fis = new FileInputStream(file);
		bis = new BufferedInputStream(fis);
		bis.read(bytes,0,bytes.length);	//Reads File to byte array
		System.out.println("Loading File complete. \n");
		
		//Server stuff
		try {
			while(running) {
				try {
					System.out.println("Accepting Socket...");
					clientSocket = acceptSocket.accept();
					System.out.println("Socket accepted: "+ clientSocket.toString());
					out = clientSocket.getOutputStream();
					
					System.out.println("Sending "+file.toString()+"("+bytes.length+") to "+clientSocket.toString()+" ...");
					out.write(bytes, 0, bytes.length);
					out.flush();
					System.out.println("Sending complete.");
				}
				finally {
					System.out.println("Closing Connection...");
					if(bis != null) bis.close();
					if(out != null) out.close();
					if(clientSocket != null) clientSocket.close();
					System.out.println("Connection closed \n");
					
				}
			}
		}
		finally {
			System.out.println("Server closed.");
			if(acceptSocket != null) acceptSocket.close();
		}
	}
}
