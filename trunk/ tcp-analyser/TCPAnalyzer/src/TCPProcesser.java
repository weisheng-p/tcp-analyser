import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collection;

import jpcap.JpcapCaptor;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import util.SimpleMap;
import TCP.ConnectionInfo;
import TCP.Flow;
import TCP.PacketInfo;

/**
 * the main logic of the processing the tcp packets is in here

 */
public class TCPProcesser {
	public static int count = 0;
	private DecimalFormat twoDP = new DecimalFormat("#0.00");
	
	private SimpleMap<ConnectionInfo,Flow> activeConnections;
	public String filename = "/home/weisheng/Documents/trace/trace1";
	public String path = "/home/weisheng/Documents/";
	int started = 0; 
	int ended = 0;
	
	int biggie = 0;
	public TCPProcesser()
	{
		activeConnections = new SimpleMap<ConnectionInfo,Flow>();
	}
	public int leftovers()
	{
		return activeConnections.size();
	}
	
	public void readTrace(String path)
	{
		try {
			JpcapCaptor captor = JpcapCaptor.openFile(path);
			while(true)
			{
				TCPProcesser.count ++;
				 Packet packet=captor.getPacket();
				 if(packet==null || packet==Packet.EOF) break;
				 if(packet instanceof TCPPacket)
				 {
					 add((TCPPacket) packet);
				 }
			}
			captor.close();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * add packet to the processer to be process
	 * @param tcp the tcp packet of interest
	 */
	public void add(TCPPacket tcp)
	{
		PacketInfo pi = new PacketInfo(tcp);
		ConnectionInfo ci = (ConnectionInfo) pi;
		
		if(activeConnections.containsKey(ci))
		{
			Flow aFlow = activeConnections.get(ci);
			aFlow.addPacket(pi);
			if(aFlow.current == Flow.State.TERMINATED)
			{
				aFlow.timeEnded = pi.time;
				cleanUp(ci,aFlow);
				
			}
		}
		else
		{
			Flow aFlow = new Flow(ci);
			aFlow.timeStarted = pi.time;
			activeConnections.put(ci, aFlow);
			aFlow.addPacket(pi);
			if(aFlow.current == Flow.State.TERMINATED)
			{
				cleanUp(ci,aFlow);
			}
			else if(aFlow.current == Flow.State.SYNC)
			{
				started++;
			}
		}
	}


	/**
	 * printing out the traces.csv file
	 * content as following:
	 * trace_names
	 * num of connections established
	 * num of connections terminated
	 * num of connections that transfer more than 10kb of data
	 * @param aFlow
	 */
	public void printTrace(){
		BufferedWriter traceWriter = null;
		String tracename = filename.substring(filename.lastIndexOf('/')+1);
		File tf = new File(path+"traces.csv");
		try{
			traceWriter = new BufferedWriter(new FileWriter(tf, true));
		
			traceWriter.write(tracename);
			traceWriter.write(", ");
			traceWriter.write(Integer.toString(started));
			traceWriter.write(", ");
			traceWriter.write(Integer.toString(ended));
			traceWriter.write(", ");
			traceWriter.write(Integer.toString(biggie));
			traceWriter.write('\n');
			traceWriter.close();
		}
		catch (IOException e){
			System.out.println("Writing trace file error");
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * printing out flows_<trace_name>.csv
	 * Content as following:
	 * Flow id
	 * src ip:port
	 * dest ip:port
	 * final seq num for src
	 * num of dupACK
	 * num of out of order packets
	 * average throughput
	 * @param aFlow
	 */
	public void printFlow(Flow aFlow){
		if(aFlow.dataLength >= 10240) //Flow have more than 10kb of data
		{
//			BigDecimal rtt = new BigDecimal(aFlow.rtt);
//			rtt.divide(new BigDecimal(1000000));
//			BigDecimal avgThroughput;
//		
//			long maxWindowSize = aFlow.maxWindowSize * 8;
//
//			avgThroughput = new BigDecimal(maxWindowSize);
//			avgThroughput.multiply(new BigDecimal(0.75));
//			avgThroughput.divide(rtt, BigDecimal.ROUND_HALF_DOWN);
//			
			BigDecimal avgThroughput = new BigDecimal(aFlow.dataLength);
			avgThroughput.multiply(new BigDecimal(8));
//			BigDecimal timetake = new BigDecimal(aFlow.timeStarted);
			long timetaken = aFlow.timeEnded - aFlow.timeStarted;
			avgThroughput.divide(new BigDecimal(timetaken),BigDecimal.ROUND_HALF_DOWN);
			
			BufferedWriter flowWriter = null;
			String tracename = filename.substring(filename.lastIndexOf('/')+1);
			
			File ff = new File(path+"flows_"+tracename+".csv");
			biggie ++;
			try{
				flowWriter = new BufferedWriter(new FileWriter(ff, true));

				flowWriter.write(Integer.toString(aFlow.id));
				flowWriter.write(", ");
				flowWriter.write(aFlow.srcIP + ":" + aFlow.srcPort);
				flowWriter.write(", ");
				flowWriter.write(aFlow.destIP + ":" + aFlow.destPort);
				flowWriter.write(", ");
				flowWriter.write(Long.toString(aFlow.incoming.lastByteRecv - aFlow.incoming.firstSequence));
				flowWriter.write(", ");
				flowWriter.write(Long.toString(aFlow.outgoing.lastByteRecv - aFlow.outgoing.firstSequence));
				flowWriter.write(", ");
				flowWriter.write("dup::" + Integer.toString(aFlow.num_dupAck));
				flowWriter.write(", ");
				flowWriter.write("out::" + Integer.toString(aFlow.num_outOfOrder));
				flowWriter.write(", ");
				flowWriter.write(twoDP.format(avgThroughput));
				flowWriter.write('\n');
				flowWriter.close();
			}
			
			catch (IOException e){
				System.out.println("Writing flow file error");
				System.out.println(e.getMessage());
			}
		}
		
	}
	/**
	 * write out the stat for the flow and update the stat for the trace
	 * @param ci connectionInfo
	 * @param aFlow flow information
	 */
	void cleanUp(ConnectionInfo ci, Flow aFlow)
	{
		printFlow(aFlow);
		activeConnections.remove(ci);
		ended ++;
	}
	
	/**
	 * to be removed, for debugging purpose
	 */
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
				case TERMINATED: buckets[7] ++;
					break;
				case STRAY: buckets[8] ++;
					break;
			}
		}
		System.out.printf("init: %d; sync: %d; sync_ack: %d; ack: %d, data: %d, fin: %d, fin_ack: %d, terminiated: %d, stray: %d\n", 
							buckets[0], buckets[1],buckets[2],buckets[3],buckets[4],buckets[5],buckets[6],buckets[7],buckets[8]);
	}
	public static void main(String args[])
	{
		TCPProcesser tp = new TCPProcesser();
		tp.readTrace(tp.filename);
		System.out.println("Started: " + tp.started);
		System.out.println("Ended: " + tp.ended);
		System.out.println("left overs: " + tp.leftovers());
		tp.printLeftOverStates();
		tp.printTrace();
		
	}
}
