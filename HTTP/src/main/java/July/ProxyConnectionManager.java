package July;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProxyConnectionManager {
	
	public static ProxySocket getConnection(String host, int port) throws UnknownHostException, IOException{
		String keyString = host+"-"+port;
		ProxySocket pSocket = ProxyConnections.getInstance().getConnection(keyString);
		if(pSocket == null){
			System.out.println("CREA UNA NUEVA CONEXION");
    		ProxyConnections.getInstance().saveNewConnection(keyString, generateNewSocket(host, port));    		
		}
		return pSocket;
	}

	public static void closeConnection(Socket s) throws IOException {
		//Si el socket está cerrado, es que la conexión no soporta persistencia y fuerzo borrarlo del mapa.
		System.out.println("CIERRA UNA CONEXION - la borra:" + s.isClosed());
		ProxyConnections.getInstance().closeConnectionForSocket(s, s.isClosed());
	}
	
	private static ProxySocket generateNewSocket(String host, int port) throws UnknownHostException, IOException {
		System.out.println("CRANDO UN NUEVO SOCKET");
		Socket serverSocket = new Socket(host, port);
		serverSocket.setSoTimeout(5000);
		ProxySocket pSocket = new ProxySocket(serverSocket);
		return pSocket;
	}
}
