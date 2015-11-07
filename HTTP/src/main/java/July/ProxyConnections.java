package July;

import java.util.HashMap;

public class ProxyConnections {

	private HashMap<String, ProxySocket> connections;
	private static ProxyConnections instance;
	
	public static ProxyConnections getInstance(){
		if(instance == null){
			instance = new ProxyConnections();
			instance.connections = new HashMap<String, ProxySocket>();
		}
		return instance;
	}
	
	public void saveNewConnection(String host_port, ProxySocket ps){
		this.connections.put(host_port, ps);
	}
	
	public ProxySocket getConnection(String host_port){
		return this.connections.get(host_port);
	}

	public HashMap<String, ProxySocket> getConnections() {
		return this.connections;
	}
}
