package TCP;

/**
 * information for the result of the merge 
 *
 */
class MergeResult
{
	public boolean duplicate;
	public boolean merged;
}
public class Side {

	public long prevByteRecv = -1;
	public long lastAckRecv = -1;
	public long lastByteRecv = -1;
	public boolean started = false;
	public long firstSequence = 0;

	public boolean ackData(long ackNumber)
	{
		if(lastAckRecv == -1)
		{
			lastAckRecv = ackNumber; return false;
		}
		if(ackNumber < prevByteRecv && ackNumber == lastAckRecv)
		{
			return true;
		}
		lastAckRecv = ackNumber;
		return false;
	}
	
	public void updateLastByteRecv(long byteNumber)
	{
		prevByteRecv = byteNumber;
		lastByteRecv = Math.max(byteNumber, lastByteRecv);
	}
	
	
	/**
	 * 
	 * @return next expected sequence number from the sender
	 */
	public long getNextExpectedSeqNum()
	{
		return prevByteRecv; 
	}
	
	/**
	 * 
	 * @return the last expected seq number
	 */
	public long getLastExpectedSeqNum()
	{
		return lastByteRecv;
	}
	
}
