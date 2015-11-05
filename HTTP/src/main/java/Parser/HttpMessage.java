package Parser;

import java.util.HashMap;
import java.util.Map;


public class HttpMessage {

	private String method;
	private boolean hasMethod=false;
	private String host;
	private String body;
	private boolean doneReading;
	
	//TODO guardaria todos los headres.. key: header, value: valor. Host iria aca adentro
	private Map<String, String> headers;
	//TODO en la primera linea viene la version, esa version es la que usa en el response
	private String version;
	//TODO si el metodo no es valido esto va en false, y nos quiere decir que no esribimos 
	//en el servidor, y la respuesta al cliente la damos nosotros, usando 
	//ParserUtils.generateHttpResponseIM(version)
	private boolean isMethodValid;
	
	public HttpMessage() {
		headers = new HashMap<String, String>();
		isMethodValid = true;
	}
		
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
