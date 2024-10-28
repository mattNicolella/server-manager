//Aplication.java
//Author: Matthew Nicolella
//Spec: Application Handler For Server Manager

public class Application {
    private long startTime;
    private Process p;
    private String name;

    public Application(String path, String name, String args) {
		try {

			startTime   = System.currentTimeMillis();
			this.name 	= name;
			p 			= new ProcessBuilder(path, args).start();

		} catch (Exception e) {e.printStackTrace();}
    }
    public void kill() {
        p.destroy();
    }
    public String getName() {
		return name;
	}
    public String uptime() {
		long time 		= System.currentTimeMillis()-startTime;
		if(time / 86400000 > 0)
			return time / 86400000 + " Day(s)";
		else if(time / 3600000 > 0)
			return time / 3600000 + " Hour(s)";
		else if(time / 60000 > 0)
			return time / 60000 + " Minute(s)";
		else
			return time / 1000 + " Second(s)";
    }
}