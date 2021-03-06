package client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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
	int resendDelay = 5000;
	Random rng = new Random();
	
	public Client(int PCID, int PORT, HashSet<Server> SERVERS)
	{
		pcid = PCID;
		port = PORT;
		Iterator<Server> serverIterator = SERVERS.iterator();
		while (serverIterator.hasNext())
		{
			servers.add(serverIterator.next());
		}
		
	}
	
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
	
	public void addServer(Server SERVER)	//VIOLATES ATOMICITY - USE WITH CARE
	{
		servers.add(SERVER);
	}
	
	public void removeServer(Server SERVER)		//for oh-SAM/oh-MAM, the servers must also be informed
	{
		removeServerFromSet(SERVER, servers);
	}
	

	private boolean removeServerFromSet(Server SERVER, HashSet<Server> SERVERSET)	//DEPRECATED
	{
		if (SERVERSET.contains(SERVER))
		{
			SERVERSET.remove(SERVER);
			return true;
		}
		return false;
		//System.err.printf("[i]	failed to remove server\n");
	}
	
	//blocks until a management message is received. For testing purposes
	public void beManaged() throws SocketException
	{
		DatagramSocket socket = new DatagramSocket(port);
		Message response;
		DatagramPacket packet;
		while (true)
		{
			packet = new DatagramPacket(new byte[1024], 1024);
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
			catch(Exception e)
			{
				e.printStackTrace();
				continue;
			}
			try {response = new Message(ByteArray.parseToString(packet.getData()));}
			catch (Exception e)
			{
				socket.close();
				e.printStackTrace();
				throw e;
			}
			if (response.getFlag().equals("set-location"))
			{
				xpos = response.getXVal();
				ypos = response.getYVal();
				System.err.printf("[i]\treceived a setloc command.\n");
				socket.close();
				return;
			}
			else if (response.getFlag().equals("drop"))
			{
				droprate = response.getDroprate();
				System.err.printf("[i]\treceived a drop rate command.\n");
				socket.close();
				return;
			}
			else if (response.getFlag().equals("kill"))
			{
				System.err.printf("[i]\treceived a kill command for some reason.\n");
				socket.close();
				return;
			}
		}
		
	}
	
	public void setLoc(float newX, float newY)		//sets client's simulated position for simulated ping
	{
		xpos = newX;
		ypos = newY;
	}
	
	public void setDroprate(int newRate)		//sets client's simulated packet drop rate
	{
		droprate = newRate;
	}
	
	public void setResendDelay(int newDelay)	//sets how long client will wait for responses before assuming packet loss and resending
	{
		resendDelay = newDelay;
	}
	
	//the public read command that gets a value from the servers
	public String read(String key) throws IOException
	{
		reqID++;
		Message value = readMessage(key);
		writeMessage(key, value.getValue(), value.getSeqID() + 1);
		return value.getValue();
	}
	
	//the public write command that writes a value to the servers following multiple-writer protocol
	public void write(String key, String value) throws IOException
	{
		reqID++;
		Message seqID = readMessage(key);
		writeMessage(key, value, seqID.getSeqID() + 1);
	}
	
	//the public write command that writes a value to the servers following single-writer protocol
	public void singleWrite(String key, String value) throws IOException
	{
		reqID++;
		writeMessage(key, value, reqID);
	}
	
	//duplicate of write method
	public void ohmamWrite(String key, String value) throws IOException
	{
		reqID++;
		Message seqID = readMessage(key);
		writeMessage(key, value, seqID.getSeqID() + 1);
	}
	
	
	//duplicate of singleWrite method
	public void ohsamWrite(String key, String value) throws IOException
	{
		reqID++;
		writeMessage(key, value, reqID);
	}
	
	
	//public command that performs and oh-SAM/oh-MAM read
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
		
		//send the requests and set awaitingSet = serverSet
		HashSet<Server> awaitingSet = sendRequests(messageBytes, socket);
		
		//wait for and read responses for most recent seqId
		socket.setSoTimeout(resendDelay);
		Message returnMessage = getResponses(socket, awaitingSet, messageBytes, 1);
		socket.close();
		return returnMessage;
	}
	
	//specifically writes to the servers. Used privately by both the read and write functions
	private void writeMessage(String key, String value, int seqId) throws IOException
	{
		DatagramSocket socket = new DatagramSocket(port);
		byte[] messageBytes = (reqID + ":" + "write-request:" + pcid + ":" + String.valueOf(xpos) + ":" + String.valueOf(ypos) + ":" + seqId + ":" + key + ":" + value).getBytes();	
		
		//send the requests and set awaitingSet = serverSet
		HashSet<Server> awaitingSet = sendRequests(messageBytes, socket);
		
		socket.setSoTimeout(resendDelay);
		//wait for majority responses
		getResponses(socket, awaitingSet, messageBytes, 0);
		socket.close();
	}
	
	//Specifically does an OHSAM read. Used privately by the ohsamRead method
	private Message ohsamReadMessage(String key) throws IOException
	{
		DatagramSocket socket = new DatagramSocket(port);
		byte[] messageBytes = (reqID + ":" + "ohsam-read-request:" + pcid + ":" + String.valueOf(xpos) + ":" + String.valueOf(ypos) + ":" + key).getBytes();
		
		//send the requests and set awaitingSet = serverSet
		HashSet<Server> awaitingSet = sendRequests(messageBytes, socket);
		
		//wait for and read responses for most recent seqId
		socket.setSoTimeout(resendDelay);
		Message returnMessage = getResponses(socket, awaitingSet, messageBytes, 2);
		//System.err.printf("%s\n",returnMessage.formatMessage());
		socket.close();
		return returnMessage;
	}
	

	//used by writeMessaged and readMessage
	//sends a message to all known servers, responds a duplicate of the known servers set
	private HashSet<Server> sendRequests(byte[] messageBytes, DatagramSocket socket) throws IOException
	{
		DatagramPacket packet;
		HashSet<Server> awaitingSet = new HashSet<Server>(servers.size());
		Iterator<Server> serverIterator = servers.iterator();
		Server destinationServer;
		while (serverIterator.hasNext())
		{
			destinationServer = serverIterator.next();
			awaitingSet.add(destinationServer);
			packet = new DatagramPacket(messageBytes, messageBytes.length, destinationServer.getAddress(), destinationServer.getPort());
			socket.send(packet);
		}
		return awaitingSet;
	}
	

	//used by writeMessage and readMessage
	//operation == 0: write
	//operation == 1: ABD read
	//operation == 2: oh-SAM read
	private Message getResponses(DatagramSocket socket, HashSet<Server> awaitingSet, byte[] messageBytes, int operation) throws IOException
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
			System.err.printf("about to wait for responses to a thing\n");
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
				System.err.printf("found a packet, reqID = %d\n", response.getReqID());
				//if packet is a management packet, handle it appropriately
				if (response.getFlag().equals("set-location"))
				{
					xpos = response.getXVal();
					ypos = response.getYVal();
				}
				else if (response.getFlag().equals("drop"))
				{
					droprate = response.getDroprate();
				}
				else if (response.getFlag().equals("kill"))
				{
					System.err.printf("received a kill command for some reason. Ignoring it\n");
				}
				else if (response.getReqID() == reqID && rng.nextInt(100) >= droprate)			//check reqID to make sure it isn't an old request
				{
					System.err.printf("got a response from: Address=%s port=%d\n", packet.getAddress().getHostAddress(), packet.getPort());
					
					System.err.printf(response.formatMessage() + "\n");
					
					receivedServer = new Server(packet.getAddress(), packet.getPort());
					if (removeServerFromSet(receivedServer, awaitingSet))
					{
					    //-10 is the default seqID indicating no response has been received yet
						//ABD reads take the largest seqID
						if (operation == 1 && (bestResponse.getSeqID() == -10 || (response.getSeqID() > bestResponse.getSeqID() || (response.getSeqID() == bestResponse.getSeqID() && response.getPcID() >= bestResponse.getPcID()))))
						{
							bestResponse = new Message(response.formatMessage());
							//System.err.printf("updating best response\n");
						}
					    //oh-SAM reads take the smallest seqID
						if (operation == 2 && (bestResponse.getSeqID() == -10 || (response.getSeqID() < bestResponse.getSeqID() || (response.getSeqID() == bestResponse.getSeqID() && response.getPcID() >= bestResponse.getPcID()))))
						{
							bestResponse = new Message(response.formatMessage());
							//System.err.printf("updating best response\n");
						}
						i++;
					}
					else
					{
						System.err.printf("response was a duplicate\n");
					}
				}
				
			}
			else	//timed out without finding a packet, so retransmit to remaining servers
			{
				Iterator<Server> resendIterator = servers.iterator();
				while (resendIterator.hasNext())
				{
					destinationServer = resendIterator.next();
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, destinationServer.getAddress(), destinationServer.getPort());}
						catch (RuntimeException e)
						{
							socket.close();
							throw new RuntimeException("ERROR - failed to resend\n");
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
