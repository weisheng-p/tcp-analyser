import java.util.ArrayList;


public class Flow {
	
	// TCP 3 Way Hand shake
	public Integer syn, synAck, ackAck;
	/*
	PacketInfo syn;
	PacketInfo synAck;
	PacketInfo ackAck;
	 */
	
	// data packet that is being transmitted
	public ArrayList <Integer> dataPackets;
	//ArrayList<PacketInfo> dataPackets;
	
	// TCP terminate sequence
	public Integer fin, finAck;
	
	public long totalLength;
	
	public int dupAck = 0;
	// timestamp when started and ended
	public long started;
	public long ended;
	
	public Flow()
	{
		dataPackets = new ArrayList<Integer>();
	}
	
}
