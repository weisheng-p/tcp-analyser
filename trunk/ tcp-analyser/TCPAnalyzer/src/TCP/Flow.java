package TCP;

import util.SlidingWindow;

public class Flow {
	
	/**
	 * Total number of flow
	 */
	public static int count = 0; 
	
	public int id;
	public int srcPort, destPort;
	public String srcIP, destIP;
	public int num_dupAck = 0,
			   num_outOfOrder = 0;	// number of out of order packet
	public SlidingWindow 	srcWindow = new SlidingWindow(), 
					destWindow = new SlidingWindow();
	public long dataLength = 0;
	public State current = State.INIT;
	long started = 0;
	boolean predicited = false;
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
	
	public void predictState(PacketInfo pi)
	{
		predicited = true;
		if(pi.ack && pi.sync)
		{
			current = State.SYNC_ACK;
		}
		else if(pi.sync)
		{
			current = State.SYNC;
		}
		else if(pi.fin && pi.ack)
		{
			current = State.FIN_ACK;
		}
		else if(pi.fin)
		{
			current = State.FIN;
		}
		// can either be the last ack of the connection, or the last ack of the 3 way handshake or the ack in the middle connection
		else if(pi.ack)
		{
			// data transfer
			if(pi.dataLen > 1)
			{
				current = State.DATA_TRANSFER;
				dataLength += pi.dataLen;
				addToSlidingWindow(pi);
				return;
			}
			current = State.ACK;
		}
		
	}

	private void addToSlidingWindow(PacketInfo pi) {
		if(pi.dataLen == 0) return;
		if(pi.incoming)
		{
			if(srcWindow.getNextExpectedSeqNum() != pi.seqNum) num_outOfOrder++;
			srcWindow.addFilledWindow(pi.seqNum, pi.seqNum + pi.dataLen);
		}
		else
		{
			if(destWindow.getNextExpectedSeqNum() != pi.seqNum) num_outOfOrder++;
			destWindow.addFilledWindow(pi.seqNum, pi.seqNum + pi.dataLen);
		}
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
					break;
				}
				else
				{
					predictState(pi);
					break;
				}
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
					addToSlidingWindow(pi);
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
					addToSlidingWindow(pi);
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
		return "Flow [id=" + id + ", srcPort=" + srcPort + ", destPort="
				+ destPort + ", srcIP=" + srcIP + ", destIP=" + destIP
				+ ", dupAck=" + num_dupAck + ", oop=" + num_outOfOrder + ", dataLength="
				+ dataLength + ", current=" + current + "]";
	}
	
	public enum State
	{
		INIT, SYNC, SYNC_ACK, ACK, DATA_TRANSFER, FIN, FIN_ACK, TERMINIATED, STRAY;
	}
}
