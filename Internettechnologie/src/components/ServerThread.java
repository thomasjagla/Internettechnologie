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
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerThread implements Runnable {

	//DECL
	Socket clientSocket = null;

	OutputStream oStream = null;
	BufferedInputStream bis = null;
	InputStreamReader inputReader = null;
	BufferedReader bReader = null;
	Scanner sc = null;
	ArrayList<String> todoList = null;
	Lock lock = null;

	String[] httpHeader = null;
	String getTarget = null;
	String parameter = null;

	byte[] headerBytes = null;
	byte[] fileBytes = null;

	File file = null;
	long fileLength = 0;
	
	JsonSerializer js = null;
	ArrayList<Integer> canvasCoords = null;

	public ServerThread(Socket clientSocket, ArrayList<String> todoList, Lock lock, ArrayList<Integer> canvasCoords) {
		this.clientSocket = clientSocket;
		this.todoList = todoList;
		this.lock = lock;
		
		lock.lock();
		this.canvasCoords = canvasCoords;
		lock.unlock();
		
		bis = null;
		oStream = null;
		bReader = null;
		inputReader = null;
		sc = null;

		httpHeader = null;
		getTarget = null;
		parameter = null;

		headerBytes = null;
		fileBytes = null;

		file = null;
		fileLength = 0;
		
		
	}

	public void addTodo(String todo) {	//zur Todo hinzufügen
		lock.lock();
		try {
			todoList.add(todo);
		}
		finally {
			lock.unlock();
		}
	}

	public ArrayList<String> getTodoList() {	//Todo abrufen
		ArrayList<String> r = null;
		lock.lock();
		try {
			r = this.todoList;
		}
		finally {
			lock.unlock();
		}
		return r;
	}

	@Override
	public void run() {
		try {
			//Input einlesen und auswerten
			oStream = clientSocket.getOutputStream();
			inputReader = new InputStreamReader(clientSocket.getInputStream());

			httpHeader = new String[20];
			char[] in = new char[5000];
			String all = "";
			
			if (inputReader.ready()) {
				
				inputReader.read(in);

				for (int i = 0; i < in.length; i++) {	//Stream to string
					all = all + in[i];
				}
				sc = new Scanner(all);
				sc.useDelimiter("\n");

				for (int i = 0; i < httpHeader.length; i++) {	//String to String-Array
					if (sc.hasNext()) {
						httpHeader[i] = sc.next();
						httpHeader[i] = httpHeader[i].replace("\n", "").replace("\r", "");
					}
					else {
						break;
					}
				}

				if (httpHeader[0] != null) {	//letzten eintrag vom Stream trimmen
					for (int i = 1; i < httpHeader.length; i++) {
						if (httpHeader[i] == null && httpHeader[i - 1] != null) {
							httpHeader[i - 1] = httpHeader[i - 1].trim();
							break;
						}
					}
				}

				for (int i = 0; i < httpHeader.length; i++) {	//Header ausgeben
					if (httpHeader[i] != null)
						System.out.println("HTTP-Header[" + i + "]: " + httpHeader[i]);
				}
				
			}
			
			//////

			if (httpHeader[0] != null) {
				if (httpHeader[0].substring(0, 3).equals("GET")) { // GET
					getTarget = httpHeader[0].substring(httpHeader[0].indexOf(" ") + 1, httpHeader[0].length());
					getTarget = getTarget.substring(0, getTarget.indexOf(" "));
					if (getTarget.contains("?")) {	//GET-Parameter
						parameter = getTarget.substring(getTarget.indexOf("?"));
						getTarget = getTarget.substring(0, getTarget.indexOf("?"));
						System.out.println("GET-Parameter: " + parameter);
					}
					if (getTarget.equals("/todo.html")) { //GET von todo.html
						ArrayList<String> l = this.getTodoList();

						//HTML mit dynamischer Listenlänge erstellen
						String todo = "<html><head><title>TODO-Liste</title></head><body><h1>TODO-LISTE:</h1><ol>";
						for (int i = 0; i < l.size(); i++) {
							todo = todo + "<li>" + l.get(i) + "</li>";
						}
						todo = todo + "</ol><form method=\"get\" action=\"./newtodo.html\"><input type=\"submit\" value=\"New Todo\" /></form><form method=\"get\" action=\"./index.html\">\r\n<input type=\"submit\" value=\"Back to Index\" />\r\n</form></body></html>";
						fileBytes = todo.getBytes();
						
						//Header erstellen
						httpHeader = new String[4];
						httpHeader[0] = "HTTP/1.1 200";
						httpHeader[1] = "Server: HTTPd/1.0 Date: Wed, 17 May 2017 15:32:15 CET";
						httpHeader[2] = "Content-Type: text/html";
						httpHeader[3] = "Content-Length: " + fileBytes.length;
						headerBytes = (httpHeader[0] + "\n" + httpHeader[1] + "\n" + httpHeader[2] + "\n" + httpHeader[3] + "\n\n").getBytes();

						//Senden
						System.out.print("Sending todo.html ... ");
						oStream.write(headerBytes);
						oStream.write(fileBytes, 0, fileBytes.length);
						oStream.flush();
						System.out.println("Done.");
					}
					else {	//GET aber keine todo.html
						file = new File("./src/miscellaneous" + getTarget);	//Datei raussuchen
						if (!file.isFile()) {	//Keine Datei gefunden -> Standartdatei /index.html
							System.out.println(getTarget + " not found. Selected index.html instead.");
							file = new File("./src/miscellaneous/index.html");
						}
						
						//Datei in Byte-Array einlesen
						fileBytes = new byte[(int) file.length()];
						bis = new BufferedInputStream(new FileInputStream(file));
						bis.read(fileBytes, 0, fileBytes.length);

						//Header erstellen
						httpHeader = new String[4];
						httpHeader[0] = "HTTP/1.1 200";
						httpHeader[1] = "Server: HTTPd/1.0 Date: Wed, 17 May 2017 15:32:15 CET";
						httpHeader[2] = "Content-Type: text/html";
						httpHeader[3] = "Content-Length: " + fileBytes.length;
						headerBytes = (httpHeader[0] + "\n" + httpHeader[1] + "\n" + httpHeader[2] + "\n" + httpHeader[3] + "\n\n").getBytes();

						//Senden
						System.out.print("Sending " + file.getName() + " ... ");
						oStream.write(headerBytes);
						oStream.write(fileBytes, 0, fileBytes.length);
						oStream.flush();
						System.out.println("Done.");
					}

				}
				else if (httpHeader[0].substring(0, 4).equals("POST")) { // POST
					getTarget = httpHeader[0].substring(httpHeader[0].indexOf(" ") + 1, httpHeader[0].length());
					getTarget = getTarget.substring(0, getTarget.indexOf(" "));

					//POST-Parameter auslesen
					if (httpHeader[0] != null) {
						for (int i = 1; i < httpHeader.length; i++) {
							if (httpHeader[i] == null && httpHeader[i - 1] != null) {
								parameter = httpHeader[(i - 1)];
								break;
							}
						}
					}

					//Falls neues todo enthält
					if (parameter.contains("newtodo")) {
						this.addTodo(parameter.replace("newtodo=", ""));
					}

					if(getTarget.equals("/login")) {	//LOGIN
						if(parameter.contains("username=") && parameter.contains("password=")) {
							String username = parameter.substring(parameter.indexOf("=")+1, parameter.indexOf("&"));
							parameter = parameter.replaceFirst("=", "");
							String password = parameter.substring(parameter.indexOf("=")+1);
							
							if(username.equals("admin") && password.equals("123")) {
								oStream.write("{success: true}".getBytes());
								oStream.flush();
							}
							else {
								oStream.write("{success: false}".getBytes());
								oStream.flush();
							}
						}
						return;
					}
					else if(getTarget.equals("/canvas_save"))	//CANVAS_SAVE
					{
						if(parameter.contains("coords=")) {
							String coords = parameter.substring(parameter.indexOf("=")+1);
							int x1,y1,x2,y2;
							x1=Integer.parseInt(coords.substring(coords.indexOf(":")+1, coords.indexOf(",")));
							coords = coords.substring(coords.indexOf(",")+1);
							y1=Integer.parseInt(coords.substring(coords.indexOf(":")+1, coords.indexOf(",")));
							coords = coords.substring(coords.indexOf(",")+1);
							x2=Integer.parseInt(coords.substring(coords.indexOf(":")+1, coords.indexOf(",")));
							coords = coords.substring(coords.indexOf(",")+1);
							y2=Integer.parseInt(coords.substring(coords.indexOf(":")+1, coords.indexOf("}")));

							lock.lock();
							canvasCoords.add(x1);
							canvasCoords.add(y1);
							canvasCoords.add(x2);
							canvasCoords.add(y2);
							lock.unlock();
							
							
						}
					}
					else {
						//Datei suchen
						file = new File("./src/miscellaneous" + getTarget);
						if (!file.isFile()) {
							System.out.println(getTarget + " not found. Selected index.html instead.");
							file = new File("./src/miscellaneous/index.html");
						}

						//Datei in Byte-Array einlesen
						fileBytes = new byte[(int) file.length()];
						bis = new BufferedInputStream(new FileInputStream(file));
						bis.read(fileBytes, 0, fileBytes.length);

						//Header erstellen
						httpHeader = new String[4];
						httpHeader[0] = "HTTP/1.1 200";
						httpHeader[1] = "Server: HTTPd/1.0 Date: Wed, 17 May 2017 15:32:15 CET";
						httpHeader[2] = "Content-Type: text/html";
						httpHeader[3] = "Content-Length: " + fileBytes.length;
						headerBytes = (httpHeader[0] + "\n" + httpHeader[1] + "\n" + httpHeader[2] + "\n" + httpHeader[3] + "\n\n").getBytes();

						//Senden
						System.out.print("Sending " + file.getName() + " ... ");
						oStream.write(headerBytes);
						oStream.write(fileBytes, 0, fileBytes.length);
						oStream.flush();
					}
					
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
			System.out.print("Closing connection ... ");
			if (clientSocket != null)
				try {
					clientSocket.close();
					System.out.println("Done.\n");
				}
				catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}
