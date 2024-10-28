// serverManagerCleint.java
// Author: Matthew Nicolella
// Spec: A client to start and stop remote server files

import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class serverManagerClient {

	// declare UI objects
	private JButton[] buttons;
	private JLabel[] status, curStatus, uptime;
	private JPanel[] panels;
	private JPanel[] grids;
	private JPanel pnlUpdate, pnlButtons;
	private JButton btnUpdate;
	private JFrame frame;
	private JTextArea jtaLog;
	private JScrollPane scroll;
	private JLabel lblUpdated, lblUpdatedInfo;
	private GridBagConstraints constraints;
	private ButtonListener listener;

	// declare Server Objects
	private clientGameServer[] servers;

	// declare a "general" socket
	private Socket connection;
	//declare low level and high level objects for input
	private InputStream inStream;
	private DataInputStream inDataStream;

	// declare low level and high level objects for output
	private OutputStream outStream;
	private DataOutputStream outDataStream;

	// declare other needed objects
	private DateTimeFormatter dtf;
	private String[] initialData, updateData;
	private String tmp;
	private String serverIP;
	private int port;
	private String key;

	public static void main(String [] args) {
		try {
			new serverManagerClient("192.168.0.26", 7777);
		} catch(Exception e){e.printStackTrace();}
	}

	public serverManagerClient(String serverIP, int port) {

		// intialize other variables for later use
		tmp = "";
		dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

		try {
			// set IP and Port
			this.serverIP 	= serverIP;
			this.port 		= port;
			this.key		= "Secret Password!";

			if(loadConfig())
				System.out.println("Config loaded.");
			else
				System.out.println("Config failed to load.");

			// configures socket
			if(serverIP.equals("localhost")) {
				System.out.println("Attempting localhost");
				connection 	= new Socket(InetAddress.getLocalHost(), port);
			} else {
				System.out.println("Attempting loaded config.");
				connection 	= new Socket(this.serverIP, this.port);
			}

			// create an input stream from the client
			inStream 		= connection.getInputStream();
			inDataStream 	= new DataInputStream(inStream);

			// create an output stream from the client
			outStream 		= connection.getOutputStream();
			outDataStream 	= new DataOutputStream(outStream);

			System.out.println("\nConnection esablished with server at: " + connection.getInetAddress());

			// key for additional "security"
			outDataStream.writeUTF("Secret Password!");

			// tell server to initially update
			outDataStream.writeUTF("3");

			// initializes Server Objects
			updateInitial(inDataStream.readUTF());

			// thread To Monitor Incoming Messages
			Thread handler  = new Thread(new Runnable() {
				public void run() {
					while (!Thread.currentThread().isInterrupted())
						try {
							tmp = inDataStream.readUTF();
							update(tmp);
						} catch(SocketException e){
							System.out.println("Connection to server lost");
							jtaLog.append(dtf.format(LocalDateTime.now()) + ":Connection to server lost.\n");
							break;

						} catch(Exception e) {System.out.println("Failed To Obtain Message From Server\n" + e.toString());}
				}
			});

			handler.start();

		} catch (Exception e) {e.printStackTrace();}

		// UI configuration
		frame			   = new JFrame();
		frame.setSize(600, (((servers.length/3)+(servers.length%3))*100)+200);
		frame.setLayout(new GridBagLayout());

		// initialize all UI elements
		listener		   = new ButtonListener();
		buttons			   = new JButton[servers.length];
		status			   = new JLabel[servers.length];
		curStatus		   = new JLabel[servers.length];
		uptime			   = new JLabel[servers.length];
		panels 			   = new JPanel[servers.length];
		grids			   = new JPanel[servers.length];
		pnlUpdate		   = new JPanel();
		pnlButtons		   = new JPanel();
		btnUpdate		   = new JButton("Update");
		jtaLog			   = new JTextArea();
		scroll			   = new JScrollPane(jtaLog);
		lblUpdated		   = new JLabel("");
		lblUpdatedInfo     = new JLabel("Last Updated: ");
		constraints		   = new GridBagConstraints();

		btnUpdate.addActionListener(listener);

		pnlUpdate.setLayout(new GridLayout(1, 3));

		pnlUpdate.add(lblUpdatedInfo);
		pnlUpdate.add(lblUpdated);
		pnlUpdate.add(btnUpdate);

		pnlButtons.setLayout(new GridLayout((servers.length/3)+(servers.length%3), 3));

		for(int i = 0; i < servers.length; i++) {
			buttons[i] 	   = new JButton(servers[i].getName());
			status[i] 	   = new JLabel("Status:");
			curStatus[i]   = new JLabel("Unknown");
			uptime[i]	   = new JLabel("Uptime Placeholder");
			panels[i]      = new JPanel();
			grids[i]	   = new JPanel();

			buttons[i].addActionListener(listener);

			panels[i].setLayout(new GridLayout(3, 1));
			grids[i].setLayout(new GridLayout(1, 2));

			grids[i].add(status[i]);
			grids[i].add(curStatus[i]);

			panels[i].add(buttons[i]);
			panels[i].add(grids[i]);
			panels[i].add(uptime[i]);

			pnlButtons.add(panels[i]);

		}

		// setup UI layout
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = .1;
		constraints.insets = new Insets(3,3,3,3);

		frame.add(pnlUpdate, constraints);

		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 2.0;

		frame.add(pnlButtons, constraints);

		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weightx = 1.0;
		constraints.weighty = 2.0;

		frame.add(scroll, constraints);

		jtaLog.setLineWrap(true);
		jtaLog.setWrapStyleWord(true);
		scroll.setPreferredSize(new Dimension(480, 500));

		jtaLog.setEditable(false);
		jtaLog.append(dtf.format(LocalDateTime.now()) + ": Connection esablished with server at: " + connection.getInetAddress() + "\n");
		jtaLog.append("Client Initialized.\n");
    	scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// UI final preperations
		sendUpdate();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
	// sends update code to server
	private void sendUpdate() {
		try{
			outDataStream.writeUTF("2");
		}catch(Exception ex){
			System.out.println("Failed To Send Update Code.");
		}
	}

	//Finds Server By Name, Then Sets Status
	private void update(String unparsed) {
		try {
			updateData = unparsed.split(",");
			for(int i = 0; i < updateData.length; i++)
				if(updateData[i].split(" ").length == 3)
					for(int j = 0; j < servers.length; j++)
						if(updateData[i].split(" ")[0].indexOf(servers[j].getName()) > -1) {
							servers[j].setStatus("On");
							servers[j].setUptime(updateData[i].split(" ")[1] + " " + updateData[i].split(" ")[2]);
						}

			correctOff(unparsed);
		}catch(Exception e){System.out.println("Update Method Broke");}
		lblUpdated.setText(dtf.format(LocalDateTime.now()));
		jtaLog.append(dtf.format(LocalDateTime.now()) + ": Client Updated.\n");
		updateGui();
	}
	// checks which servers are currently off based on lack of information from serverManager
	private void correctOff(String unparsed) {
		boolean[] stat = new boolean[servers.length];
		String[] parsed = new String[unparsed.split(",").length];

		try {
			tmp = "";

			// adds all server names to a string
			for(int i = 0; i < servers.length; i++)
				if(i != servers.length-1)
					tmp += servers[i].getName() + ",";
				else
					tmp += servers[i].getName();

			// cleans unparsed, removes all commas and spaces so that parsed is just an array of names
			for(int i = 0; i < unparsed.split(",").length; i++)
				parsed[i] = unparsed.split(",")[i].split(" ")[0];

			for(int i = 0; i < servers.length; i++) {
				stat[i] = false;
				for(int j = 0; j < parsed.length; j++)
					if(parsed[j].equalsIgnoreCase(servers[i].getName()))
						stat[i] = true;


			}

			for(int i = 0; i < servers.length; i++) {
				if(!stat[i])
					servers[i].setStatus("Off");
			}

		} catch(Exception e){System.out.println("Correction Method Broke.");}
	}
	// initially updates the client with current server data
	private void updateInitial(String unparsed) {
		servers = new clientGameServer[unparsed.split(",").length];
		for(int i = 0; i < unparsed.split(",").length; i++) {
			servers[i] = new clientGameServer(unparsed.split(",")[i]);
		}
	}
	// updates the Gui with current server status's
	private void updateGui() {
		for(int i = 0; i < servers.length; i++) {
			if(servers[i].getStatus().equalsIgnoreCase("On")) {
				curStatus[i].setText("On");
				curStatus[i].setForeground(Color.GREEN);
				uptime[i].setText("Uptime: " + servers[i].getUptime());
			} else if(servers[i].getStatus().equalsIgnoreCase("Off")) {
				curStatus[i].setText("Off");
				curStatus[i].setForeground(Color.RED);
				uptime[i].setText("");
			} else {
				curStatus[i].setText("Unknown");
				curStatus[i].setForeground(Color.BLACK);
				uptime[i].setText("Uptime: Unknown");
			}
		}

		jtaLog.setCaretPosition(jtaLog.getDocument().getLength() - 1);

	}
	public boolean loadConfig() {
		try {

				FileInputStream fis = new FileInputStream(new File("./config.dat"));
				InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
       			BufferedReader reader = new BufferedReader(isr);

				serverIP	= reader.readLine().split(":")[1];
				port	 	= Integer.parseInt(reader.readLine().split(":")[1]);
				key 		= reader.readLine().split(":")[1];

				if(!reader.readLine().equals("mental-note:Senate Is Dingus")) {
					reader.close();
					isr.close();
					fis.close();
					return false;
				}

			reader.close();
			isr.close();
			fis.close();

			return true;
		} catch(Exception e) {
			System.out.println(e.toString());
			return false;
		}
	}

	private class ButtonListener implements ActionListener {
		JButton source;
		public void actionPerformed(ActionEvent e) {

			if(e.getSource() instanceof JButton)
				source = (JButton)(e.getSource());

			for(int i = 0; i < servers.length; i++) {
				if(source.getText().equals(servers[i].getName())) {
					if(servers[i].getStatus().equalsIgnoreCase("on")) {
						try {
							outDataStream.writeUTF("1 " + servers[i].getName());
							jtaLog.append(dtf.format(LocalDateTime.now()) + ": Sending stop to server: " + "1 " + servers[i].getName() + "\n");
						} catch(Exception ex) {
								System.out.println("Failed To Send Shutdown Command");
								jtaLog.append(dtf.format(LocalDateTime.now()) + ": Failed To Send Shutdown Command\n");
							}
					} else if(servers[i].getStatus().equalsIgnoreCase("off")) {
						try {
							jtaLog.append(dtf.format(LocalDateTime.now()) + ": Sending start to server: " + "1 " + servers[i].getName() + "\n");
							outDataStream.writeUTF("0 " + servers[i].getName());
						} catch(Exception ex) {
							System.out.println("Failed To Send Start Command");
							jtaLog.append(dtf.format(LocalDateTime.now()) + ": Failed To Send Start Command\n");
						}
					} else {
						JOptionPane.showMessageDialog(null, "Cannot Broadcast Signal Without Knowing Server Status's", "Failure", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
			if(source == btnUpdate)
				sendUpdate();
		}
	}
}