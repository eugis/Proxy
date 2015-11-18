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
		Socket serverSocket = null;
		try{
			InputStream in = s.getInputStream();
	        OutputStream out = s.getOutputStream();
	        
	        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer
	        
	        ReadingState state = ReadingState.UNFINISHED;
	        
	        String host2connect = "";
	        int port2connect = -1;
	        
	        // Receive until client closes connection, indicated by -1 return
	        boolean keepReading = !s.isClosed() && s.isConnected();
	        ByteBuffer bBuffer;
	        byte[] byteReq;
	        
	        while ((keepReading && (in.read(receiveBuf)) != -1)) {      //Connection reset
	        	bBuffer = ByteBuffer.wrap(receiveBuf);
	        	
	            state = parser.sendData(bBuffer);
	            switch (state) {
					case UNFINISHED:
							
						break;
					case FINISHED:
		                keepReading = false; //indicar el fin del request
	
		            	host2connect = parser.getHost();
		                port2connect = parser.getPort();
						
		                byteReq = parser.getRequest();
	//	                System.out.println("long request: " + byteReq.length);                   
	//	                System.out.println("request: " + new String(byteReq));
	
		                try{	                  	
		                   	serverSocket = writeToServer(host2connect, port2connect, byteReq, serverSocket);
		                   	parser.resetParser();
			                readFromServer(serverSocket, out);
		                }catch(UnknownHostException e){
		       				logs.error(e);
		        			serverSocket = null;
	        				int sCode = 112; //TODO: cambiar po un 500
	        				byteReq = parser.getHttpResponse(sCode).getBytes();
		        			out.write(byteReq, 0, byteReq.length);
		        			out.flush();
		                }catch(SocketException e){
		                	if (s.isClosed()) {
		                		keepReading = false;
		                	}
		                }
	           			break;
					case ERROR:
						if(parser.isFinished()){
							byteReq = parser.getHttpResponse().getBytes();
							out.write(byteReq);
							out.flush();	
							keepReading = false;
						}
						break;
					}
	           	receiveBuf = new byte[BUFSIZE];
	           	if(s.isClosed() || !s.isConnected()) {
	       			keepReading = false;
	       		}
	        }

		}catch(SocketException e){
			System.out.println("reset connection??");	
			//TODO: add logger
		} finally {
			//Agregado del finally para las cosas que siempre deberán pasar: cerar el serversocket y cerrar el cliente en aso de que terminase o sucediecen exceptionse
			if (serverSocket != null) {
	        	ProxyConnectionManager.closeConnection(serverSocket);
	        	serverSocket = null;
	        }
	        if (!s.isClosed()) {
		        s.close();	
			}
		}
	}

	synchronized
	private Socket writeToServer(String host, int port, byte[] byteReq, Socket serverSocket) throws UnknownHostException, IOException{
		
		if (serverSocket == null || serverSocket.isClosed()){
			ProxySocket pSocket = ProxyConnectionManager.getConnection(host, port);
			serverSocket = pSocket.getSocket();	
		}else if (!serverSocket.getInetAddress().getHostName().equals(host)){
			ProxyConnectionManager.closeConnection(serverSocket);
			ProxySocket pSocket = ProxyConnectionManager.getConnection(host, port);
			serverSocket = pSocket.getSocket();	
		}
    	OutputStream outFromServer = serverSocket.getOutputStream();
    	outFromServer.write(byteReq, 0, byteReq.length);
    	outFromServer.flush();
    	return serverSocket;
	}
    synchronized
	//TODO: Agregar el parser de respuesta al contenido del servidor
    private void readFromServer(Socket serverSocket, OutputStream out) throws IOException{
    	byte[] responseBuf = new byte[BUFSIZE];
    	HttpResponse resp = new HttpResponse();
    	boolean keepReading = true;
    	InputStream inFromServer = serverSocket.getInputStream();
    	ByteBuffer bBuffer;
    	int readSize = 0;
    	try {
    		
    		while (/*!resp.isResponseFinished() &&*/ keepReading && ((readSize = inFromServer.read(responseBuf)) != -1) ) {
//    			bBuffer = ByteBuffer.wrap(responseBuf);
//				responseBuf = ServerParserUtils.processResponse(bBuffer, resp);

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

                out.write(responseBuf, 0, readSize);
				out.flush();
				responseBuf = new byte[BUFSIZE];
//				boolean d = resp.isResponseFinished();
				
    		}
		} catch (SocketTimeoutException e) {
				logs.error("timeout");
				
				int sCode = 504;
				byte[] byteReq = parser.getHttpResponse(sCode).getBytes();
				out.write(byteReq, 0, byteReq.length);
				out.flush();

				keepReading = false;
				return;
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
    		//TODO: verificar si el status code es válido
    		int sCode = 503;
			byte[] byteReq = parser.getHttpResponse(sCode).getBytes();
			out.write(byteReq, 0, byteReq.length);
			out.flush();
    	}
    }
}
