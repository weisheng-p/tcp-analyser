public abstract class TCPProcesser {
	
	/**
	 * The database for a list of meta information for the tcp packets
	 */
	public PacketDatabase db;
	
	/**
	 * process a trace file and extract some meta data information about the packet in the trace file 
	 * @param path the location of the trace file
	 */
	abstract void processTrace(String path);
	
	/**
	 * try to follow a flow based on the sync packet
	 * @param syn the index (according to the database) to the syn packet 
	 */
	abstract void follow(int syn);
}
