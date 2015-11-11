package Proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import Logs.CarlyLogger;
import ParserRequest.HttpParser;
import ParserRequest.ParserResponse;
import ParserRequest.ParserUtils;
import ParserRequest.ReadingState;
import ParserResponse.HttpResponse;
import ParserResponse.ServerParserUtils;

public class ThreadSocketHandler implements ConnectionHandler{

	private static final int BUFSIZE = 1024;
	private HttpParser parser;
	private ServerParserUtils serverParser;
	
	private static Logger logs = CarlyLogger.getCarlyLogger();
	
	@Override
	public void handle(Socket s) throws IOException {
		this.parser = new HttpParser();
		this.serverParser = new ServerParserUtils();
		readDataFromClient(s);
	}

	private void readDataFromClient(Socket s) throws IOException{
		InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();
        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer
        int recvMsgSize = 0;   // Size of received message
        ParserResponse resp = null;
        ReadingState state = ReadingState.UNFINISHED;
        //boolean keepReading = false;
        String host2connect = "";
        int port2connect = -1;
        boolean client = true;
        // Receive until client closes connection, indicated by -1 return
        Socket serverSocket = null;
        ByteBuffer bBuffer;
        byte[] byteReq;
        while (recvMsgSize != -1 /*&& !keepReading*/) {
        	receiveBuf = new byte[BUFSIZE]; //TODO: hacer de forma elegante
        	recvMsgSize = in.read(receiveBuf);
        	bBuffer = ByteBuffer.wrap(receiveBuf);
        	if(recvMsgSize != -1){
        		//Harcoded receiveBuf
//            	String request = "GET / HTTP/1.1\n"+"Host: www.google.com\n\n";
//        		byte[] msg = request.getBytes(Charset.forName("UTF-8"));
//            	

            	state = parser.sendData(bBuffer);
            		switch (state) {
					case UNFINISHED:
						System.out.println("UNFINISHED: No termino de leer el request");
						//TODO con esto empieza a escribir mientras va leyendo una vez que tiene el host
						/*if(parser.getHost() != null){	
		            		host2connect = parser.getHost();
		                    port2connect = parser.getPort();
		                    
							logs.info("Host: " + host2connect);
							logs.info("Port: " + port2connect);
							
		                    ByteBuffer request = parser.getRequest();
		                    byteReq = request.array();
		                    serverSocket = writeToServer(host2connect, port2connect, byteReq, serverSocket);
		                    //voy limpiando el buffer que va almacenando el request
		                    parser.cleanRequest();
						}*/
						
						break;
					case FINISHED:
						//TODO si vas vaciando el request cuando encontras el host esto no va
						logs.info("Host: " + host2connect);
						logs.info("Port: " + port2connect);
						System.out.println("Host:" + parser.getHost());
	                	System.out.println("Port:" + parser.getPort());
	                	
	            		
	//            		if(!resp.returnToClient()){
	            		host2connect = parser.getHost();
	                    port2connect = parser.getPort();
//	                    ByteBuffer request = parser.getRequest();
//	                    ParserUtils.printBuffer(request);
//	                    byteReq = request.array();
//	                    System.out.println("long: " + byteReq.length);
//	                    String req = new String(byteReq);
//	                    System.out.println("req:" + req);
	                    String hardCodeResp = "GET / HTTP/1.1\nHost: www.google.com\n\n\n";
//	                    System.out.println("iguales:" + req.equals(hardCodeResp));
	                    byteReq = hardCodeResp.getBytes();
	                    
	                    serverSocket = writeToServer(host2connect, port2connect, byteReq, serverSocket);
	                    readFromServer(serverSocket, out);		
            			break;
					case ERROR:
						System.out.println("Error: No termino de leer el request");
						if(parser.isFinished()){
							byteReq = parser.getHttpResponse().getBytes();
							out.write(byteReq);
		              	  	out.flush();	
						}
						break;
					}
        		}
        	}

//        System.out.println("cerrarrrrrrrrrr");
        // Close the socket.  We are done with this client!
        if (serverSocket != null) {
        	ProxyConnectionManager.closeConnection(serverSocket);	
        }
        s.close();
	}

	private Socket writeToServer(String host, int port, byte[] byteReq, Socket serverSocket) throws UnknownHostException, IOException{
		if (serverSocket == null ){
			ProxySocket pSocket = ProxyConnectionManager.getConnection(host, port);
			serverSocket = pSocket.getSocket();	
		}
		//TODO: remover syso
    	System.out.println(serverSocket);
    	OutputStream outFromServer = serverSocket.getOutputStream();
    	outFromServer.write(byteReq);
    	outFromServer.flush();
    	return serverSocket;
	}
    
	//TODO: Agregar el parser de respuesta al contenido del servidor
    private void readFromServer(Socket serverSocket, OutputStream out) throws IOException{
    	byte[] responseBuf = new byte[BUFSIZE];
    	int recvMsgSize = 0;
    	HttpResponse resp = new HttpResponse();
    	boolean keepReading = true;
    	InputStream inFromServer = serverSocket.getInputStream();
    	boolean reading = false;
    	ByteBuffer bBuffer;
		while (recvMsgSize != -1 && keepReading) {
			try {
				recvMsgSize = inFromServer.read(responseBuf);
				bBuffer = ByteBuffer.wrap(responseBuf);
	        	ServerParserUtils.processResponse(bBuffer, resp);
				reading = true;
				String res = new String(responseBuf);
                System.out.println("req:" + res);
				out.write(resp.getBuf(), 0, resp.getBufLength());
				out.flush();
			} catch (SocketTimeoutException e) {
				if (reading) {
					keepReading = false;
				} else {
					System.out.println("timeout");
					
					//TODO: devolver un response de timeout (504)
//					byteReq = parser.getHttpResponse().getBytes();
//					out.write(byteReq);
//              	out.flush();
				}
			}
			
        }
    }
}
