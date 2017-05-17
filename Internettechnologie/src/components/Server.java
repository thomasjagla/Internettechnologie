package components;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;


public class Server{
	public static void main(String argv[]) throws IOException {
		
		//Decl
		final int SOCKET_PORT = 80;
		
		boolean running = true;
		
		ServerSocket acceptSocket = null;
		Socket clientSocket = null;
		
		OutputStream out = null;
		InputStream in = null;
		BufferedReader buff = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		
		byte[] bytes = null;
		File file = null;
		long fileLength = 0;
		
		String[] httpInfo = new String[9];
		String getTarget = "";
		
		//Serversocket
		acceptSocket = new ServerSocket(SOCKET_PORT);

		//File
		file = new File("./src/miscellaneous/test.html");
		System.out.println("Loading File from: "+file.getAbsolutePath());
		bytes = new byte[(int)file.length()];
		System.out.println("Loading File complete. \n");
		
		//JSON FILE DATA
		JsonSerializer js = new JsonSerializer();
		js.addDouble("datasize", (double)file.getTotalSpace());
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		js.addString("last modified", sdf.format(file.lastModified()));
		System.out.println(js.getString());
		
		//Server stuff
		try {
			while(running) {
				try {
					System.out.println("Accepting Socket...");
					clientSocket = acceptSocket.accept();
					System.out.println("Socket accepted: "+ clientSocket.toString());
					out = clientSocket.getOutputStream();
					in = clientSocket.getInputStream();
					buff = new BufferedReader(new InputStreamReader(in));
					
					int httpLines = 0;
					while(buff.ready())httpInfo[httpLines++] = buff.readLine();
					for(int i=0; i<httpLines; i++) System.out.println("HTTP-INFO: "+httpInfo[i]);
					
					getTarget = httpInfo[0].substring(httpInfo[0].indexOf(" ")+1, httpInfo[0].length());
					getTarget = getTarget.substring(0, getTarget.indexOf(" "));
	
					String httpResponse = "";
					
					if(getTarget.equals("/close")){
						running = false;
					}
					else if(getTarget.equals("/")){
						try {
							file = new File("./src/miscellaneous/index.html");
							bytes = new byte[(int)file.length()];
							fis = new FileInputStream(file);
							bis = new BufferedInputStream(fis);
							bis.read(bytes,0,bytes.length);
							
							httpResponse = httpResponse+"HTTP/1.1 200\n";
							httpResponse = httpResponse+"Server: HTTPd/1.0 Date: Wed, 17 May 2017 15:32:15 CET\n";
							httpResponse = httpResponse+"Content-Type: text/html\n";
							httpResponse = httpResponse+"Content-Length: "+bytes.length+"\n\n";
							
							bytes = httpResponse.getBytes();
							System.out.println("Sending HTTP-Response to "+clientSocket.toString()+" ...");
							out.write(bytes);
							out.flush();
							
							bytes = new byte[(int)file.length()];
							fis = new FileInputStream(file);
							bis = new BufferedInputStream(fis);
							bis.read(bytes,0,bytes.length);
							System.out.println("Sending "+file.toString()+"("+bytes.length+") to "+clientSocket.toString()+" ...");
							out.write(bytes, 0, bytes.length);
							
							out.flush();
							System.out.println("Sending complete.");
						}catch(FileNotFoundException e) {System.out.println("~ERROR  Failed to access index.html");}
					}
					else {
						try {
							file = new File("./src/miscellaneous"+getTarget);
							bytes = new byte[(int)file.length()];
							fis = new FileInputStream(file);
							bis = new BufferedInputStream(fis);
							bis.read(bytes,0,bytes.length);
						
							httpResponse = httpResponse+"HTTP/1.1 200\n";
							httpResponse = httpResponse+"Server: HTTPd/1.0 Date: Wed, 17 May 2017 15:32:15 CET\n";
							httpResponse = httpResponse+"Content-Type: text/html\n";
							httpResponse = httpResponse+"Content-Length: "+bytes.length+"\n\n";
						
							bytes = httpResponse.getBytes();
							System.out.println("Sending HTTP-Response to "+clientSocket.toString()+" ...");
							out.write(bytes);
							out.flush();
						
							bytes = new byte[(int)file.length()];
							fis = new FileInputStream(file);
							bis = new BufferedInputStream(fis);
							bis.read(bytes,0,bytes.length);
							System.out.println("Sending "+file.toString()+"("+bytes.length+") to "+clientSocket.toString()+" ...");
							out.write(bytes, 0, bytes.length);
						
							out.flush();
							System.out.println("Sending complete.");
						}catch(FileNotFoundException e) {System.out.println("~ERROR  Client tried to access: "+getTarget);}
					}
					
				}
				finally {
					System.out.println("Closing Connection...");
					if(bis != null) bis.close();
					if(out != null) out.close();
					if(clientSocket != null) clientSocket.close();
					System.out.println("Connection closed. \n");
					
				}
			}
		}
		finally {
			System.out.println("Server closing...");
			if(acceptSocket != null) acceptSocket.close();
			System.out.println("Server closed. \n");
		}
	}
	
	public void sendHTTP(File file) {
		
	}
}
