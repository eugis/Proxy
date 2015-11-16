package Proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.apache.log4j.Logger;

import CarlyAdmin.CarlyAdmin;
import Logs.CarlyLogger;
import Proxy.ConnectionHandler;

public class ThreadPoolSocketServer  {
    private ServerSocket serverSocket;
    private ConnectionHandler handler;
    private int THREAD_POOL_SIZE = 15;

    private static Logger logs = CarlyLogger.getCarlyLogger();
    
    public ThreadPoolSocketServer(final int port, final InetAddress interfaz, final ConnectionHandler handler)
            throws IOException {
    	//TODO sacar el port y inetAddres desde un properties
        init(new ServerSocket(port, 50, interfaz), handler);
    }

    public ThreadPoolSocketServer(final int port, final ConnectionHandler handler) throws IOException {
        init(new ServerSocket(port, 50), handler);
    }

    public ThreadPoolSocketServer() {
    	InputStream is = getClass().getResourceAsStream(
				"resources/setup.properties");
    	Properties p = new Properties();
		try {
			p.load(is);
			String stringPort = p.getProperty("proxy-port");
			String address = p.getProperty("proxy-address");
			int port = Integer.parseInt(stringPort);
			InetAddress interfaz = InetAddress.getByName(address);
			ConnectionHandler handler = new ThreadSocketHandler();
			
			init(new ServerSocket(port, 50, interfaz), handler);
		} catch (Exception e) {
			logs.error("Proxy: Proxy - Missing configuration file", e);
			throw new RuntimeException("Missing configuration file...");
		} finally{
			try {
				is.close();
			} catch (IOException e) {
				logs.error("Proxy: Proxy - Error when reading the configuration file", e);
				throw new RuntimeException("Error when reading the configuration file");
			}
		}
    	
	}

	private void init(final ServerSocket s, final ConnectionHandler handler) {
    	if(s == null || handler == null) {
    		throw new IllegalArgumentException();
    	}

        this.serverSocket = s;
        this.handler = handler;
    }

    public void run() {
        System.out.printf("Escuchando en %s\n", serverSocket.getLocalSocketAddress());
        
        for(int i = 0; i < THREAD_POOL_SIZE; i++) {
            Thread thread = new Thread() {
                public void run() {
                    while(true) {
                        try {
                            Socket socket = ThreadPoolSocketServer.this.serverSocket.accept();
                            
                            String s = socket.getRemoteSocketAddress().toString();
                            System.out.printf("Se conecto %s (%s)\n", s, this.getName());
                            logs.info("Se conecto " + s + "(" + this.getName() + ")");
                            
                            ThreadPoolSocketServer.this.handler.handle(socket);
                            
                            if (!socket.isClosed()) {
                                socket.close();
                                System.out.printf("Cerrando %s (%s)\n", s, this.getName());
                                logs.info("Cerrando" +  s + "(" + this.getName() + ")");
                            }
                            System.out.printf("Se desconecto %s (%s)\n", s, this.getName());
                            logs.info("Se desconecto" +  s + "(" + this.getName() + ")");
                        } catch (IOException e) {
                        	e.printStackTrace();
                            System.err.printf("Excepcion al manejar conexion\n");            
                            logs.error(e);
                            
                        }
                    }
                };
            };
            thread.start();
            System.out.println("Se inicio el thread " + thread.getName());
        }
    }

    public static void main(String[] args) {
        try {
    		System.out.println("Starting CarlyAdmin Server...");
    		logs.info("Starting CarlyAdmin Server...");
    		(new Thread(new CarlyAdmin())).start();
            //ThreadPoolSocketServer server = new ThreadPoolSocketServer(20007, InetAddress.getByName("localhost"), new ThreadSocketHandler());
            ThreadPoolSocketServer server = new ThreadPoolSocketServer();
            server.run();
        } catch (final Exception e) {
            System.out.println("Ocurrio un error");
            e.printStackTrace();
            logs.error(e);
        }
    }

}
