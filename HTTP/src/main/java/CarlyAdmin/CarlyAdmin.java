package CarlyAdmin;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

import CarlyAdmin.manager.ConfigurationManager;
import CarlyAdmin.manager.StatisticsManager;
import Logs.CarlyLogger;

public class CarlyAdmin implements Runnable{
	
	private static final int TIMEOUT = 3000;
	
	private int port;
	
	private Logger logs = CarlyLogger.getCarlyLogger();
	
	public CarlyAdmin() {
		
		InputStream is = getClass().getResourceAsStream(
				"resources/setup.properties");
		Properties p = new Properties();
		try {
			p.load(is);
			String stringPort = p.getProperty("carlyAdmin-port");
			this.port = Integer.parseInt(stringPort);
		} catch (Exception e) {
			logs.error("carlyAdmin: carlyAdmin - Missing configuration file", e);
			throw new RuntimeException("Missing configuration file...");
		} finally{
			try {
				is.close();
			} catch (IOException e) {
				logs.error("carlyAdmin: carlyAdmin - Error when reading the configuration file", e);
				throw new RuntimeException("Error when reading the configuration file");
			}
		}
	}
	
	public void run() {
		try {
			// Crea el selector
			Selector selector = Selector.open();
			// Crea el canal
			ServerSocketChannel channel = ServerSocketChannel.open();
			// Crea el binding entre el canal y el puerto
			channel.socket().bind(new InetSocketAddress(port));
			// Se registra el canal como no bloqueante
			channel.configureBlocking(false);
			// Registra el selector con el canal
			channel.register(selector, SelectionKey.OP_ACCEPT);
			TCPProtocol protocol = new CarlyAdminHandler(
					StatisticsManager.getInstance(),
					ConfigurationManager.getInstance());
			// Ciclo infinito para atender clientes
			while (true) {
				// Espera a que el canal este listo o que se de un timeout
				System.out.print(".");
				if (selector.select(TIMEOUT) == 0) {
					continue;
				}

				// Crea un iterador para las Keys del selector
				Iterator<SelectionKey> keys = selector.selectedKeys()
						.iterator();
				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					// Acepta la conxion
					if (key.isAcceptable()) {
						protocol.handleAccept(key);
					}
					// Lee del cliente
					if (key.isReadable()) {
						protocol.handleRead(key);
					}
					// Escribe en el cliente?
					if (key.isValid() && key.isWritable()) {
						protocol.handleWrite(key);
					}
					// Elimina la Key
					keys.remove();
				}
			}
		} catch (IOException e) {
			logs.error("XMPPC: run - Error inesperado en el Managers", e);
		}
		
	}
	
	public static void main(String[] args) {
		System.out.println("Starting CarlyAdmin Server...");
		(new Thread(new CarlyAdmin())).start();
	}
}
