package Proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import CarlyAdmin.manager.StatisticsManager;
import Logs.CarlyLogger;

public class ThreadSocket extends Thread {
	
	private Socket currentSocket;
	private ConnectionHandler handler;
	
//	private ServerSocket serverSocket;
	
	private static Logger logs = CarlyLogger.getCarlyLogger();

	public ThreadSocket(Socket socket, ConnectionHandler handler) {
		super();

		this.currentSocket = socket;
		this.handler = handler;
	}
	
//	public ThreadSocket(ConnectionHandler handler, ServerSocket serverSocket) {
//		this.handler = handler;
//		this.serverSocket = serverSocket;
//	}

	public void run() {
		
//		while(true) {

		try {
			
//			currentSocket = serverSocket.accept();
			
			String s = currentSocket.getRemoteSocketAddress().toString();
			
			System.out.printf("Se conecto %s en %s\n", s, this.getName());
			logs.info("Se conecto " + s + "(" + this.getName() + ")");
			//una nueva conexion
			StatisticsManager.getInstance().incRequestAccess();
			
			handler.handle(currentSocket);
			
			if (!currentSocket.isClosed()) {
				currentSocket.close();
				System.out.printf("Cerrando %s (%s)\n", s, this.getName());
				logs.info("Cerrando" +  s + "(" + this.getName() + ")");
			}
			
			System.out.printf("Se desconecto %s en %s\n", s, this.getName());
			logs.info("Se desconecto" +  s + "(" + this.getName() + ")");
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.printf("Excepcion al manejar conexion\n");  
			logs.error("Excepcion al manejar conexion\n");
			logs.error(e);
		}
		}
//	}

}
