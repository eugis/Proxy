package Proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import Logs.CarlyLogger;
import ParserRequest.HttpParser;
import ParserRequest.ReadingState;
import ParserResponse.HttpResponse;
import ParserResponse.ServerParserUtils;

public class ThreadSocketHandler implements ConnectionHandler{

	private static final int BUFSIZE = 4096*2;
	private HttpParser parser;
	
	private static Logger logs = CarlyLogger.getCarlyLogger();
	
	@Override
	synchronized
	public void handle(Socket s) throws IOException {
		this.parser = new HttpParser();
		readDataFromClient(s);
	}
	
	synchronized
	private void readDataFromClient(Socket s) throws IOException{
		try{
		InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();
        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer
//        int recvMsgSize = 0;   // Size of received message
        ReadingState state = ReadingState.UNFINISHED;
        String host2connect = "";
        int port2connect = -1;
        // Receive until client closes connection, indicated by -1 return
        Socket serverSocket = null;
        boolean keepReading = !s.isClosed() && s.isConnected();
        ByteBuffer bBuffer;
        byte[] byteReq;
        int i = 0;
        while ((keepReading && (/*recvMsgSize = */in.read(receiveBuf)) != -1)) {      //Connection reset
//        	System.out.println("request sin parsear" + new String(receiveBuf));
        	bBuffer = ByteBuffer.wrap(receiveBuf);
        	
            state = parser.sendData(bBuffer);
            switch (state) {
				case UNFINISHED:
//					System.out.println("UNFINISHED: No termino de leer el request");
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
//					System.out.println("Host:" + parser.getHost());
//	                System.out.println("Port:" + parser.getPort());
	                	
	            	host2connect = parser.getHost();
	                port2connect = parser.getPort();
					
	                byteReq = parser.getRequest();
//	                System.out.println("long request: " + byteReq.length);
	                    
//	                System.out.println("request: " + new String(byteReq));

//	                    String req = new String(byteReq);
//	                    System.out.println("req:" + req);
	                try{	                  	
	                   	serverSocket = writeToServer(host2connect, port2connect, byteReq, serverSocket);
	                   	parser.resetParser();
//	                   	System.out.println("voy a intentar leer");
		                i = readFromServer(serverSocket, out, i);
	                }catch(UnknownHostException e){
	       				logs.error(e);
	        			serverSocket = null;
        				int sCode = 112;
        				byteReq = parser.getHttpResponse(sCode).getBytes();
	        			out.write(byteReq, 0, byteReq.length);
	        			out.flush();
	                }catch(SocketException e){
//	                	System.out.println("socket cerrado");
	                	if (s.isClosed()) {
	                		keepReading = false;
	                	}
	                }
           			break;
				case ERROR:
//					System.out.println("Error: No termino de leer el request");
					if(parser.isFinished()){
						byteReq = parser.getHttpResponse().getBytes();
						out.write(byteReq);
						out.flush();	
					}
					break;
				}
//        	}
           	receiveBuf = new byte[BUFSIZE];
//           	System.out.println("por pasar a la verificacion del reset");
           	if(s.isClosed() || !s.isConnected()) {
       			keepReading = false;
       		}
        }

//        System.out.println("cerrarrrrrrrrrr");
        // Close the socket.  We are done with this client!
        if (serverSocket != null) {
        	ProxyConnectionManager.closeConnection(serverSocket);
        	serverSocket = null;
        }
        if (!s.isClosed()) {
			s.getInputStream().close();
	        s.close();	
		}
		}catch(SocketException e){
			if (!s.isClosed()) {
				System.out.println("reset connection?? ");
				s.getInputStream().close();
		        s.close();	
			}			
		}
	}

	synchronized
	private Socket writeToServer(String host, int port, byte[] byteReq, Socket serverSocket) throws UnknownHostException, IOException{
		if (serverSocket == null || serverSocket.isClosed()){
			ProxySocket pSocket = ProxyConnectionManager.getConnection(host, port);
			serverSocket = pSocket.getSocket();	
//			System.out.println("Socket nuevo!!");
		}else if (!serverSocket.getInetAddress().getHostName().equals(host)){
			ProxyConnectionManager.closeConnection(serverSocket);
			ProxySocket pSocket = ProxyConnectionManager.getConnection(host, port);
			serverSocket = pSocket.getSocket();	
//			System.out.println("Cambio de socket!!");
		}
		//TODO: remover syso
//		System.out.println(" ");
//    	System.out.println("lo que escribo en el server" + new String(byteReq));
    	OutputStream outFromServer = serverSocket.getOutputStream();
    	outFromServer.write(byteReq, 0, byteReq.length);
    	outFromServer.flush();
    	return serverSocket;
	}
    synchronized
	//TODO: Agregar el parser de respuesta al contenido del servidor
    private int readFromServer(Socket serverSocket, OutputStream out, int i) throws IOException{
    	byte[] responseBuf = new byte[BUFSIZE];
    	HttpResponse resp = new HttpResponse();
    	boolean keepReading = true;
    	InputStream inFromServer = serverSocket.getInputStream();
    	ByteBuffer bBuffer;
    	boolean read = false;
    	int readSize = 0;
//    	System.out.println("corridas del read del servidor" + i);
        i ++;
    	try {
    		
    		while (!resp.isResponseFinished() && keepReading && ((readSize = inFromServer.read(responseBuf)) != -1) ) {
    			
//    			System.out.println("antes de leet" + new String(responseBuf));
    			read = true;
    			bBuffer = ByteBuffer.wrap(responseBuf);
				responseBuf = ServerParserUtils.processResponse(bBuffer, resp);

//				String res = new String(responseBuf);
//                System.out.println("req:" + res);
//                keepReading = !resp.getState().getIsFinished();
//				out.write(responseBuf, 0, responseBuf.length);
//				out.flush();
//		    	if (s.isClosed()) {
//		            if (serverSocket != null) {
//		            	ProxyConnectionManager.closeConnection(serverSocket);	
//		            }
//		            return; //TODO: evitar pipeline?? ver si funciona.
//		    	} 
//                System.out.println("después de leet" + new String(responseBuf));
//   			  logs.error("writing client");
//                System.out.println(out);
//                System.out.println(readSize);
                out.write(responseBuf, 0, readSize);
				out.flush();
//				System.out.println("Escribiendo en el cliente ... "); 
				responseBuf = new byte[BUFSIZE];
				boolean d = resp.isResponseFinished();
				
    		}
		} catch (SocketTimeoutException e) {
				System.out.println("timeout");
				logs.error("timeout");
				
				int sCode = 504;
				byte[] byteReq = parser.getHttpResponse(sCode).getBytes();
				out.write(byteReq, 0, byteReq.length);
				out.flush();

				keepReading = false;
				System.out.println("timeout completed");
		}
    	if (serverSocket.isClosed() || !serverSocket.isConnected()) {
    		String host = serverSocket.getInetAddress().getHostName();
    		int port = serverSocket.getPort();
    		ProxySocket pSocket = ProxyConnectionManager.getConnection(host, port);
			serverSocket = pSocket.getSocket();	
			logs.error("reopen socket");
			System.out.println("Reabriendo Socket!!");
    	} //TODO: ver si esto está realmente de forma coherente...
    	if (readSize == -1) {
    		System.out.println("DEVOLVIOOOO -1");
    		int sCode = 504;
			byte[] byteReq = parser.getHttpResponse(sCode).getBytes();
			out.write(byteReq, 0, byteReq.length);
			out.flush();
    	}
    	System.out.println("terminando el read");
    	return i;
    }
}
