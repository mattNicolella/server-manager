//clientGameServer.java
//Author: Matthew Nicolella
//Spec: Holds data for easy access from serverManagerClient

public class clientGameServer {

	private String name, status, uptime;

	public clientGameServer(String name, String status) {
		this.name 	= name;
		this.status = status;
		this.uptime = "";
	}
	public clientGameServer(String name, boolean base) {
		this.name 	= name;
		this.status = "Unknown";
		this.uptime = "";

		System.out.println("New Server Created: " + name);
	}
	public clientGameServer(String unparsed) {
		String[] split = unparsed.split(" ");

		this.name 	= split[0];
		this.status = split[1];
		this.uptime = "";
	}
	public String getName() {
		return name;
	}
	public String getStatus() {
		return status;
	}
	public String getUptime() {
		return uptime;
	}
	public void setName(String name) {
		this.name 	= name;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
}