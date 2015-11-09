package ParserResponse;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

	private String version;
	private int statusCode;
	private String statusResponse;
	private int length;
	private boolean plainText;
	private boolean gZip;
	private boolean closeConnection;
	private StateResponse state;
	Map<String, String> headers;
	

	public HttpResponse(){
		this.closeConnection = false;
		setState(new StateResponse());
		headers = new HashMap<String, String>();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusResponse() {
		return statusResponse;
	}

	public void setStatusResponse(String body) {
		this.statusResponse = body;
	}
	
	public boolean isPlainText() {
		return plainText;
	}

	public void setPlainText(boolean plainText) {
		this.plainText = plainText;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	
	public boolean isgZip() {
		return gZip;
	}

	public void setgZip(boolean gZip) {
		this.gZip = gZip;
	}

	public void closeConnection(boolean non_persistent) {
		this.closeConnection = non_persistent;
	}
	
	public Map<String, String> getHeaders(){
		return this.headers;
	}

	public StateResponse getState() {
		return state;
	}

	public void setState(StateResponse state) {
		this.state = state;
	}
}
