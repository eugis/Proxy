package Proxy;

public class SocketRankingKey {
	private String host_port;
	private long rank;
	
	public SocketRankingKey(String host_port) {
		super();
		this.host_port = host_port;
		this.rank = 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host_port == null) ? 0 : host_port.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SocketRankingKey)) {
			return false;
		}
		SocketRankingKey other = (SocketRankingKey) obj;
		if (host_port == null) {
			if (other.host_port != null) {
				return false;
			}
		} else if (!host_port.equals(other.host_port)) {
			return false;
		}
		return true;
	}

	public void newUsed() {
		this.rank ++;
	}
	
	public String getHostPort() {
		return this.host_port;
	}
	
	public long getRank() {
		return this.rank;
	}
}
