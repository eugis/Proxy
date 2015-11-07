package Parser;

import Parser.HttpMessage;

public class ParserResponse {

	private boolean doneReading;
	private boolean validMessage;
	private String host;
		
	public boolean isDoneReading(){
		return this.doneReading;
	}
	
	public boolean isValidMessage(){
		return validMessage;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return 80;
	}

	protected void populate(HttpMessage message) {
		
		this.host=message.getHeader("Host");
		this.validMessage = message.isValidMessage();
		this.doneReading = message.isDoneReading(); 
		
		
	}
}
