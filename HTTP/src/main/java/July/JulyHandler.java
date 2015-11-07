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
	private static final int SLEEP_TIME = 1000;
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
        int recvMsgSize = in.read(receiveBuf);   // Size of received message
        ParserResponse resp = null;
        //boolean keepReading = false;
        String host2connect = "";
        int port2connect = -1;
        // Receive until client closes connection, indicated by -1 return
        Socket serverSocket = null;
        while (recvMsgSize != -1 /*&& !keepReading*/) {
        	recvMsgSize = in.read(receiveBuf);
        	resp = parser.sendData(receiveBuf);
        	//keepReading = resp.isDoneReading();
        	
        	if(resp.isDoneReading()){
        		host2connect = resp.getHost();
                port2connect = resp.getPort();
                String hardCodeResp = "GET / HTTP/1.1 \n\n";
                byte[] byteReq = hardCodeResp.getBytes();
                if(!s.getInetAddress().toString().contains(host2connect)){//devuelve la response al mismo cliente, o sea, reboto en el proxy
                	serverSocket = writeToServer(host2connect, port2connect, byteReq);
                	readFromServer(serverSocket, out);
                	ProxyConnectionManager.endUsingConnection(serverSocket, Thread.currentThread());
                }else{
                	out.write(byteReq);
              	  	out.flush();
                }
        	}
        }

        s.close();  // Close the socket.  We are done with this client!
        if (serverSocket != null) {
        	ProxyConnectionManager.closeConnection(serverSocket);	
        }
	}

	private Socket writeToServer(String host, int port, byte[] byteReq) throws UnknownHostException, IOException{
		ProxySocket pSocket = ProxyConnectionManager.getConnection(host, port, Thread.currentThread());
		while (pSocket.getCurrentUser() != Thread.currentThread()) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				// I'm awake :)
			}
		}
		Socket serverSocket = pSocket.getSocket();
    	if(serverSocket != null){//doble validacion, podria no ir
    		OutputStream outFromServer = serverSocket.getOutputStream();
    		outFromServer.write(byteReq);
    		outFromServer.flush();
    	}
    	return serverSocket;
	}
    	
    private void readFromServer(Socket serverSocket, OutputStream out) throws IOException{
    	byte[] responseBuf = new byte[BUFSIZE];
    	int recvMsgSize = 0;
    	ParserResponse resp = null;
    	boolean keepReading = false;
    	InputStream inFromServer = serverSocket.getInputStream();
		while (recvMsgSize != -1 && !keepReading) {
			recvMsgSize = inFromServer.read(responseBuf);
			out.write(responseBuf, 0, recvMsgSize);
			out.flush();
        	resp = parser.sendData(responseBuf);
        	keepReading = resp.isDoneReading();
        }
    }
}
