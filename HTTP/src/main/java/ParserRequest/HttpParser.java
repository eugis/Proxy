package ParserRequest;

import java.nio.ByteBuffer;

public class HttpParser {

	private ReadingState state;
	private HttpMessage message;
	
	public HttpParser() {
		message = new HttpMessage();
	}
	
	public ReadingState getState() {
		return state;
	}
	
	
	public ReadingState sendData(ByteBuffer buf){
//	public ParserResponse sendData(ByteBuffer buf) {
//		ParserResponse resp = new ParserResponse();
		//TODO chequear que quede el buffer bien
		ByteBuffer preparedBuffer = ByteBuffer.allocate(buf.capacity());
		//buf.flip();
		preparedBuffer.put(buf);
		//buf.compact();
		state = message.parser(buf);
//		resp.setMessage(message);
//		return resp;
		return state;
	}
	
//	public static void main(String[] args) {
//		HttpParser parser = new HttpParser();
//		String req= "GET / HTTP/1.1\nHost: www.google.com\n\n";
//		ByteBuffer buf = (ByteBuffer) ByteBuffer.allocate(1024);
//		buf.put(req.getBytes());
//		parser.sendData(buf);
//	}
	
	public HttpMessage getMessage() {
		return message;
	}

	public String getHost() {
		return message.getHost();
	}

	public int getPort() {
		return message.getPort();
	}

	public String getHttpResponse() {
		int sCode = 405;
		//TODO x como esta hecho el parserMethod esto viene en null
//		String version = message.getVersion();
		String version = "1.0";
		return ResponseUtils.generateHttpResponseIM(sCode, "1.0");
	}

	public boolean isFinished() {
		return message.isFinished();
	}

}
