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
import ParserRequest.ReadingState;
import ParserResponse.HttpResponse;
import ParserResponse.ServerParserUtils;

public class ThreadSocketHandler implements ConnectionHandler{

	private static final int BUFSIZE = 4096;
	private HttpParser parser;
	
	private static Logger logs = CarlyLogger.getCarlyLogger();
	
	@Override
	public void handle(Socket s) throws IOException {
		this.parser = new HttpParser();
		readDataFromClient(s);
	}

	private void readDataFromClient(Socket s) throws IOException{
		InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();
        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer
//        int recvMsgSize = 0;   // Size of received message
        ReadingState state = ReadingState.UNFINISHED;
        String host2connect = "";
        int port2connect = -1;
        // Receive until client closes connection, indicated by -1 return
        Socket serverSocket = null;
        ByteBuffer bBuffer;
        byte[] byteReq;
        while ((/*recvMsgSize = */in.read(receiveBuf)) != -1 /*&& !keepReading*/) {      
        	System.out.println("request sin parsear" + new String(receiveBuf));
        	bBuffer = ByteBuffer.wrap(receiveBuf);
        	
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
					System.out.println("Host:" + parser.getHost());
	                System.out.println("Port:" + parser.getPort());
	                	
	            	host2connect = parser.getHost();
	                port2connect = parser.getPort();
	                logs.info("Host: " + host2connect);
					logs.info("Port: " + port2connect);
					
	                byteReq = parser.getRequest();
	                System.out.println("long request: " + byteReq.length);
	                    
	                System.out.println("request: " + new String(byteReq));

//	                    String req = new String(byteReq);
//	                    System.out.println("req:" + req);
	                try{
	                  	parser.resetParser();
	                   	serverSocket = writeToServer(host2connect, port2connect, byteReq, serverSocket);
		                readFromServer(serverSocket, s);
	                }catch(UnknownHostException e){
	       				logs.error(e);
	        			serverSocket = null;
        				int sCode = 112;
        				byteReq = parser.getHttpResponse(sCode).getBytes();
	        			out.write(byteReq, 0, byteReq.length);
	        			out.flush();
	                }		
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
        		//}
           		receiveBuf = new byte[BUFSIZE];
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
			System.out.println("Socket nuevo!!");
		}else if (!serverSocket.getInetAddress().getHostName().equals(host)){
			ProxyConnectionManager.closeConnection(serverSocket);
			ProxySocket pSocket = ProxyConnectionManager.getConnection(host, port);
			serverSocket = pSocket.getSocket();	
			System.out.println("Cambio de socketh!!");
		}
		//TODO: remover syso
//    	System.out.println(serverSocket);
		System.out.println(" ");
    	System.out.println("lo que escribo en el server" + new String(byteReq));
    	OutputStream outFromServer = serverSocket.getOutputStream();
    	outFromServer.write(byteReq);
    	outFromServer.flush();
    	return serverSocket;
	}
    
	//TODO: Agregar el parser de respuesta al contenido del servidor
    private void readFromServer(Socket serverSocket, /*OutputStream out*/Socket s) throws IOException{
    	byte[] responseBuf = new byte[BUFSIZE];
    	if (s.isClosed()) {
            if (serverSocket != null) {
            	ProxyConnectionManager.closeConnection(serverSocket);	
            }
            return; //TODO: evitar pipeline?? ver si funciona.
    	}
    	HttpResponse resp = new HttpResponse();
    	boolean keepReading = true;
    	InputStream inFromServer = serverSocket.getInputStream();
    	ByteBuffer bBuffer;
    	try {
    		while (keepReading && (inFromServer.read(responseBuf) != -1) /* && !resp.isResponseFinished()*/ ) {
				bBuffer = ByteBuffer.wrap(responseBuf);
				responseBuf = ServerParserUtils.processResponse(bBuffer, resp);
//				String res = new String(responseBuf);
//                System.out.println("req:" + res);
                keepReading = !resp.getState().getIsFinished();
//				out.write(responseBuf, 0, responseBuf.length);
//				out.flush();
                System.out.println("después de leet" + new String(responseBuf));
                s.getOutputStream().write(responseBuf, 0, responseBuf.length);
				s.getOutputStream().flush();
				System.out.println("Escribiendo en el cliente ... ");
    		}
		} catch (SocketTimeoutException e) {
					System.out.println("timeout");
					logs.error("timeout");

					int sCode = 504;
					byte[] byteReq = parser.getHttpResponse(sCode).getBytes();
//					out.write(byteReq, 0, byteReq.length);
//					out.flush();
					s.getOutputStream().write(byteReq, 0, byteReq.length);
					s.getOutputStream().flush();
					keepReading = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
