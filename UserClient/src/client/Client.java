package client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import util.ByteArray;
import util.Message;
import util.Server;

public class Client {
	int pcid;	//save the pcid of the machine in the class
	HashSet<Server> servers = new HashSet<Server>(5);	//set of Servers. Can be modified with functions
	int port = 2000;	//port to use for everything
	int reqID = 0;
	float xpos = 0;
	float ypos = 0;
	int droprate = 0;
	Random rng = new Random();
	
	public Client(int PCID, int PORT, HashSet<Server> SERVERS, float XPOS, float YPOS)
	{
		pcid = PCID;
		port = PORT;
		xpos = XPOS;
		ypos = YPOS;
		Iterator<Server> serverIterator = SERVERS.iterator();
		while (serverIterator.hasNext())
		{
			servers.add(serverIterator.next());
		}
		
	}
	
	public void addServer(Server SERVER)
	{
		servers.add(SERVER);
	}
	
	public void removeServer(Server SERVER)
	{
		removeServerFromSet(SERVER, servers);
	}
	

	private void removeServerFromSet(Server SERVER, HashSet<Server> SERVERSET)
	{
		Iterator<Server> removeIterator = SERVERSET.iterator();
		Server checkServer;
		while (removeIterator.hasNext())
		{
			checkServer = removeIterator.next();
			if (checkServer.equals(SERVER))
			{
				SERVERSET.remove(checkServer);
				return;
			}
		}
	}
	
	//the public read command that gets a value from the servers
	public String read(String key) throws IOException
	{
		reqID++;
		Message value = readMessage(key);
		writeMessage(key, value.getValue(), value.getSeqID() + 1);
		return value.getValue();
	}
	
	//the public write command that writes a value to the servers
	public void write(String key, String value) throws IOException
	{
		reqID++;
		Message seqID = readMessage(key);
		writeMessage(key, value, seqID.getSeqID() + 1);
	}
	
	public void ohsamWrite(String key, String value) throws IOException
	{
		reqID++;
		System.out.printf("about to read before ohsam-writing\n");
		Message seqID = readMessage(key);
		System.out.printf("about to ohsam-write\n");
		//System.out.printf("Final seqID is %d\n", seqID.getSeqID());
		writeMessage(key, value, seqID.getSeqID() + 1);
	}
	
	public String ohsamRead(String key) throws IOException
	{
		reqID++;
		return ohsamReadMessage(key).getValue();
	}
	
	//specifically reads from the servers. Used privately by both the read and write functions
	private Message readMessage(String key) throws IOException
	{
		DatagramSocket socket = new DatagramSocket(port);
		byte[] messageBytes = (reqID + ":" + "read-request:" + pcid + ":" + String.valueOf(xpos) + ":" + String.valueOf(ypos) + ":" + key).getBytes();
		
		//send the requests and set resendSet = serverSet
		HashSet<Server> resendSet = sendRequests(messageBytes, socket);
		
		//wait for and read responses for most recent seqId
		socket.setSoTimeout(5000);	//TODO : get better timeout duration
		Message returnMessage = getResponses(socket, resendSet, messageBytes, 1);
		socket.close();
		return returnMessage;
	}
	
	//specifically writes to the servers. Used privately by both the read and write functions
	private void writeMessage(String key, String value, int seqId) throws IOException
	{
		DatagramSocket socket = new DatagramSocket(port);
		byte[] messageBytes = (reqID + ":" + "write-request:" + pcid + ":" + String.valueOf(xpos) + ":" + String.valueOf(ypos) + ":" + seqId + ":" + key + ":" + value).getBytes();	
		
		//send the requests and set resendSet = serverSet
		HashSet<Server> resendSet = sendRequests(messageBytes, socket);
		
		socket.setSoTimeout(5000);	//TODO : get better timeout duration
		//wait for majority responses
		getResponses(socket, resendSet, messageBytes, 0);
		socket.close();
	}
	
	//Specifically does an OHSAM read. Used privately by the ohsamRead method
	private Message ohsamReadMessage(String key) throws IOException
	{
		DatagramSocket socket = new DatagramSocket(port);
		byte[] messageBytes = (reqID + ":" + "ohsam-read-request:" + pcid + ":" + String.valueOf(xpos) + ":" + String.valueOf(ypos) + ":" + key).getBytes();
		
		//send the requests and set resendSet = serverSet
		HashSet<Server> resendSet = sendRequests(messageBytes, socket);
		
		//wait for and read responses for most recent seqId
		socket.setSoTimeout(5000);	//TODO : get better timeout duration
		Message returnMessage = getResponses(socket, resendSet, messageBytes, 2);
		//System.out.printf("%s\n",returnMessage.formatMessage());
		socket.close();
		return returnMessage;
	}
	

	//used by writeMessaged and readMessage
	private HashSet<Server> sendRequests(byte[] messageBytes, DatagramSocket socket) throws IOException
	{
		DatagramPacket packet;
		HashSet<Server> resendSet = new HashSet<Server>(servers.size());
		Iterator<Server> serverIterator = servers.iterator();
		Server destinationServer;
		while (serverIterator.hasNext())
		{
			destinationServer = serverIterator.next();
			resendSet.add(destinationServer);
			packet = new DatagramPacket(messageBytes, messageBytes.length, destinationServer.getAddress(), destinationServer.getPort());
			socket.send(packet);
		}
		return resendSet;
	}
	

	//used by writeMessage and readMessage
	//operation == 0: write
	//operation == 1: read
	//operation == 2: oh-SAM
	private Message getResponses(DatagramSocket socket, HashSet<Server> resendSet, byte[] messageBytes, int operation) throws IOException
	{
		int i = 0;
		boolean timeout = false;
		Server receivedServer;
		Message response;
		DatagramPacket packet;
		Server destinationServer;
		Message bestResponse = new Message(String.valueOf(reqID) + ":read-return:" + String.valueOf(pcid) + ":" + String.valueOf(xpos) + ":" + String.valueOf(ypos) + ":-10:0");
		
		
		while (i < (servers.size() / 2) + 1) //address.length / 2 is an int and should self-truncate
		{
			packet = new DatagramPacket(new byte[1024], 1024);
			timeout = false;
			System.out.printf("about to wait for responses to a thing\n");
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
				catch (SocketTimeoutException e)
				{
					timeout = true;
				}
			if (!timeout)	//found a packet
			{
				
				try
					{response = new Message(ByteArray.parseToString(packet.getData()));}
				catch (Exception e)
				{
					socket.close();
					e.printStackTrace();
					throw e;
				}
				System.out.printf("found a packet, reqID = %d\n", response.getReqID());
				if (response.getFlag() == "set-location")
				{
					xpos = response.getXVal();
					ypos = response.getYVal();
				}
				else if (response.getFlag() == "drop")
				{
					droprate = response.getDroprate();
				}
				else if (response.getFlag() == "kill")
				{
					System.out.printf("received a kill command for some reason. Ignoring it\n");
				}
				else if (response.getReqID() == reqID && rng.nextInt(100) >= droprate)
				{
					System.out.printf("got a response\n");
					
					System.out.printf(response.formatMessage() + "\n");
					
					//TODO: MAKE THIS NOT HORRIBLE
					receivedServer = new Server(packet.getAddress(), packet.getPort());
					removeServerFromSet(receivedServer, resendSet);
					
					//track most recent data
					//System.out.printf("seqid from response is %d\n", response.getSeqID());
					if (operation == 1 && (bestResponse.getSeqID() == -10 || (response.getSeqID() > bestResponse.getSeqID() || (response.getSeqID() == bestResponse.getSeqID() && response.getPcID() >= bestResponse.getPcID()))))
					{
						bestResponse = new Message(response.formatMessage());
						//System.out.printf("updating best response\n");
					}
					
					if (operation == 2 && (bestResponse.getSeqID() == -10 || (response.getSeqID() < bestResponse.getSeqID() || (response.getSeqID() == bestResponse.getSeqID() && response.getPcID() >= bestResponse.getPcID()))))
					{
						bestResponse = new Message(response.formatMessage());
						//System.out.printf("updating best response\n");
					}
					i++;
				}
				
			}
			else	//timed out without finding a packet, so retransmit to remaining servers
			{
				Iterator<Server> resendIterator = resendSet.iterator();
				while (resendIterator.hasNext())
				{
					destinationServer = resendIterator.next();
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, destinationServer.getAddress(), destinationServer.getPort());}
						catch (RuntimeException e)
						{
							socket.close();
							throw new RuntimeException("ERROR - resendSet smaller than expected\n");
						}
					try {
						socket.send(packet);
					} catch (IOException e) {
						socket.close();
						e.printStackTrace();
						throw e;
					}
				}
			}
			
		}
		return bestResponse;
	}
	
}
