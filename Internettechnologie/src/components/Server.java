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

		OutputStream out = null;
		InputStream in = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		BufferedReader buff = null;

		String[] httpHeader = new String[20];
		String getTarget = "";

		byte[] headerBytes = null;
		byte[] fileBytes = null;

		File file = null;
		long fileLength = 0;

		// Serversocket
		acceptSocket = new ServerSocket(SOCKET_PORT);

		// File
		file = new File("./src/miscellaneous/test.html");
		System.out.println("Loading File from: " + file.getAbsolutePath());
		fileBytes = new byte[(int) file.length()];
		System.out.println("Loading File complete. \n");

		// JSON FILE DATA
		JsonSerializer js = new JsonSerializer();
		js.addDouble("datasize", (double) file.getTotalSpace());
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		js.addString("last modified", sdf.format(file.lastModified()));
		System.out.println(js.getString());

		// Server stuff

		try {

			while (running) {
				try {

					out = null;
					in = null;
					fis = null;
					bis = null;
					buff = null;

					httpHeader = new String[20];
					getTarget = "";

					headerBytes = null;
					fileBytes = null;

					file = null;
					fileLength = 0;

					System.out.print("Accepting Socket... ");
					clientSocket = acceptSocket.accept();
					System.out.println("Socket accepted: " + clientSocket.toString());

					out = clientSocket.getOutputStream();
					in = clientSocket.getInputStream();

					buff = new BufferedReader(new InputStreamReader(in));
					int httpHeaderLines = 0;
					while (buff.ready())
						httpHeader[httpHeaderLines++] = buff.readLine();
					for (int i = 0; i < httpHeaderLines; i++)
						System.out.println("HTTP-Header[" + i + "]: " + httpHeader[i]);

					if (httpHeader[0] != null) {
						if (httpHeader[0].contains("GET")) {
							getTarget = httpHeader[0].substring(httpHeader[0].indexOf(" ") + 1, httpHeader[0].length());
							getTarget = getTarget.substring(0, getTarget.indexOf(" "));

							file = new File("./src/miscellaneous" + getTarget);
							if (!file.isFile()) {
								System.out.println(getTarget + " not found. Selected index.html instead.");
								file = new File("./src/miscellaneous/index.html");
							}
							fileBytes = new byte[(int) file.length()];
							fis = new FileInputStream(file);
							bis = new BufferedInputStream(fis);
							bis.read(fileBytes, 0, fileBytes.length);

							httpHeader = new String[4];
							httpHeader[0] = "HTTP/1.1 200";
							httpHeader[1] = "Server: HTTPd/1.0 Date: Wed, 17 May 2017 15:32:15 CET";
							httpHeader[2] = "Content-Type: text/html";
							httpHeader[3] = "Content-Length: " + fileBytes.length;
							headerBytes = (httpHeader[0] + "\n" + httpHeader[1] + "\n" + httpHeader[2] + "\n" + httpHeader[3] + "\n\n").getBytes();

							System.out.print("Sending HTTP-Header to " + clientSocket.toString() + " ... ");
							out.write(headerBytes);
							out.flush();
							System.out.println("Successfull.");

							System.out.print("Sending " + file.toString() + "(" + fileBytes.length + ") to " + clientSocket.toString() + " ... ");
							out.write(fileBytes, 0, fileBytes.length);
							out.flush();
							System.out.println("Successfull.");
						}
						else { // httpHeader[0] enthält kein GET

						}
					}
					else { // httpHeader[0] ist null

					}

				}
				finally {
					System.out.print("Closing Connection... ");
					if (bis != null)
						bis.close();
					if (out != null)
						out.close();
					if (clientSocket != null)
						clientSocket.close();
					System.out.println("Successfull. \n");
				}
			}

		}
		finally {
			System.out.print("Server closing...");
			if (acceptSocket != null)
				acceptSocket.close();
			System.out.println("Successfull. \n");
		}
	}
}
