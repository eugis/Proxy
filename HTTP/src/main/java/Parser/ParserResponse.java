package Parser;

import Parser.HttpMessage;

public class ParserResponse {

	private boolean doneReading;
	private boolean availableToSend;
	private boolean completeRead;
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

	//TODO: estos métodos hay que unirlos bien con el parser.
	public boolean isAvailableToSend() {
		return availableToSend;
	}

	public void setAvailableToSend(boolean availableToSend) {
		this.availableToSend = availableToSend;
	}

	public boolean isCompleteRead() {
		return completeRead;
	}

	public void setCompleteRead(boolean isCompleteRead) {
		this.completeRead = isCompleteRead;
	}
}
