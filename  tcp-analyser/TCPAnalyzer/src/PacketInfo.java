import jpcap.packet.TCPPacket;


public class PacketInfo {
	// index of the packet, addition slot are for resend
	public String destIP;
	public String srcIP;
	public int destPort;
	public int srcPort;
	public boolean sync, fin, ack;
	public long ackNum, seqNum;
	public int dataLen;
	//captured time in seconds
	public long time;
	
	public PacketInfo(TCPPacket tcp)
	{
		this.ack = tcp.ack;
		this.fin = tcp.fin;
		this.sync = tcp.syn;
		this.ackNum = tcp.ack_num;
		this.seqNum = tcp.sequence;
		this.ackNum = tcp.ack_num;
		this.destIP = tcp.dst_ip.getHostAddress();
		this.srcIP = tcp.src_ip.getHostAddress();
		this.destPort = tcp.dst_port;
		this.srcPort = tcp.src_port;
		this.dataLen = (tcp.data.length);
		this.time = tcp.sec;
	}
}

