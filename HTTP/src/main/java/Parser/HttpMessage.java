package Parser;


public class HttpMessage {

	private String method;
	private boolean hasMethod=false;
	private String host;
	private String body;
	private boolean doneReading;
		
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public boolean isDoneReading() {
		return doneReading;
	}
	public void setDoneReading(boolean doneReading) {
		this.doneReading = doneReading;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	
	public boolean hasMethod(){
		return hasMethod;
	}
	public void methodDone(){
		this.hasMethod = true;
	}
	
	
	
}
