package ParserResponse;

import java.nio.ByteBuffer;
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
	//private byte[] buf;
	//private boolean hasRead;

	public HttpResponse(){
		this.closeConnection = false;
		setState(new StateResponse());
		headers = new HashMap<String, String>();
		//this.hasRead = false;
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

	public boolean isResponseFinished() {
		if (state.onMethod() > 2) {
			if (headers.get("Content-Length") != null) {
				return state.getIsFinished();	
			} else {
				return true;
			}	
		} 
		return false;
	}

	/*public byte[] getBuf() {
		return buf;
	}

	public void setBuf(byte[] buf) {
		this.buf = buf;
	}
	
	public int getBufLength() {
		return buf.length;
	}

	public boolean hasRead() {
		return hasRead;
	}

	public void setReaded(boolean hasRead) {
		this.hasRead = hasRead;
	}*/
}
