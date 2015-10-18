package CarlyProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import Parser.HTTPParser;

public class ClientTCPHandler implements TCPProtocol{
	
	private int bufSize; // Size of I/O buffer

	private HashMap<SocketChannel, SocketChannel> openChannels = new HashMap<SocketChannel, SocketChannel>();
	
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
        	//busco si tenia un canal asociado
        	SocketChannel openChannel = this.openChannels.get(clntChan); 
        	if(openChannel != null){
        		this.openChannels.remove(clntChan);
        		openChannel.close();
        	}
            clntChan.close();
        } else if (bytesRead > 0) {
        	if(attch.getClientChannel() != null){ //SI LA CONEXION VIENE DEL "SERVER"
            	attch.getClientChannel().register(key.selector(), SelectionKey.OP_WRITE, new ProxyAttachment(null, buf, new HTTPParser()));
            }else{
	            
            	SocketChannel hostChan = null;
            	ParserResponse resp = attch.getParser().sendData(buf);
            	
            	//busco si ya existe este canal (porque no termino de leer)
            	SocketChannel openChannel = this.openChannels.get(clntChan); 
	        	if(openChannel == null){ //sino, lo creo
		        	hostChan = SocketChannel.open();
		        	hostChan.configureBlocking(false);
		        	hostChan.connect(new InetSocketAddress(resp.getHost(), resp.getPort()));
		        	while(!hostChan.finishConnect()){}
	        	}else{
	        		hostChan = this.openChannels.get(clntChan);
	        	}
	        	
	        	//al attch le agregue con que "cliente" habla
	        	ProxyAttachment hostAttch = new ProxyAttachment(clntChan, buf, new HTTPParser());
        		hostChan.register(key.selector(), SelectionKey.OP_WRITE, hostAttch);
        		
        		if(resp.isDoneReading()){ //si no hay mas nada para leer
        			
	        	}else{ //si no termino de leer
	        		if(openChannel == null){ //y el canal abierto es nuevo (o sea, primer leida)
	        			this.openChannels.put(clntChan, hostChan);
	        		}
	        		key.interestOps(SelectionKey.OP_READ); //sigo leyendo
	        	}
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