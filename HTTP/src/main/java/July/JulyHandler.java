package July;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
		readDataFromClient(s);
		this.parser = new HTTPParser();
		this.connections = new HashMap<String, Socket>();
	}

	private void readDataFromClient(Socket s) throws IOException{
		InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();
        
        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer
        int recvMsgSize = in.read(receiveBuf);   // Size of received message
        ParserResponse resp = null;
        if(recvMsgSize != -1){
        	resp = parser.sendData(receiveBuf);
        	
        }
        String host2connect = "";
        int port2connect = -1;
        Socket serverSocket = null;
        // Receive until client closes connection, indicated by -1 return
        while (recvMsgSize != -1 && !resp.isDoneReading()) {
          //out.write(receiveBuf, 0, recvMsgSize);
          resp = parser.sendData(receiveBuf);
          host2connect = resp.getHost();
          port2connect = resp.getPort();
          
          if(host2connect.equals(s.getInetAddress())){//devuelve la response al mismo cliente, o sea, reboto en el proxy
        	  serverSocket = connections.get(host2connect+"-"+port2connect);
        	  if(serverSocket == null){
        		  serverSocket = new Socket(host2connect, port2connect);
        		  connections.put(host2connect+"-"+port2connect, serverSocket);
        		  out = serverSocket.getOutputStream();
        	  }
          }else{
        	  
          }
        }
        
        s.close();  // Close the socket.  We are done with this client!
	}
}
