import java.util.ArrayList;
import java.util.Collection;

import jpcap.JpcapCaptor;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import util.SimpleMap;
import TCP.ConnectionInfo;
import TCP.Flow;
import TCP.PacketInfo;
import TCP.Flow.State;


public class TCPProcesser {
	private SimpleMap<ConnectionInfo,Flow> activeConnections;
	public ArrayList<ConnectionInfo> blackListConnection;		// keep track of stray connection
	int started = 0; 
	int ended = 0;
	public TCPProcesser()
	{
		activeConnections = new SimpleMap<ConnectionInfo,Flow>();
		blackListConnection = new ArrayList<ConnectionInfo>();
	}
	public int leftovers()
	{
		return activeConnections.size();
	}
	
	public void readTrace(String path)
	{
		try {
			JpcapCaptor captor=JpcapCaptor.openFile(path);
			while(true)
			{
				 Packet packet=captor.getPacket();
				 if(packet==null || packet==Packet.EOF) break;
				 if(packet instanceof TCPPacket)
				 {
					 add((TCPPacket) packet);
				 }
			}
			captor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void add(TCPPacket tcp)
	{
		PacketInfo pi = new PacketInfo(tcp);
		ConnectionInfo ci = (ConnectionInfo) pi;
		if(blackListConnection.contains(ci))
		{
			if(pi.sync && (! pi.ack))	// start of new connection
				blackListConnection.remove(ci);
			else
				return;	// blacklisted connection
		}
		if(activeConnections.containsKey(ci))
		{
			Flow aFlow = activeConnections.get(ci);
			aFlow.addPacket(pi);
			if(aFlow.current == Flow.State.TERMINIATED)
			{
				cleanUp(ci,aFlow);
				
			}
		}
		else
		{
			Flow aFlow = new Flow(ci);
			activeConnections.put(ci, aFlow);
			aFlow.addPacket(pi);
			if(aFlow.current == Flow.State.STRAY)
			{
				blackList(ci);
			}
			else if(aFlow.current == Flow.State.TERMINIATED)	// actually impossible to reach
			{
				cleanUp(ci,aFlow);
				System.out.println("weeee");
			}
			else
			{
				started++;
			}
		}
	}

	private void blackList(ConnectionInfo ci) {
		Flow.count --;	// correct the flow counter
		blackListConnection.add(ci);
		activeConnections.remove(ci);
		
	}
	/**
	 * write out the stat for the flow and update the stat for the trace
	 * @param ci
	 * @param aFlow
	 */
	void cleanUp(ConnectionInfo ci, Flow aFlow)
	{
		activeConnections.remove(ci);
		ended ++;
	}
	public void printLeftOverStates()
	{
		Collection<Flow> a = activeConnections.values();
		int[] buckets = new int[9];
		for(int i = 0; i < 9; i ++)
			buckets[i] = 0;
		for(Flow f : a)
		{
			switch(f.current)
			{
				case INIT: buckets[0] ++;
					break;
				case SYNC: buckets[1] ++;
					break;
				case SYNC_ACK: buckets[2] ++;
					break;
				case ACK: buckets[3] ++;
					break;
				case DATA_TRANSFER: buckets[4] ++;
					break;
				case FIN: buckets[5] ++;
					break;
				case FIN_ACK: buckets[6] ++;
					break;
				case TERMINIATED: buckets[7] ++;
					break;
				case STRAY: buckets[8] ++;
					break;
			}
		}
		System.out.printf("init: %d; sync: %d; sync_ack: %d; ack: %d, data: %d, fin: %d, ter: %d, stray: %d\n", 
							buckets[0], buckets[1],buckets[2],buckets[3],buckets[4],buckets[5],buckets[6],buckets[7],buckets[8]);
	}
	public static void main(String args[])
	{
		TCPProcesser tp = new TCPProcesser();
		String filename = "/home/weisheng/Documents/trace/trace3";
		tp.readTrace(filename);
		System.out.println("Started: " + tp.started);
		System.out.println("Ended: " + tp.ended);
		System.out.println("left overs: " + tp.leftovers());
		System.out.println("Black listed: "+ tp.blackListConnection.size());
		tp.printLeftOverStates();
	}
}
