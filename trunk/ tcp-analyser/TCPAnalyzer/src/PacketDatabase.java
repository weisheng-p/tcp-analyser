import java.util.ArrayList;

import jpcap.packet.TCPPacket;


public class PacketDatabase {
	
	private ArrayList<PacketInfo> db;
	private ArrayList<Integer> unprocessed;
	
	public PacketInfo get(int i)
	{
		return db.get(i);
	}
	
	public boolean mark(int index)
	{
		return unprocessed.remove(new Integer(index));
	}
	
	public boolean add(TCPPacket tcp)
	{
		PacketInfo pi = new PacketInfo(tcp);
		unprocessed.add(new Integer(db.size() - 1));
		return db.add(pi);
	}
	
	public int numberOfUnprocessed()
	{
		return unprocessed.size();
	}
	
}
