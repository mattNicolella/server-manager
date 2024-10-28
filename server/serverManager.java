//serverManager.java
//Author: Matthew Nicolella
//Spec: Main server manager file, controls initial connecting to clients and server management

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class serverManager {

	 private ServerSocket lisSocket;
	 private String tmp, tmpMsg;
	 private ArrayList<ClientHandler> clients;
	 private ArrayList<Application> a;
	 private String[] servers, serverPaths, serverArgs;
	 private Socket tmpConnect;

	 public static void main(String [] args) {
	 	   new serverManager();
   	}

     public serverManager() {
      try {
         // declare other variables
		 int port 			= 7777;
		 a 					= new ArrayList<Application>();
		 clients 			= new ArrayList<ClientHandler>();
		 servers 			= new String[]{"ark", 		  "gmod", 								"arma",			"SpaceEngineers", "minecraft", 		"OpenTTD"};
		 serverPaths 		= new String[]{"notepad.exe", "D:/Servers/SteamCMD/ttt2/srcds.exe", "notepad.exe", 	"notepad.exe", 	  "notepad.exe", 	"openttd-12.1-linux-ubuntu-focal-amd64.deb"};
		 serverArgs			= new String[]{"", 			  "", 									"", 			"", 			  "",				"-D"};

		 // create a server socket
		 lisSocket			= new ServerSocket(port, 1, InetAddress.getLocalHost());

		 System.out.println("Server open at: "+ InetAddress.getLocalHost() + ":" + port +"\n");

		 Thread handshake = new Thread(() -> {
			 try {
				 while(true) {

					 tmpConnect = lisSocket.accept();

					 //listen for a connection from the client
					 clients.add(new ClientHandler(tmpConnect));
					 clients.get(clients.size()-1).start();

					 tmpConnect = null;
				 }
			} catch(Exception e) {e.printStackTrace();}
		});

		Thread handler = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted())
					for(int i = 0; i < clients.size(); i++)
						if(clients.get(i).updated()) {
							parse(clients.get(i).getStream(), i);
						}
					}
		});

		handshake.start();
		System.out.println("Server Socket Ready");

		handler.start();
		System.out.println("Client Handler Ready");

		System.out.println("All Systems Go!");

      } catch (Exception ex) {ex.printStackTrace();}
   }
   public void parse(String unparsed, int clientID) {
	   if(unparsed.equalsIgnoreCase("closed")) {
		   System.out.println("Removing Client: " + clients.get(clientID).getID());
		   clients.remove(clientID);
		   return;
	   }
	   switch(Integer.parseInt(unparsed.substring(0, 1))) {
		   case 0: startServer(unparsed); break; 			//Start Server
		   case 1: endServer(unparsed); break; 				//End Server
		   case 2: updateClientData(clientID); break; 		//Update Client
		   case 3: initialData(clientID); break;			//Gives Client Inital Data To Correctly Configure
	   }

   }
   //Update The Client On Active Servers
   public void updateClientData(int clientID) {
	   tmp = "";

	   for(int i = 0; i < a.size(); i++)
			tmp +=a.get(i).getName() + " " + a.get(i).uptime() + ",";

	   clients.get(clientID).sendMsg(tmp);
   }
   //Updates All Clients Due To A Change In Status
   public void updateAll() {
    for(int i = 0; i < clients.size(); i++)
		updateClientData(i);
   }
   //Start Server By Name
   public void startServer(String unparsed) {
	   try {
		   for(int i = 0; i < servers.length; i++) {
			   if(unparsed.indexOf(servers[i]) > -1)
					a.add(new Application(serverPaths[i], servers[i], serverArgs[i]));
		   }
		   updateAll();
	   } catch(Exception e) {System.out.println("Error Starting Server, Check Path Or Name.");}
   }
   //Ends Server By Name
   public void endServer(String unparsed) {
	   for(int i = 0; i < a.size(); i++) {
	   		   if(unparsed.indexOf(a.get(i).getName()) > -1) {
				   a.get(i).kill();
				   a.remove(i);
			   }
	   }
	   updateAll();
   }
   //Sends Client All Servers And Status's Initally To Build Gui
   public void initialData(int clientID) {
	   String[] appNames = new String[servers.length];
	   boolean[] serverStatus = new boolean[servers.length];
	   tmpMsg = "";

	   if(a.size() > 0) {

		   for(int i = 0; i < a.size(); i++)
		   	appNames[i] = a.get(i).getName();

		   for(int i = 0; i < servers.length; i++)
				if(Arrays.toString(appNames).indexOf(servers[i]) > -1)
					serverStatus[i]=true;
				else if(Arrays.toString(appNames).indexOf(servers[i]) <= -1)
					serverStatus[i]=false;

			for(int i = 0; i <servers.length; i++) {
				if(i != servers.length-1) {
					if(serverStatus[i])
						tmpMsg+=servers[i] + " On,";
					else
						tmpMsg+=servers[i] + " Off,";
				} else {
					if(serverStatus[i])
						tmpMsg+=servers[i] + " On";
					else
						tmpMsg+=servers[i] + " Off";
				}
			}
		} else
	   		for(int i = 0; i < servers.length; i++)
	   			if(i != servers.length-1)
	   				tmpMsg+=servers[i] + " Off,";
	   			else
	   				tmpMsg+=servers[i] + " Off";

	   	clients.get(clientID).sendMsg(tmpMsg);
   }
}