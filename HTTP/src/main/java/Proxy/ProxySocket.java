package Proxy;

import java.io.IOException;
import java.net.Socket;

public class ProxySocket {

	private Socket s;
	private boolean inUse;
	
	public ProxySocket(Socket serverSocket) {
		this.s = serverSocket;
		this.inUse = true;
	}

	public Socket getSocket() {
		return s;
	}

	public void setSocket(Socket s) {
		this.s = s;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}
	
	public void userFinished() {
		this.setInUse(false);
	}
	
	public void close() throws IOException {
		this.getSocket().close();
	}
}
