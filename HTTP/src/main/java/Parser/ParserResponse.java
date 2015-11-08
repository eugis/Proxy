package Parser;

import Parser.HttpMessage;

public class ParserResponse {

	private boolean doneReading;
	private boolean returnToClient;
	private String host;
	private String httpResponse;

	protected void populate(HttpMessage message) {
		
		this.host=message.getHeader("Host");
		//this.validMessage = message.isValidMessage();
		this.doneReading = message.isDoneReading(); 
		this.returnToClient = message.returnToclient();
		this.httpResponse = message.getHttpResponse();
		
	}
	
	public boolean isDoneReading(){
		return this.doneReading;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return 80;
	}

	public boolean returnToClient() {
		return this.returnToClient;
	}

	public String getHttpResponse() {
		return this.httpResponse;
	}
}
