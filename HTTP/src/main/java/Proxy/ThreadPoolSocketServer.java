package Proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import CarlyAdmin.CarlyAdmin;
import Logs.CarlyLogger;
import Proxy.ConnectionHandler;

public class ThreadPoolSocketServer implements Runnable {

    private ServerSocket serverSocket;
//    private ConnectionHandler handler;
    private int THREAD_POOL_SIZE = 30;
    private final ExecutorService pool;

    private static Logger logs = CarlyLogger.getCarlyLogger();

//    public ThreadPoolSocketServer(final ConnectionHandler handler) {
//    	InputStream is = getClass().getResourceAsStream(
//				"resources/setup.properties");
//    	Properties p = new Properties();
//		try {
//			p.load(is);
//			String stringPort = p.getProperty("proxy-port");
//			String address = p.getProperty("proxy-address");
//			int port = Integer.parseInt(stringPort);
//			InetAddress interfaz = InetAddress.getByName(address);
//			this.handler = handler;
//			
//			init(new ServerSocket(port, 50, interfaz), handler);
//		} catch (Exception e) {
//			logs.error("Proxy: Proxy - Missing configuration file", e);
//			throw new RuntimeException("Missing configuration file...");
//		} finally{
//			try {
//				is.close();
//			} catch (IOException e) {
//				logs.error("Proxy: Proxy - Error when reading the configuration file", e);
//				throw new RuntimeException("Error when reading the configuration file");
//			}
//		}
//    	
//	}

//	private void init(final ServerSocket s, final ConnectionHandler handler) {
//    	if(s == null || handler == null) {
//    		throw new IllegalArgumentException();
//    	}
//
//        this.serverSocket = s;
//        this.handler = handler;
//    }

    public ThreadPoolSocketServer(final ConnectionHandler handler) {
    	InputStream is = getClass().getResourceAsStream(
				"resources/setup.properties");
    	Properties p = new Properties();
		try {

			p.load(is);
			String stringPort = p.getProperty("proxy-port");
			String address = p.getProperty("proxy-address");
			int port = Integer.parseInt(stringPort);
			InetAddress interfaz = InetAddress.getByName(address);

			// Se crea el socket para escuchar las conexiones
			this.serverSocket = new ServerSocket(port, 50, interfaz);

			// Se crea el executor de los threads para atender las conexiones
//			pool = Executors.newCachedThreadPool();
			pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

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

    public void run() {
        System.out.printf("Escuchando en %s\n", serverSocket.getLocalSocketAddress());
        
		try {
			while(true) {
				Socket socket = serverSocket.accept();
				ConnectionHandler handler = new ThreadSocketHandler();
				Runnable thread = new RunnableSocket(socket, handler);
				pool.execute(thread);
		        // System.out.println("Se inicio el thread " + thread.getName());
			}
		} catch (IOException e) {
			System.out.println("Error al hacer el acep");
			pool.shutdown();
		}
    }

//    public void run() {
//        System.out.printf("Escuchando en %s\n", serverSocket.getLocalSocketAddress());
//        
//		try {
//			while(true) {
//				Socket socket = serverSocket.accept();
//				ConnectionHandler handler = new ThreadSocketHandler();
//				ThreadSocket thread = new ThreadSocket(socket, handler);
//		        thread.start();
//		        System.out.println("Se inicio el thread " + thread.getName());
//			}
//		} catch (IOException e) {
//			System.out.println("Error al hacer el acep");
//		}
//    }

    public static void main(String[] args) {
        try {
    		System.out.println("Starting CarlyAdmin Server...");
    		logs.info("Starting CarlyAdmin Server...");
    		(new Thread(new CarlyAdmin())).start();
            ThreadPoolSocketServer server = new ThreadPoolSocketServer(new ThreadSocketHandler());
            server.run();
        } catch (final Exception e) {
            System.out.println("Ocurrio un error");
            e.printStackTrace();
            logs.error(e);
        }
    }

}
