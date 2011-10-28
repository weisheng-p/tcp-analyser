import java.util.ArrayList;

import jpcap.JpcapCaptor;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import util.SimpleMap;
import TCP.ConnectionInfo;
import TCP.Flow;
import TCP.PacketInfo;


public class TCPProcesser {
	private SimpleMap<ConnectionInfo,Flow> activeConnections;
	public ArrayList<ConnectionInfo> blackListConnection;		// keep track of stray connection
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
			if(pi.sync && ! pi.ack)
				blackListConnection.remove(ci);
			else
				return;	// blacklisted connection
		}
		if(activeConnections.containsKey(ci))
		{
			Flow aFlow = activeConnections.get(ci);
			if(aFlow.addPacket(pi))
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
			else if(aFlow.current == Flow.State.TERMINIATED)
			{
				cleanUp(ci,aFlow);
			}
		}
	}

	private void blackList(ConnectionInfo ci) {
		Flow.count --;	// correct the flow counter
		blackListConnection.add(ci);
		
	}
	/**
	 * write out the stat for the flow and update the stat for the trace
	 * @param ci
	 * @param aFlow
	 */
	void cleanUp(ConnectionInfo ci, Flow aFlow)
	{
		activeConnections.remove(ci);
	}
	
	public static void main(String args[])
	{
		TCPProcesser tp = new TCPProcesser();
		String filename = "/home/weisheng/Documents/trace/trace1";
		tp.readTrace(filename);
		System.out.println("end count: " + Flow.count);
		System.out.println("left overs: " + tp.leftovers());
		System.out.println("Black listed: "+ tp.blackListConnection.size());
	}
}
