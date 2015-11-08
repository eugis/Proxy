package July;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

public class ProxySocket {

	private Socket s;
	private boolean inUse;
	private LinkedList<Thread> users;
	
	public ProxySocket(Socket serverSocket, Thread thread) {
		this.s = serverSocket;
		this.inUse = true;
		this.users = new LinkedList<Thread>();
		//this.users.add(thread);
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
	
	synchronized
	public void setUser(Thread t) {
		this.users.addLast(t);
	}

	public Thread getCurrentUser() {
		return this.users.getFirst();
	}
	
	synchronized
	public void userFinished() {
		this.users.removeFirst();
		System.out.println(this.users);
		this.setInUse(false);
		if (this.users.size() != 0) {
			//Thread t = this.users.getFirst();
			//this.users.removeFirst();
			this.users.getFirst().interrupt(); //Despertar el primero de la cola??	
		} else {
			try {
				System.out.println("closing socket");
				this.getSocket().close();
				//hostConnections.remove(host);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
