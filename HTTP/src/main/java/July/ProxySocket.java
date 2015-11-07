package July;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class ProxySocket {

	private Socket s;
	private boolean inUse;
	private LinkedList<Thread> users;
	
	public ProxySocket(Socket serverSocket, Thread thread) {
		this.s = serverSocket;
		this.inUse = true;
		this.users = new LinkedList<Thread>();
		this.users.add(thread);
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
	
	public void setUser(Thread t) {
		this.users.addLast(t);
	}

	public Thread getCurrentUser() {
		return this.users.getFirst();
	}
	
	public void userFinished() {
		this.users.removeFirst();
		if (this.users.size() != 0) {
			this.users.getFirst().interrupt(); //Despertar el primero de la cola??	
		} else {
			try {
				this.getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
