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
	
	
//	public ReadingState sendData(ByteBuffer buf){
	public ParserResponse sendData(ByteBuffer buf) {
		ParserResponse resp = new ParserResponse();
		//TODO chequear que quede el buffer bien
		ByteBuffer preparedBuffer = ByteBuffer.allocate(buf.capacity());
		//buf.flip();
		preparedBuffer.put(buf);
		//buf.compact();
		state = message.parser(buf);
		resp.setMessage(message);
		return resp;
//		return state;
	}
	
	public static void main(String[] args) {
		HttpParser parser = new HttpParser();
		String req= "GET / HTTP/1.1\nHost: www.google.com\n\n";
		ByteBuffer buf = (ByteBuffer) ByteBuffer.allocate(1024);
		buf.put(req.getBytes());
		parser.sendData(buf);
	}

}
