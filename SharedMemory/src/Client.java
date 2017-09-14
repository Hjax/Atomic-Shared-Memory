import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;

public class Client {
	int pcid;	//save the pcid of the machine in the class
	InetAddress[] addresses;	//list of server addresses. Can be modified with functions (WIP)
	int port = 2000;	//port to use for everything
	
	Client(int PCID, int PORT, InetAddress[] ADDRESSES)
	{
		pcid = PCID;
		port = PORT;
		addresses = ADDRESSES.clone();
	}
	
	public String read(String key) throws IOException
	{
		String[] value = readMessage(key);
		writeMessage(key, value[2], Integer.parseInt(value[3]) + 1);
		return value[2];
	}
	
	public void write(String key, String value) throws IOException
	{
		String seqId = readMessage(key)[3];
		writeMessage(key, value, Integer.parseInt(seqId));
	}
	
	private String[] readMessage(String key) throws IOException
	{
		//send the request
		DatagramPacket packet;
		DatagramSocket socket = new DatagramSocket(port);
		String message = "read-request:" + pcid + ":" + key;
		byte[] messageBytes = message.getBytes();
		packet = new DatagramPacket(messageBytes, messageBytes.length, addresses[0], port);
		HashSet<InetAddress> resendSet = new HashSet<InetAddress>(addresses.length);
		for (int i = 0; i < addresses.length; i++)
		{
			resendSet.add(addresses[i]);
			packet = new DatagramPacket(messageBytes, messageBytes.length, addresses[i], port);
			socket.send(packet);
		}
		
		//wait for and read responses
		socket.setSoTimeout(100);	//TODO : get better timeout duration
		String[] response = new String[4];
		String value = "0";
		int maxSeq = -1;
		int maxPc = -1;
		
		for (int i = 0; i < (addresses.length / 2) + 1; i++) //address.length / 2 is an int and should self-truncate
		{
			packet = new DatagramPacket(null, 0);
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
			finally{}
			if (packet.getData() != null)	//found a packet
			{
				response[0] = new String(packet.getData());
				response = response[0].split(":");
				//TODO : validate somehow that the packet is indeed a response to this read and not a prior read/write
				resendSet.remove(packet.getAddress());
				//track most recent data
				if (Integer.parseInt(response[3]) > maxSeq || (Integer.parseInt(response[3]) == maxSeq && Integer.parseInt(response[1]) > maxPc))
				{
					value = new String(packet.getData());
					maxSeq = Integer.parseInt(response[3]);
					maxPc = Integer.parseInt(response[1]);
				}	
			}
			else	//timed out without finding a packet, so retransmit to remaining servers
			{
				Iterator<InetAddress> resendIterator = resendSet.iterator();
				for (int j = 0; j < resendSet.size(); j++)
				{
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, resendIterator.next(), port);}
					finally {new RuntimeException("ERROR - resendSet smaller than expected");}
					socket.send(packet);
				}
				i--;
			}
			
		}
		socket.close();
		return value.split(":");
	}
	
	public void writeMessage(String key, String value, int seqId) throws IOException
	{
		//send the request
		DatagramPacket packet;
		DatagramSocket socket = new DatagramSocket(port);
		String message = "write-request:" + pcid + ":" + key + ":" + value + ":" + seqId;	//TODO : figured out where seqId is supposed to come from
		byte[] messageBytes = message.getBytes();
		HashSet<InetAddress> resendSet = new HashSet<InetAddress>(addresses.length);
		for (int i = 0; i < addresses.length; i++)
		{
			resendSet.add(addresses[i]);
			packet = new DatagramPacket(messageBytes, messageBytes.length, addresses[i], port);
			socket.send(packet);
		}
		
		//wait for responses
		socket.setSoTimeout(100);	//TODO : get better timeout duration
		String[] response = new String[4];
		
		for (int i = 0; i < (addresses.length / 2) + 1; i++) //address.length / 2 is an int and should self-truncate
		{
			packet = new DatagramPacket(null, 0);
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
			finally{}
			if (packet.getData() != null)	//found a packet
			{
				response[0] = new String(packet.getData());
				response = response[0].split(":");
				//TODO : validate somehow that the packet is indeed a response to this write and not a prior read/write
				resendSet.remove(packet.getAddress());
			}
			else	//timed out without finding a packet, so retransmit to remaining servers
			{
				Iterator<InetAddress> resendIterator = resendSet.iterator();
				for (int j = 0; j < resendSet.size(); j++)
				{
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, resendIterator.next(), port);}
					finally {new RuntimeException("ERROR - resendSet smaller than expected");}
					socket.send(packet);
				}
				i--;
			}
			
		}
		socket.close();
	}
	
}
