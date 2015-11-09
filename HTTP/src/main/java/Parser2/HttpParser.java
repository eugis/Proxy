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
		buf.flip();
		preparedBuffer.put(preparedBuffer);
		buf.compact();
		state = message.parser(buf);
		return state;
	}

}
