package Parser;

import java.util.HashMap;
import java.util.Map;


public class HttpMessage {

	//TODO Juli llama a "getMessage" si no está completo le mando null y ella va a tener q seguir leyendo y pasandome bytes 
	
	private String method;
	private Map<String, String> headers;
	private String body;
	private boolean doneReading;
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
	
		
	
}
