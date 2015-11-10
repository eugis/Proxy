package July;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import CarlyAdmin.CarlyAdmin;
import July.ConnectionHandler;
import Logs.CarlyLogger;

public class ThreadPoolSocketServer  {
    private ServerSocket serverSocket;
    private ConnectionHandler handler;
    private int THREAD_POOL_SIZE = 2;

    private static Logger logs = CarlyLogger.getCarlyLogger();
    
    public ThreadPoolSocketServer(final int port, final InetAddress interfaz, final ConnectionHandler handler)
            throws IOException {
        init(new ServerSocket(port, 50, interfaz), handler);
    }

    public ThreadPoolSocketServer(final int port, final ConnectionHandler handler) throws IOException {
        init(new ServerSocket(port, 50), handler);
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
                            System.out.printf("Se conecto %s\n", s);
                            logs.info("Se conecto " + s);
                            
                            System.out.println(ThreadPoolSocketServer.this.handler);
                            ThreadPoolSocketServer.this.handler.handle(socket);
                            
                            if (!socket.isClosed()) {
                                socket.close();
                                System.out.printf("Cerrando %s\n", s);
                                logs.info("Cerrando" +  s);
                            }
                            System.out.printf("Se desconecto %s\n", s);
                            logs.info("Se desconecto" +  s);
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
    		(new Thread(new CarlyAdmin())).start();
            ThreadPoolSocketServer server = new ThreadPoolSocketServer(20007, InetAddress.getByName("localhost"), new ThreadSocketHandler());
            server.run();
        } catch (final Exception e) {
            System.out.println("Ocurrio un error");
            e.printStackTrace();
        }
    }

}
