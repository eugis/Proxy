package July;

import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class ProxyConnections {
	private final int MaxConnections = 10;
	private int availableConnections; 
	private HashMap<SocketRankingKey, Set<ProxySocket>> connections;
	private static ProxyConnections instance;
	
	public static ProxyConnections getInstance(){
		if(instance == null){
			instance = new ProxyConnections();
			instance.connections = new HashMap<SocketRankingKey, Set<ProxySocket>>();
			instance.availableConnections = instance.MaxConnections;
		}
		return instance;
	}
	
	public void saveNewConnection(String host_port, ProxySocket ps){
		makeAPlaceIfNeeded();
		SocketRankingKey k = new SocketRankingKey(host_port);
		Set<ProxySocket> set = this.connections.get(k);
		if ( set == null) {
			//si la clave no existe porque no hay abierta ninguna conexión.
			set = new HashSet<ProxySocket>();
		} else {
			//actualizo el ranking de las claves en caso de que esté en uso.
			updateKeyRank(host_port);
		}
		set.add(ps);
		this.connections.put(k, set);
		this.availableConnections--;
	}
	
	public ProxySocket getConnection(String host_port){
		SocketRankingKey key = new SocketRankingKey(host_port);
		Set<ProxySocket> psSet = this.connections.get(key);
		if (psSet != null) {
			return getUnusedSocket(psSet);
		}
		return null;
	}
	
	public HashMap<SocketRankingKey, Set<ProxySocket>> getConnections() {
		return this.connections;
	}
	
	public void closeConnectionForSocket(Socket s, boolean forceDelete) {
		for (Entry<SocketRankingKey, Set<ProxySocket>> e : this.connections.entrySet()) {
			Iterator<ProxySocket> it = e.getValue().iterator();
			while (it.hasNext()) {
				ProxySocket ps = it.next();
				if (ps.getSocket().equals(s)) {
					if (e.getValue().size() > 1 || forceDelete) {
						it.remove();
						this.availableConnections++ ;
					} else {
						ps.setInUse(false);
					}
				}
			}
			if (e.getValue().size() == 0) {
				this.connections.remove(e.getKey());
			}
		}
	}

	//TODO: ver si se puede modularizar un poco más
	private ProxySocket getUnusedSocket(Set<ProxySocket> psSet) {
		for (Entry<SocketRankingKey, Set<ProxySocket>> e : this.connections.entrySet()) {
			if(e.getValue().equals(psSet)) {
				e.getKey().newUsed();
				if (psSet.size() == 1) {
					ProxySocket ps = psSet.iterator().next();
					if (!ps.isInUse()) {
						return ps;
					}
				}
			}
		}
		return null;
	}
	
	private void updateKeyRank(String keyString) {
		for(SocketRankingKey k : this.connections.keySet()) {
			if (k.getHostPort().equals(keyString)) {
				k.newUsed();
			}
		}
	}
	
	private void makeAPlaceIfNeeded() {
		if (this.availableConnections == 0) {
			//TODO: revisar si esto está medianamente bien pensado
			long min = Long.MAX_VALUE;
			SocketRankingKey k = null;
			SocketRankingKey lastUnusedKey = null;
			for (Entry<SocketRankingKey, Set<ProxySocket>> e : this.connections.entrySet()) {
				if (e.getValue().size() == 1 && !e.getValue().iterator().next().isInUse()) {
					lastUnusedKey = k;
					if (e.getKey().getRank() < min) {
						k = e.getKey();
					}
				}
			}
			//Si existe una clave con menor ranking la borro, sino borro alguna que no esté en uso.
			this.connections.remove((k != null ? k : lastUnusedKey));
			this.availableConnections ++;
		}
	}

}
