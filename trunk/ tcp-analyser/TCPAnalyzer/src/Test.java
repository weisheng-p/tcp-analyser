import java.io.IOException;

import jpcap.*;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

public class Test {
	public static void main(String arg[]) throws IOException
	{
		String filename = "/home/weisheng/Desktop/trace/trace1";
		JpcapCaptor captor=JpcapCaptor.openFile(filename);
		TCPPacket tcppacket;
		int count = 0;
		int count2 = 0;
		while(true)
		{
			  //read a packet from the opened file
			  Packet packet=captor.getPacket();
			  //if some error occurred or EOF has reached, break the loop
			  if(packet==null || packet==Packet.EOF) break;
			  //otherwise, print out the packet
			  if(packet instanceof TCPPacket)
			  {
				  count ++;
			  }
			  count2 ++;
			  
		}

		captor.close();
		System.out.println("Found " + count + " tcppackets out of " + count2 + " packets");
	}
}
