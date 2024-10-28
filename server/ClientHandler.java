//ClientHandler.java
//Author: Matthew Nicolella
//Spec: Handles Individual Client Freeing Up serverManager.java

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ClientHandler extends Thread implements Runnable {

	// declare a "general" socket
	private Socket connection;
	//declare low level and high level objects for input
	private InputStream inStream;
	private DataInputStream inDataStream;

	// declare low level and high level objects for output
	private OutputStream outStream;
	private DataOutputStream outDataStream;

	// Other Variables For Passing Information
	private String unparsed;
	private boolean updated;
	private String clientIP;

	public ClientHandler(Socket connection) {

		this.connection 	= connection;
		updated 			= false;

		try {

			// create an input stream from the client
			inStream 		= connection.getInputStream();
			inDataStream 	= new DataInputStream(inStream);

			// create an output stream from the client
			outStream 		= connection.getOutputStream();
			outDataStream 	= new DataOutputStream(outStream);

			clientIP		= connection.getInetAddress().toString();

			if(!inDataStream.readUTF().equals("Secret Password!")) {
				System.out.println(clientIP + " Used A Bad Key");
				dump();
			}

			System.out.println("Connection esablished with new client: " + clientIP + "\n");
		} catch (Exception e) {e.printStackTrace();}
	}
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {

				unparsed = inDataStream.readUTF();
				updated = true;

			}
		} catch(Exception e) {updated = true; unparsed = "closed";}
	}
	public void dump() {
		try {
			connection.close();

			inStream.close();
			inDataStream.close();

			outStream.close();
			outDataStream.close();

			System.out.println("Client " + clientIP + " Dumped");
		} catch(Exception e) {System.out.println("Client " + clientIP + " Failed To Dump");}
	}
	public boolean updated() {
		return updated;
	}
	public String getStream() {
		updated = false;
		return unparsed;
	}
	public String getID() {
		return clientIP;
	}
	public void sendMsg(String msg) {
		try {
			outDataStream.writeUTF(msg);
		} catch(Exception e) {System.out.println("Faild To Send Message To" + connection.getInetAddress());}
	}
}