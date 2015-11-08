package Parser;

import java.util.HashMap;
import java.util.Map;


public class HttpMessage {

	//TODO Juli llama a "getMessage" si no está completo le mando null y ella va a tener q seguir leyendo y pasandome bytes 
	
	private boolean validMessage=true;
	private String method;
	private String url;
	private Map<String, String> headers;
	private String body;
	private boolean doneReading=true;
	//TODO en la primera linea viene la version, esa version es la que usa en el response
	private String version;
	//TODO si el metodo no es valido esto va en false, y nos quiere decir que no esribimos 
	//en el servidor, y la respuesta al cliente la damos nosotros, usando 
	//ParserUtils.generateHttpResponseIM(version)
	private boolean isMethodValid;
	private String httpResponse;
	

	public HttpMessage() {
		headers = new HashMap<String, String>();
		setMethodValid(true);
	}
	
	public void setHeader(String header, String value){
		headers.put(header, value);
	}
	public String getHeader(String header){
		return headers.get(header);
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
	
	public void setMethod(String method){
		this.method = method;
	}


	public boolean isMethodValid() {
		return isMethodValid;
	}


	public void setMethodValid(boolean isMethodValid) {
		this.isMethodValid = isMethodValid;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isValidMessage() {
		return validMessage;
	}

	public void setValidMessage(boolean validMessage) {
		this.validMessage = validMessage;
	}
	
	public String getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(String httpResponse) {
		this.httpResponse = httpResponse;
	}	
	
	public boolean returnToclient(){
		return !(this.isMethodValid && this.validMessage); 
	}
	
}
