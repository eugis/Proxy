package ParserRequest;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import Logs.CarlyLogger;

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
		ByteBuffer preparedBuffer = ByteBuffer.allocate(buf.capacity());
		//buf.flip();
		preparedBuffer.put(buf);
		//buf.compact();
		state = message.parser(buf);
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

	public boolean isFinished() {
		return message.isFinished();
	}

	public byte[] getRequest() {
		byte[] request = new byte[message.pos];
		message.buffer.get(request, 0, message.pos);
		
		System.out.println("Long request: "+ request.length);
		System.out.println(new String(request));
		
		message.reset();
		
		return request;
		
	}

	public void cleanRequest() {
		message.cleanBuffer();		
	}

	public String getHttpResponse(int sCode) {
		String version = "1.1";
		return ResponseUtils.generateHttpResponseIM(sCode, version, message);
	}
	
	public String getHttpResponse() {
		int sCode = 0;
		if(message.isInvalidMethod()){
			sCode = 405;
		}else if(message.isInvalidHeader()){
			sCode = 400;	
		}
		
//		String version = "1.1";
//		return ResponseUtils.generateHttpResponseIM(sCode, version, message);
		
		return getHttpResponse(sCode);
	}

}
