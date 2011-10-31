package TCP;


public class Flow {
	
	/**
	 * indicate the state this tcp flow is in
	 *
	 */
	public enum State
	{
		INIT, SYNC, SYNC_ACK, ACK, DATA_TRANSFER, FIN, FIN_ACK, TERMINATED, STRAY;
	}
	/**
	 * indicate the direction of a packet
	 *
	 */
	public enum Direction
	{
		INCOMING, OUTGOING;
	}
	/**
	 * Total number of flow
	 */
	public static int count = 0; 
	/**
	 * the flow id assigned to this flow
	 */
	public int id;
	/**
	 * source and destination tcp port number
	 */
	public int srcPort, destPort;
	/**
	 * source and destination ip address
	 */
	public String srcIP, destIP;
	
	public int num_dupAck = 0,
			   num_outOfOrder = 0;
	public SlidingWindow 	srcWindow = new SlidingWindow(), 
							destWindow = new SlidingWindow();
	public long dataLength = 0;
	public State current = State.INIT, prev = State.INIT;

	public static final float RTT_ALPHA = 0.9f;
	
	// estimated
	public float incomingRTT = 0, outgoingRTT = 0;
	public long	lastSend = 0, lastRecv = 0;
	public Direction lastDirection = Direction.INCOMING; 
	
	/**
	 * update the rrt for the various direction with the packet 
	 * @param pi the packet to use to update the rtt
	 */
	public void updateRTT (PacketInfo pi)
	{
		if(pi.direction.equals(Direction.INCOMING))
		{
			if(lastDirection.equals(Direction.INCOMING))
			{
				lastRecv = pi.time;	// reset timer
			}
			else
			{
				// update rtt
				if(outgoingRTT == 0) outgoingRTT = (pi.time - lastSend);
				else					
					outgoingRTT = Flow.RTT_ALPHA * outgoingRTT + (1 - Flow.RTT_ALPHA) * (pi.time - lastSend);
			}
			lastRecv = pi.time;
		}
		else
		{
			if(lastDirection.equals(Direction.OUTGOING))
			{
				lastSend = pi.time;	// reset timer
			}
			else
			{
				// update rtt
				if(incomingRTT == 0) incomingRTT = (pi.time - lastRecv);
				else
					incomingRTT = Flow.RTT_ALPHA * incomingRTT + (1 - Flow.RTT_ALPHA) * (pi.time - lastRecv);
			}
			lastSend = pi.time;
		}
		
		lastDirection = pi.direction;
	}
	
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
	/**
	 * check the direction of the packet base on this flow
	 * @param pi
	 */
	void checkDirection(PacketInfo pi)
	{
		if(destIP.equals(pi.destIP) && srcIP.equals(pi.srcIP))
			pi.direction = Direction.INCOMING;
		else pi.direction = Direction.OUTGOING;
	}
	/**
	 * predict the state of this flow for stay packets
	 * @param pi the stray packet
	 */
	public void predictState(PacketInfo pi)
	{
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

	/**
	 * add the packet to the sliding window
	 * @param pi the packet of interest
	 */
	private void addToSlidingWindow(PacketInfo pi) {
		if(pi.dataLen == 0) return;
		
		if(pi.direction.equals(Direction.INCOMING))
		{
			if(srcWindow.getNextExpectedSeqNum() != pi.seqNum && srcWindow.started) num_outOfOrder++;
			srcWindow.addFilledWindow(pi.seqNum, pi.seqNum + pi.dataLen);
			srcWindow.updateWindowSize(pi.window);
		}
		else
		{
			if(destWindow.getNextExpectedSeqNum() != pi.seqNum && srcWindow.started) num_outOfOrder++;
			destWindow.addFilledWindow(pi.seqNum, pi.seqNum + pi.dataLen);
			destWindow.updateWindowSize(pi.window);
		}
		updateRTT(pi);
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
				if(pi.dataLen == 0)
				{
					num_dupAck ++;
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
					current = State.TERMINATED;	
				}
				break;
			case TERMINATED:
				// we actually don't have to do anything here
				break;
			
		}
		return current == State.TERMINATED;
	}
	
	/**
	 * clear both sliding window
	 */
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
	
	
}
