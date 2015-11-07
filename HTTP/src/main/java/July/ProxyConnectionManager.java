package July;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProxyConnectionManager {

	public static ProxySocket getConnection(String host, int port) throws UnknownHostException, IOException{
		ProxySocket pSocket = ProxyConnections.getInstance().getConnection(host+"-"+port);
		Socket serverSocket = null;
		if(pSocket == null){
			serverSocket = new Socket(host, port);
    		serverSocket.setSoTimeout(5000);
    		pSocket = new ProxySocket(serverSocket);
    		pSocket.setInUse(true);
    		ProxyConnections.getInstance().saveNewConnection(host+"-"+port, pSocket);
		}else{
			if(!pSocket.isInUse()){
				//serverSocket = pSocket.getSocket();
				pSocket.setInUse(true);
			}
			else{
				//thread wait?
			}
		}
		return pSocket;
	}

	public static void closeConnection(Socket s) {
		
		
	}
}
