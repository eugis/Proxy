package ParserRequest;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import Logs.CarlyLogger;

public class HttpMessage {
	
	protected StateHttp state;
	
	private Logger logs = CarlyLogger.getCarlyLogger();
	
	private Map<String, String> headers;
	//TODO supuestamente sale del host
	private int port;
	private String version;
	//TODO creo q aca habria q ir guardando el buffer por si es mas largo y viene partido
	private ByteBuffer buffer;
	
	//TODO aca guardo hasta que posicion leyo del buffer, cada vez que lo lees tiene q estar en 0
	//private int posRead;
	private String method;
	
	public HttpMessage() {
		this.state = StateHttp.REQUEST_LINE;
		this.headers = new HashMap<String, String>();
	//	this.posRead = 0;
		this.port = 80;
		this.buffer = ByteBuffer.allocate(0);
	}

	public ReadingState parser(ByteBuffer buf) {
		state = state.process(buf, this);
		switch(state){
		case INVALID:
			return ReadingState.ERROR;
		case DONE:
			return ReadingState.FINISHED;
		default:
			return ReadingState.UNFINISHED;
		}
	}

//	public int getPosRead() {
//		return posRead;
//	}
//
//	public void setPosRead(int pos) {
//		this.posRead = pos;	
//	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean addHeader(String header, String value){
		if(ParserUtils.validHeader(header)){
			if(header.equals("host")){
				int index = value.indexOf(":");
				if(index > 0){
					port = Integer.parseInt(value.substring(index + 1, value.length()));
					value = value.substring(0,index);
				}
			}
			headers.put(header, value);
			return true;
		}
		return false;
	}

	public String getHeader(String string) {
		return headers.get(string);
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getHost() {
		return headers.get("host");
	}

	public boolean bodyEnable() {
		return headers.containsKey("content-length");
	}

}
