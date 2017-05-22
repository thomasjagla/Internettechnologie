package components;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ServerThread implements Runnable {

	Socket clientSocket = null;

	OutputStream oStream = null;
	BufferedInputStream bis = null;
	BufferedReader bReader = null;

	String[] httpHeader = null;
	String getTarget = null;
	String getParameter = null;

	byte[] headerBytes = null;
	byte[] fileBytes = null;

	File file = null;
	long fileLength = 0;

	public ServerThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
		bis = null;
		oStream = null;
		bReader = null;
		httpHeader = null;
		getTarget = null;
		getParameter = null;

		headerBytes = null;
		fileBytes = null;

		file = null;
		fileLength = 0;
	}

	@Override
	public void run() {
		try {
			oStream = clientSocket.getOutputStream();
			bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			httpHeader = new String[20];
			for (int i = 0; i < 20; i++) {
				if (bReader.ready())
					httpHeader[i] = bReader.readLine();
				else
					break;
			}

			for (int i = 0; i < httpHeader.length; i++) {
				if (httpHeader[i] != null)
					System.out.println("HTTP-Header[" + i + "]: " + httpHeader[i]);
			}
			if (httpHeader[0] != null) {
				if (httpHeader[0].substring(0, 3).equals("GET")) { // GET
					getTarget = httpHeader[0].substring(httpHeader[0].indexOf(" ") + 1, httpHeader[0].length());
					getTarget = getTarget.substring(0, getTarget.indexOf(" "));

					file = new File("./src/miscellaneous" + getTarget);
					if (!file.isFile()) {
						System.out.println(getTarget + " not found. Selected index.html instead.");
						file = new File("./src/miscellaneous/index.html");
					}

					fileBytes = new byte[(int) file.length()];
					bis = new BufferedInputStream(new FileInputStream(file));
					bis.read(fileBytes, 0, fileBytes.length);

					httpHeader = new String[4];
					httpHeader[0] = "HTTP/1.1 200";
					httpHeader[1] = "Server: HTTPd/1.0 Date: Wed, 17 May 2017 15:32:15 CET";
					httpHeader[2] = "Content-Type: text/html";
					httpHeader[3] = "Content-Length: " + fileBytes.length;
					headerBytes = (httpHeader[0] + "\n" + httpHeader[1] + "\n" + httpHeader[2] + "\n" + httpHeader[3] + "\n\n").getBytes();

					System.out.print("Sending ... ");
					oStream.write(headerBytes);
					oStream.write(fileBytes, 0, fileBytes.length);
					oStream.flush();
					System.out.println("Done.");
				}
				else if (httpHeader[0].substring(0, 4).equals("POST")) { // POST
					System.out.println("POST, bisher nur GET");
				}
				else { // HTTP-Header[0] unbekannt
					System.out.println("~ Weder GET noch POST");
				}
			}
			else { // HTTP-Header = null
				System.out.println("~ Kein HTTP-Header");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (clientSocket != null)
				try {
					clientSocket.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}
