package TCP;

import util.SlidingWindow;

public class Flow {
	
	/**
	 * Total number of flow
	 */
	public static int count = 0; 
	
	int id;
	int srcPort, destPort;
	String srcIP, destIP;
	int dupAck = 0,
		oop = 0;	// number of out of order packet
	SlidingWindow 	srcWindow = new SlidingWindow(), 
					destWindow = new SlidingWindow();
	long dataLength = 0;
	public State current = State.INIT;
	long started = 0;
	
	public Flow(int srcPort, int destPort, String srcIP, String destIP) {
		this.srcPort = srcPort;
		this.destPort = destPort;
		this.srcIP = srcIP;
		this.destIP = destIP;
		this.id = Flow.count ++;

	}
	
	public Flow(ConnectionInfo pi)
	{
		this.srcIP = pi.srcIP;
		this.destIP = pi.destIP;
		this.srcPort = pi.srcPort;
		this.destPort = pi.destPort;
		this.id = Flow.count ++;
	}
	void checkDirection(PacketInfo pi)
	{
		if(destIP.equals(pi.destIP) && srcIP.equals(pi.srcIP)) pi.incoming = false;
		else pi.incoming = true;
	}
	/**
	 * 
	 * @param pi
	 * @return true if this flow has terminated
	 */
	public boolean addPacket(PacketInfo pi)
	{
		checkDirection(pi);
		switch(current)
		{
			case INIT:
				// expect a sync
				if(pi.sync)
				{
					current = State.SYNC;
				}
				else
				{
					current = State.STRAY;
				}
				break;
			case SYNC:
				if(pi.sync && pi.ack)
				{
					current = State.SYNC_ACK;
				}
				break;
			case SYNC_ACK:
				if(pi.ack)
				{
					current = State.ACK;
				}
				break;
			case ACK:
				current = State.DATA_TRANSFER;
//				break;
			case DATA_TRANSFER:
				if(pi.fin)
				{
					current = State.FIN;
				}
				else
				{
					dataLength += pi.dataLen;
					if(pi.incoming)
					{
						srcWindow.addFilledWindow(pi.seqNum, pi.seqNum + pi.dataLen);
					}
					else
					{
						destWindow.addFilledWindow(pi.seqNum, pi.seqNum + pi.dataLen);
					}
				}
				break;
			case FIN:
				if(pi.fin && pi.ack)
				{
					current = State.FIN_ACK;
				}
				// more data from other side
				else
				{
					dataLength += pi.dataLen;
					if(pi.incoming)
					{
						srcWindow.addFilledWindow(pi.seqNum, pi.seqNum + pi.dataLen);
					}
					else
					{
						destWindow.addFilledWindow(pi.seqNum, pi.seqNum + pi.dataLen);
					}
				}
				break;
			case FIN_ACK:
				if(pi.ack)
				{
					current = State.TERMINIATED;
				}
				break;
			case TERMINIATED:
				// we actually don't have to do anything here
				break;
			
		}
		return current == State.TERMINIATED;
	}
	
	public void clear()
	{
		srcWindow.clear();
		destWindow.clear();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
	public enum State
	{
		INIT, SYNC, SYNC_ACK, ACK, DATA_TRANSFER, FIN, FIN_ACK, TERMINIATED, STRAY;
	}
}
