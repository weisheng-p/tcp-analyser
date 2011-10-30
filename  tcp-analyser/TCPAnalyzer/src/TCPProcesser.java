import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import jpcap.JpcapCaptor;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import util.SimpleMap;
import TCP.ConnectionInfo;
import TCP.Flow;
import TCP.PacketInfo;


public class TCPProcesser {
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
			if(aFlow.current == Flow.State.TERMINIATED)
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
			long maxWindowSize =aFlow.srcWindow.maxWindowSize; //1 * 8; //in bits
			double rtt = aFlow.incomingRTT;//0.3; //convert ms to s. 
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
				flowWriter.write(Long.toString(aFlow.srcWindow.getLastExpectedSeqNum()));
				flowWriter.write(", ");
				flowWriter.write(Long.toString(aFlow.destWindow.getLastExpectedSeqNum()));
				flowWriter.write(", ");
				flowWriter.write(Integer.toString(aFlow.num_dupAck));
				flowWriter.write(", ");
				flowWriter.write(Integer.toString(aFlow.num_outOfOrder));
				flowWriter.write(", ");
				double avgThroughput = 0.75 * maxWindowSize * rtt;
				//avgThroughput = 0.75 * maxWindowSize (in bits so * 8) * RTT (in sec)
				flowWriter.write(Double.toString(avgThroughput));
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
	 * @param ci
	 * @param aFlow
	 */
	void cleanUp(ConnectionInfo ci, Flow aFlow)
	{
		printFlow(aFlow);
		//printTrace();
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
