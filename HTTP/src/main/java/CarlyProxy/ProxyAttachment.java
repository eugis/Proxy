package CarlyProxy;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import Parser.HTTPParser;

public class ProxyAttachment {

	private SocketChannel client;
	private ByteBuffer buffer;
	private HTTPParser parser;
	
	public ProxyAttachment(SocketChannel cChan, ByteBuffer buf, HTTPParser httpParser) {
		this.client = cChan;
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

	public SocketChannel getClientChannel() {
		return this.client;
	}

}
