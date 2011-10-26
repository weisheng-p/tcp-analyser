import java.util.ArrayList;

import jpcap.JpcapCaptor;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;


public class OurTCPProcesser extends TCPProcesser {

	public OurTCPProcesser()
	{
		db = new PacketDatabase();
		flows = new ArrayList<Flow>();
	}
	
	/* (non-Javadoc)
	 * @see TCPProcesser#readTrace(java.lang.String)
	 */
	@Override
	void readTrace(String path) {
		
		try {
			JpcapCaptor captor=JpcapCaptor.openFile(path);
			TCPPacket tcppacket;
			while(true)
			{
				 Packet packet=captor.getPacket();
				 if(packet==null || packet==Packet.EOF) break;
				 if(packet instanceof TCPPacket)
				 {
					 db.add((TCPPacket) packet);
				 }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see TCPProcesser#follow(int)
	 */
	@Override
	void follow(int syn) {
		// TODO Auto-generated method stub
		int synAck = -1, ackAck = -1;
		
		PacketInfo synP, syncAckP, ackAckP, tmp;
		
		int i = syn + 1;
		
		synP = db.get(syn);

		while(i < db.size())
		{
			syncAckP = db.get(i);
			//if the packet's sync and ack is set and ack number is the sync seq's number and if the communicating port and ip is the same
			if(syncAckP.ack && syncAckP.sync && syncAckP.ackNum == synP.seqNum + 1
					&& (syncAckP.destIP.equals(synP.srcIP) && syncAckP.srcIP.equals(synP.destIP) &&
							syncAckP.destPort == synP.srcPort && syncAckP.srcPort == synP.destPort 
							)
					)
			{
				
				synAck = i;
				break;
			}
			i ++;
		}
		
		if(synAck != -1 )
		{
			syncAckP = db.get(synAck);
			// find the next ack
			i = synAck + 1;
			while(i < db.size())
			{
				ackAckP = db.get(i);
				// if the packet's ack flag is set and ack number is the sync-ack seq's number and if the communicating port and ip is the same
				if(ackAckP.ack && ackAckP.ackNum == syncAckP.seqNum + 1
						&& (ackAckP.destIP.equals(syncAckP.srcIP) && ackAckP.srcIP.equals(syncAckP.destIP) &&
								ackAckP.destPort == syncAckP.srcPort && ackAckP.srcPort == syncAckP.destPort
								)
						)
				{
					ackAck = i;
					break;
				}
				i ++;
			}
		}
		else
		{
			return;
		}
		if(ackAck != -1)
		{
			ackAckP = db.get(ackAck);
			Flow aFlow = new Flow();
			aFlow.syn = syn;
			aFlow.ackAck = ackAck;
			aFlow.synAck = synAck;
			flows.add(aFlow);
			
			i = ackAck + 1;
			
			// get all data transfer
			
			// last ack, last seq number, dest, src based on initializer's perspective  
			long lastAck = ackAckP.ackNum, lastSeq = ackAckP.seqNum;
			String dest = ackAckP.destIP, src = ackAckP.srcIP; 
			int destPort = ackAckP.destPort, srcPort = ackAckP.srcPort;
			while(i < db.size())
			{
				tmp = db.get(i);
				if(tmp.fin) break;	// fin
				
				// to
				if(tmp.destIP.equals(dest) && tmp.srcIP.equals(src) && tmp.destPort == destPort && tmp.srcPort == srcPort)
				{
					
				}
				// from
				else if(tmp.destIP.equals(src) && tmp.srcIP.equals(dest) && tmp.destPort == srcPort && tmp.srcPort == destPort)
				{
					
				}
				i ++;
			}

		}
		else
		{
			return;
		}
		
		
	}
	
	@Override
	void processTrace() {
		int current = 0;
		
		PacketInfo pi;
		while(current < db.size())
		{
			pi = db.get(current);
			
			// is sync packet only
			if(pi.sync && !pi.ack)
			{
				// look for sync ack
//				System.out.println("hihi");
				follow(current);
			}
			
			current ++;
			
		}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OurTCPProcesser tp = new OurTCPProcesser();
		String filename = "/home/weisheng/Documents/trace/trace1";
		tp.readTrace(filename);
		tp.processTrace();
		int size = tp.flows.size();
		
		System.out.printf("size: %d\n", size);
	}


}
