package CarlyProxy;

import java.nio.ByteBuffer;

import Parser.HTTPParser;

public class ProxyAttachment {

	private ByteBuffer buffer;
	private HTTPParser parser;
	
	public ProxyAttachment(ByteBuffer buf, HTTPParser httpParser) {
		this.buffer = buf;
		this.parser = httpParser;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	public void setBuffer(ByteBuffer buffer){
		this.buffer = buffer;
	}
	
	public HTTPParser getParser(){
		return this.parser;
	}

}
