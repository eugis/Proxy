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
        ProxyAttachment attch = new ProxyAttachment(ByteBuffer.allocate(bufSize), new HTTPParser());
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
            // Indicate via key that reading/writing are both of interest now.
        	ParserResponse resp = attch.getParser().sendData(buf);
        	//if(resp.isDoneReading()){
        		key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        	//}else{
        		//key.interestOps(SelectionKey.OP_READ);
        	//}
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
        
        //SocketChannel clntChan = (SocketChannel) key.channel();
        SocketChannel clntChan = SocketChannel.open();
        clntChan.connect(new InetSocketAddress("www.google.com", 80));
        
        clntChan.write(buf);
        if (!buf.hasRemaining()) { // Buffer completely written?
            // Nothing left, so no longer interested in writes
            key.interestOps(SelectionKey.OP_READ);
        }
        buf.compact(); // Make room for more data to be read in
    }
}