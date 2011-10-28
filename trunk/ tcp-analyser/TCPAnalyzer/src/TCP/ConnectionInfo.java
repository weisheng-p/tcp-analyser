package TCP;

public class ConnectionInfo {
	public String srcIP;
	public String destIP;
	public int destPort;
	public int srcPort;
	
	public ConnectionInfo(String destIP, String srcIP,  int destPort, int srcPort) {
		super();
		this.srcIP = srcIP;
		this.destIP = destIP;
		this.destPort = destPort;
		this.srcPort = srcPort;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ConnectionInfo))
			return false;
		ConnectionInfo other = (ConnectionInfo) obj;
		return ((other.destPort == this.destPort && other.srcPort == this.srcPort &&
				other.srcIP.equals(this.srcIP) && other.destIP.equals(this.destIP)
				) ||
				(other.destPort == this.srcPort && other.srcPort == this.destPort &&
				other.srcIP.equals(this.destIP) && other.destIP.equals(this.srcIP)
				));
	}
}
