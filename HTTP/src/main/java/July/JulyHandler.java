package July;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import CarlyProxy.ParserResponse;
import Parser.HTTPParser;

public class JulyHandler implements ConnectionHandler{

	private static final int BUFSIZE = 1024; 
	private HTTPParser parser;
	private Map<String, Socket> connections;
	
	@Override
	public void handle(Socket s) throws IOException {
		this.parser = new HTTPParser();
		this.connections = new HashMap<String, Socket>();
		readDataFromClient(s);
	}

	private void readDataFromClient(Socket s) throws IOException{
		InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();
        
        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer
        byte[] responseBuf = new byte[BUFSIZE];
        int recvMsgSize = in.read(receiveBuf);   // Size of received message
        ParserResponse resp = null;
        if(recvMsgSize != -1){
        	//resp = parser.sendData(receiveBuf);
        }
        String host2connect = "";
        int port2connect = -1;
        // Receive until client closes connection, indicated by -1 return
        while (recvMsgSize != -1 && !resp.isDoneReading()) {
          //out.write(receiveBuf, 0, recvMsgSize);
          resp = parser.sendData(receiveBuf);
          recvMsgSize = in.read(receiveBuf);
        }
        
        host2connect = resp.getHost();
        port2connect = resp.getPort();
        //byte[] response = resp.getResponse();
        String hardCodeResp = "GET / HTTP/1.1 \n\n";
        byte[] byteReq = hardCodeResp.getBytes();
        if(!s.getLocalAddress().toString().contains(host2connect)){//devuelve la response al mismo cliente, o sea, reboto en el proxy
        	Socket serverSocket = writeToServer(host2connect, port2connect, byteReq);
        	readFromServer(serverSocket, out);
        }else{
      	  out.write(byteReq);
        }
        s.close();  // Close the socket.  We are done with this client!
	}

	private Socket writeToServer(String host, int port, byte[] byteReq) throws UnknownHostException, IOException{
		Socket serverSocket = connections.get(host+"-"+port);
    	if(serverSocket == null){
    		serverSocket = new Socket(host, port);
    		serverSocket.setSoTimeout(5000);
    		connections.put(host+"-"+port, serverSocket);
    		  
    		OutputStream outFromServer = serverSocket.getOutputStream();
    		outFromServer.write(byteReq);
    		outFromServer.flush();
    	}
    	return serverSocket;
	}
    	
    private void readFromServer(Socket serverSocket, OutputStream out) throws IOException{
    	byte[] responseBuf = new byte[BUFSIZE];
    	int recvMsgSize = 0;
    	InputStream inFromServer = serverSocket.getInputStream();
		while ((recvMsgSize = inFromServer.read(responseBuf)) != -1) {
			out.write(responseBuf, 0, recvMsgSize);
			out.flush();
		}
    }
}
