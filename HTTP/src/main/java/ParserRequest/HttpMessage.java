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

	private boolean noContentLength;
	private StringBuilder lastLine;
	
	protected int pos;
	private int pos_initBody;
	
	public HttpMessage() {
		this.state = StateHttp.REQUEST_LINE;
		this.headers = new HashMap<String, String>();
		this.port = 80;
		this.buffer = ByteBuffer.allocate(2048);
		this.crFlag = false;
		this.lfFlag = false;
		this.noHost = true;
		this.noContentLength = true;
		this.headerFinished = false;
		pos = 0;
		pos_initBody = -1;
		lastLine = new StringBuilder();
	}

	public ReadingState parser(ByteBuffer buf) {
		//Estoy guardando en buffer todo lo q entra
//		ParserUtils.concatBuffer(buf, this);

		state = state.process(buf, this);
		switch(state){
		case INVALIDMETHOD:
			return ReadingState.ERROR;
		case DONE:

			if(noHost || (isNoContentLength() && method.equals("POST"))){

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
			if(header.equals("Host")){
				int index = value.indexOf(":");
				if(index > 0){
					port = Integer.parseInt(value.substring(index + 1, value.length()));
					value = value.substring(0,index);
				}
				noHost = false;
			}else if(header.equals("Content-Length")){
				noContentLength = false;
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
		return headers.containsKey("Content-Length");
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
	
	public boolean isLfFlag() {
		return lfFlag;
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
	
	public boolean isNoContentLength() {
		return noContentLength;
	}

	public boolean isInvalidMethod() {
		return state.equals(StateHttp.INVALIDMETHOD);
	}

	public boolean isInvalidHeader() {
		//TODO aca tendria que agregar si no viene el content-lenght y es necesario
		if(noHost || (method!=null && method.equals("POST") && noContentLength)){
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


	public void setNoContentLength(boolean value) {
			this.noContentLength = value;
	}		

	public void closeRequest() {
		String fin = "\r\n";
		ByteBuffer aux = ByteBuffer.allocate(buffer.position() +
			fin.length());
		buffer.flip();
		aux.put(buffer);
		aux.put(fin.getBytes());
		buffer = aux;

	}

	public void setLastLine(StringBuilder genLine) {
		this.lastLine = genLine;	
	}

	public StringBuilder getLastLine() {
		return lastLine;
	}

	public void reset() {
		this.state = StateHttp.REQUEST_LINE;
		this.headers = new HashMap<String, String>();
		this.port = 80;
		this.buffer = ByteBuffer.allocate(2048);
		this.crFlag = false;
		this.lfFlag = false;
		this.noHost = true;
		this.noContentLength = true;
		this.headerFinished = false;
		pos = 0;
		if(lastLine == null){
			lastLine = new StringBuilder();
		}
	}

	public boolean initBody() {
		return pos_initBody != -1;
	}

	public void setInitBody(int init) {
		this.pos_initBody = init;	
	}

	public Integer getReadBody() {
		return pos - pos_initBody;
	}
}
