import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

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
	public String filename = "";
	public String path = "";
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
	public void setFilename(String filename)
	{
		this.filename = filename;
		if(filename.contains("/"))
		{
			this.path = filename.substring(0,filename.lastIndexOf('/') + 1);
		}
		else
			this.path ="./";
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
		File tf = new File(path + "traces.csv");
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
		
			BigDecimal avgThroughput = new BigDecimal(aFlow.dataLength);
			avgThroughput.multiply(new BigDecimal(8));
			long timetaken = aFlow.timeEnded - aFlow.timeStarted;
			avgThroughput.divide(new BigDecimal(timetaken),BigDecimal.ROUND_HALF_DOWN);
			
			BufferedWriter flowWriter = null;
			String tracename = filename.substring(filename.lastIndexOf('/')+1);
			
			File ff = new File(path + "flows_"+tracename+".csv");
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
				flowWriter.write(Integer.toString(aFlow.num_dupAck));
				flowWriter.write(", ");
				flowWriter.write(Integer.toString(aFlow.num_outOfOrder));
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
	

	public static void main(String args[])
	{
		TCPProcesser tp = new TCPProcesser();
		tp.setFilename(args[0]);
		tp.readTrace(tp.filename);
		tp.printTrace();
		
	}
}
