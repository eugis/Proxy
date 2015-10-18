package CarlyProxy;

import java.net.InetAddress;

public class ParserResponse {

	private boolean doneReading = true;
	
	public boolean isDoneReading(){
		return this.doneReading;
	}

	public String getHost() {
		return "www.google.com";
	}

	public int getPort() {
		return 80;
	}
}
