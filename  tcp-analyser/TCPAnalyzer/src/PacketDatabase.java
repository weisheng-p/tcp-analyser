import java.util.ArrayList;

import jpcap.packet.TCPPacket;


public class PacketDatabase {
	
	private ArrayList<PacketInfo> db;
	private ArrayList<Integer> unprocessed;	// use for optimising 
	private boolean dirty = false;
	
	public PacketDatabase()
	{
		db = new ArrayList<PacketInfo>();
		unprocessed = new ArrayList<Integer>();
	}
	
	public PacketInfo get(int i)
	{
		return db.get(i);
	}
	
	public boolean mark(int index)
	{
		dirty = true;
		return unprocessed.remove(new Integer(index));
	}
	
	public boolean add(TCPPacket tcp)
	{
		if(!dirty)
		{
			PacketInfo pi = new PacketInfo(tcp);
			unprocessed.add(new Integer(db.size() - 1));
			
			return db.add(pi);
		}
		return false;
	}
	
	public int numberOfUnprocessed()
	{
		return unprocessed.size();
	}
	
	public int size()
	{
		return db.size();
	}
	
}
