package ParserRequest;

//TODO esta clase no la usa nadieee!!

public class ParserResponse {

	private boolean error;
	private boolean finished;
	private boolean readyToSend;
	private HttpMessage message;
	//TODO: cómo consumo los headers, se los elimino a la clase directamente??
	
	public ParserResponse() {
		super();
		this.setError(false);
		this.setFinished(false);
		this.setReadyToSend(false);
	}

	public boolean isError() {
		return error;
	}

	private void setError(boolean error) {
		this.error = error;
	}

	public boolean isFinished() {
		return finished;
	}

	private void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isReadyToSend() {
		return readyToSend;
	}

	private void setReadyToSend(boolean readyToSend) {
		this.readyToSend = readyToSend;
	}

	public void setMessage(HttpMessage message) {
		if (message != null) {
			this.message = message;
			if ( this.message.getHeader("Host") != null) {
				this.readyToSend = true;
			}
			setStatus();
		}
	}


	
	private void setStatus(){
		switch (this.message.state) {
		case INVALID:
			this.error = true;
			this.finished = false; //TODO: revisar, no estoy segura
		case DONE:
			this.finished = true;
		default:
			this.finished = false;
		}
	}
}
