package Proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import CarlyAdmin.manager.StatisticsManager;
import Logs.CarlyLogger;

public class RunnableSocket implements Runnable {
	
	private Socket currentSocket;
	private ConnectionHandler handler;
	
	private static Logger logs = CarlyLogger.getCarlyLogger();

	public RunnableSocket(Socket socket, ConnectionHandler handler) {
		super();

		this.currentSocket = socket;
		this.handler = handler;
	}

	public void run() {

		try {
					
			String s = currentSocket.getRemoteSocketAddress().toString();
			
			System.out.printf("Se conecto %s\n", s);
			logs.info("Se conecto " + s );
			//una nueva conexion
			StatisticsManager.getInstance().incRequestAccess();
			
			handler.handle(currentSocket);
			
			if (!currentSocket.isClosed()) {
				currentSocket.close();
				System.out.printf("Cerrando %s \n", s);
				logs.info("Cerrando" +  s );
			}
			
			System.out.printf("Se desconecto %s\n", s);
			logs.info("Se desconecto" +  s );
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.printf("Excepcion al manejar conexion\n");  
			logs.error("Excepcion al manejar conexion\n");
			logs.error(e);
		}
	}

}
