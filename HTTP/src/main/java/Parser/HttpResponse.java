package Parser;

public class HttpResponse {

	private String version;
	private int statusCode;
	private String statusResponse;
	
	public HttpResponse(){
		
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
}
