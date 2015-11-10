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
	protected ByteBuffer buffer;
	
	private String method;
	private boolean crFlag;
	private boolean lfFlag;
	
	private boolean noHost;
	private boolean headerFinished;
	
	public HttpMessage() {
		this.state = StateHttp.REQUEST_LINE;
		this.headers = new HashMap<String, String>();
		this.port = 80;
		this.buffer = ByteBuffer.allocate(0);
		this.crFlag = false;
		this.lfFlag = false;
		this.noHost = true;
		this.headerFinished = false;
	}

	public ReadingState parser(ByteBuffer buf) {
		//Estoy guardando en buffer todo lo q entra
		ParserUtils.concatBuffer(buf, this);
		state = state.process(buf, this);
		switch(state){
		case INVALIDMETHOD:
			return ReadingState.ERROR;
		case DONE:
			if(noHost){
				return ReadingState.ERROR;
			}
			return ReadingState.FINISHED;
		default:
			return ReadingState.UNFINISHED;
		}
	}

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
				noHost = false;
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
	
	public String getMethod() {
		return method;
	}

	public String getHost() {
		return headers.get("Host");
	}

	public boolean bodyEnable() {
		return headers.containsKey("content-length");
	}

	public void setcrFlag(boolean cr) {
		crFlag = cr;
		
	}

	public void setlfFlag(boolean lf) {
		lfFlag = lf;	
	}
	
	public boolean isCrFlag() {
		return crFlag;
	}

	public boolean isFinished() {
		//TODO esto no se puede probar en consola, para probarlo hay q setear
		return crFlag && lfFlag;
	}

	public void setFinished() {
		crFlag = true;
		lfFlag = true;	
	}

	public void cleanBuffer() {
		buffer = ByteBuffer.allocate(0);
	}

	public void setNoHost(boolean value) {
		this.noHost = value;		
	}
	
	public boolean isNoHost() {
		return noHost;
	}

	public boolean isInvalidMethod() {
		return state.equals(StateHttp.INVALIDMETHOD);
	}

	public boolean isInvalidHeader() {
		//TODO aca tendria que agregar si no viene el content-lenght y es necesario
		if(noHost){
			return true;
		}
		return false;
	}

	public void setHeaderFinished(boolean value) {
		this.headerFinished = value;
		
	}
	
	public boolean isHeaderFinished() {
		return headerFinished;
	}

}
