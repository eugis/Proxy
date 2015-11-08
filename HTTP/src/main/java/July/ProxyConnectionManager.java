package July;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map.Entry;

public class ProxyConnectionManager {

	private static final int SLEEP_TIME = 1000;
	
	public static ProxySocket getConnection(String host, int port, Thread t) throws UnknownHostException, IOException{
		boolean itsme = false;
		ProxySocket pSocket = ProxyConnections.getInstance().getConnection(host+"-"+port);
		Socket serverSocket = null;
		if(pSocket == null){
			System.out.println("ENTRA AL PRIMER IF");
			serverSocket = new Socket(host, port);
    		serverSocket.setSoTimeout(5000);
    		pSocket = new ProxySocket(serverSocket, t);
    		//pSocket.setInUse(true);
    		ProxyConnections.getInstance().saveNewConnection(host+"-"+port, pSocket);
		}else{
			System.out.println("entra al else!!");
			if(pSocket.getCurrentUser().equals(Thread.currentThread())){
				System.out.println("SOY YO");
				itsme = true;
			}else{
				if(pSocket.isInUse()){
					System.out.println("ESTA EN USO!");
					pSocket.setUser(Thread.currentThread());
				}
				System.out.println("a nurmirrrrrrrr");
				while(pSocket.isInUse() && !Thread.interrupted()){
					try {
						Thread.sleep(SLEEP_TIME);
					} catch (InterruptedException e) {
						// I'm awake :)
						System.out.println("AWAKEeeeeee");
					}
				}
				/*if(!pSocket.isInUse()){
					//serverSocket = pSocket.getSocket();
					pSocket.setInUse(true);
				}
				else{
					//thread wait?
				}*/
			}
		}
		serverSocket = pSocket.getSocket();
		pSocket.setInUse(true);
		if(!itsme){
			pSocket.setUser(Thread.currentThread());
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
	
	/*public static void endUsingConnection(Socket s, Thread t) {
		String key = "";
		for (Entry<String, ProxySocket> conns : ProxyConnections.getInstance().getConnections().entrySet()) {
			if(conns.getValue().getSocket().equals(s)){
				key = conns.getKey();
				conns.getValue().userFinished();
				//conns.getValue().setUser(t);-----???
			}
		}
	}
	*/
}
