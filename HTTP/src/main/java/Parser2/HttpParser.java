package Parser2;

import java.nio.ByteBuffer;

public class HttpParser {

	private ReadingState state;
	private HttpMessage message;
	
	public HttpParser() {
		message = new HttpMessage();
	}
	
	public ReadingState sendData(ByteBuffer buf){
		//TODO chequear que quede el buffer bien
		ByteBuffer preparedBuffer = ByteBuffer.allocate(buf.capacity());
		//buf.flip();
		preparedBuffer.put(buf);
		//buf.compact();
		state = message.parser(buf);
		return state;
	}
	
	public static void main(String[] args) {
		HttpParser parser = new HttpParser();
		String req= "GET / HTTP/1.1\nHost: www.google.com\n\n";
		ByteBuffer buf = (ByteBuffer) ByteBuffer.allocate(1024);
		buf.put(req.getBytes());
		parser.sendData(buf);
	}

}
