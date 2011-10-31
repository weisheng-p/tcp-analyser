package TCP;
import jpcap.packet.TCPPacket;

/**
 * this is just a data container to be pass around in the application
 *
 */
public class PacketInfo extends ConnectionInfo{
	
	public boolean sync, fin, ack;
	public long ackNum, seqNum;
	public int dataLen;
	public Flow.Direction direction;
	//captured time in seconds
	public long time;
	public int window;
	public PacketInfo(TCPPacket tcp)
	{
		super(tcp.dst_ip.getHostAddress(),tcp.src_ip.getHostAddress(),tcp.dst_port,tcp.src_port);
		this.ack = tcp.ack;
		this.fin = tcp.fin;
		this.sync = tcp.syn;
		this.ackNum = tcp.ack_num;
		this.seqNum = tcp.sequence;
		this.dataLen = (tcp.data.length);
		this.time = tcp.sec;
		this.window = tcp.window;
		
	}
}

