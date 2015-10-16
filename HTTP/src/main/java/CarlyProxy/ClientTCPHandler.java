package CarlyProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import Parser.HTTPParser;

public class ClientTCPHandler implements TCPProtocol{
	
	private int bufSize; // Size of I/O buffer

    public ClientTCPHandler(int bufSize) {
        this.bufSize = bufSize;
    }

    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false); // Must be nonblocking to register
        // Register the selector with new channel for read and attach byte
        // buffer
        ProxyAttachment attch = new ProxyAttachment(null, ByteBuffer.allocate(bufSize), new HTTPParser());
        clntChan.register(key.selector(), SelectionKey.OP_READ, attch);
    }

    public void handleRead(SelectionKey key) throws IOException {
        // Client socket channel has pending data
        SocketChannel clntChan = (SocketChannel) key.channel();
        ProxyAttachment attch = (ProxyAttachment) key.attachment();
        //ByteBuffer buf = (ByteBuffer) key.attachment();
        
        ByteBuffer buf = (ByteBuffer) attch.getBuffer();
        long bytesRead = clntChan.read(buf);
        if (bytesRead == -1) { // Did the other end close?
            clntChan.close();
        } else if (bytesRead > 0) {
        	//SI LA CONEXION VIENE DEL "SERVER"
        	if(clntChan.getRemoteAddress().toString().startsWith("www.google.com")){
        		//al attch le agregue con que "cliente" habla
            	attch.getClientChannel().register(key.selector(), SelectionKey.OP_WRITE, new ProxyAttachment(clntChan, buf, new HTTPParser()));
            }else{
	            // Indicate via key that reading/writing are both of interest now.
	        	ParserResponse resp = attch.getParser().sendData(buf);
	        	//if(resp.isDoneReading()){
		        	SocketChannel hostChan = SocketChannel.open();
		        	hostChan.configureBlocking(false);
		        	hostChan.connect(new InetSocketAddress("www.google.com", 80));
		        	while(!hostChan.finishConnect()){}
		        	ProxyAttachment hostAttch = new ProxyAttachment(clntChan, buf, new HTTPParser());
		        	hostChan.register(key.selector(), SelectionKey.OP_WRITE, hostAttch);
	        		//key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	        	//}else{
	        		//key.interestOps(SelectionKey.OP_READ);
	        	//}
            }
        }
    }

    public void handleWrite(SelectionKey key) throws IOException {
        /*
         * Channel is available for writing, and key is valid (i.e., client
         * channel not closed).
         */
        // Retrieve data read earlier
    	ProxyAttachment attch = (ProxyAttachment) key.attachment();
    	//ByteBuffer buf = (ByteBuffer) key.attachment();
    	ByteBuffer buf = attch.getBuffer();
        buf.flip(); // Prepare buffer for writing
        
        SocketChannel clntChan = (SocketChannel) key.channel();
        clntChan.write(buf);
        if (!buf.hasRemaining()) { // Buffer completely written?
            // Nothing left, so no longer interested in writes
            key.interestOps(SelectionKey.OP_READ);
        }
        buf.compact(); // Make room for more data to be read in
    }
}