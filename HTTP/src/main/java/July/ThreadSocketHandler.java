package July;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import Parser2.HttpParser;
import Parser2.ParserResponse;

public class ThreadSocketHandler implements ConnectionHandler{

	private static final int BUFSIZE = 1024;
	private HttpParser parser;
	
	@Override
	public void handle(Socket s) throws IOException {
		this.parser = new HttpParser();
		readDataFromClient(s);
	}

	private void readDataFromClient(Socket s) throws IOException{
		InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();
        
        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer
        int recvMsgSize = 0;   // Size of received message
        ParserResponse resp = null;
        //boolean keepReading = false;
        String host2connect = "";
        int port2connect = -1;
        boolean client = true;
        // Receive until client closes connection, indicated by -1 return
        Socket serverSocket = null;
        ByteBuffer bBuffer;
        while (recvMsgSize != -1 /*&& !keepReading*/) {
        	receiveBuf = new byte[BUFSIZE]; //TODO: hacer de forma elegante
        	recvMsgSize = in.read(receiveBuf);
        	bBuffer = ByteBuffer.wrap(receiveBuf);
        	if(recvMsgSize != -1){
        		//Harcoded receiveBuf
//            	String request = "GET / HTTP/1.1\n"+"Host: www.google.com\n\n";
//        		byte[] msg = request.getBytes(Charset.forName("UTF-8"));
//            	
            	String str = new String(receiveBuf, StandardCharsets.UTF_8);
            	System.out.print(str);
            	
//            	ReadingState state = parser.sendData(bBuffer);
            	resp = parser.sendData(bBuffer);
            	System.out.println("Host:" + resp.getHost());
            	System.out.println("Port:" + resp.getPort());
            	
            	//keepReading = resp.isDoneReading();
            	
            	/*System.out.println("Host: "+resp.getHost());
            	
            	if(resp.isDoneReading()){ //Debería llamarse, puedo empezar a mandar
//            	if (resp.isAvailableToSend()) {	
            		byte[] byteReq;
            		if(!resp.returnToClient()){
	            		host2connect = resp.getHost();
	                    port2connect = resp.getPort();
	                    String hardCodeResp = "GET / HTTP/1.1 \n\n";
	                    byteReq = hardCodeResp.getBytes();
	                    serverSocket = writeToServer(host2connect, port2connect, byteReq);
	                    if (resp.isCompleteRead()) {
	                    	readFromServer(serverSocket, out);	
	                    }
                    }else{
                    	byteReq = resp.getHttpResponse().getBytes();
                    	out.write(byteReq);
                  	  	out.flush();
                    }
            	}*/
        	}
        }
        System.out.println("cerrarrrrrrrrrr");
        // Close the socket.  We are done with this client!
        if (serverSocket != null) {
        	ProxyConnectionManager.closeConnection(serverSocket);	
        }
        s.close();
	}

	private Socket writeToServer(String host, int port, byte[] byteReq) throws UnknownHostException, IOException{
		ProxySocket pSocket = ProxyConnectionManager.getConnection(host, port);
		Socket serverSocket = pSocket.getSocket();
    	if(serverSocket != null){//doble validacion, podria no ir
    		System.out.println(serverSocket);
    		OutputStream outFromServer = serverSocket.getOutputStream();
    		outFromServer.write(byteReq);
    		outFromServer.flush();
    	}
    	return serverSocket;
	}
    
	//TODO: Agregar el parser de respuesta al contenido del servidor
    private void readFromServer(Socket serverSocket, OutputStream out) throws IOException{
    	byte[] responseBuf = new byte[BUFSIZE];
    	int recvMsgSize = 0;
    	ParserResponse resp = null;
    	boolean keepReading = false;
    	InputStream inFromServer = serverSocket.getInputStream();
		while (recvMsgSize != -1 && keepReading) {
			recvMsgSize = inFromServer.read(responseBuf);

        	//resp = parser.sendData(responseBuf);
//        	keepReading = resp.isDoneReading();

        }
    }
}
