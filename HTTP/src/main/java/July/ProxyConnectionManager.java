package July;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map.Entry;

public class ProxyConnectionManager {

	public static ProxySocket getConnection(String host, int port) throws UnknownHostException, IOException{
		ProxySocket pSocket = ProxyConnections.getInstance().getConnection(host+"-"+port);
		Socket serverSocket = null;
		if(pSocket == null){
			serverSocket = new Socket(host, port);
    		serverSocket.setSoTimeout(5000);
    		pSocket = new ProxySocket(serverSocket, Thread.currentThread());
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
		String key = "";
		for (Entry<String, ProxySocket> conns : ProxyConnections.getInstance().getConnections().entrySet()) {
			if(conns.getValue().getSocket().equals(s)){
				key = conns.getKey();
				conns.getValue().userFinished();
				if (conns.getValue().getCurrentUser() == null) {
					//antes de borrarlo deberia preguntar si no lo quiere usar otro cliente que este actualmente bloqueado
					ProxyConnections.getInstance().getConnections().remove(key);
					//--------------------------------------------------------			
				}
			}
		}
	}
}
