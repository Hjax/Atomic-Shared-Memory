package testing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import client.Client;
import util.ByteArray;
import util.Message;
import util.Server;


/*
   Test Commands:
   To create a client:					"newclient" *clientName* *pcId* *port* *serverSet* *xpos* *ypos*
   To create a server:					"newserver" *serverName* *IpAddress* *port*
   To create a serverSet:				"newserverset" *serversetName*
   To add a server to a serverSet:		"addserverset" *serverSetName* *server*
   To add a server to a client:			"addserver" *clientName* *serverName*
   To remove a server from a client:	"removeserver" *clientName* *serverName*
   To read:								"read" *clientName* *key*
   To oh-SAM read:						"ohsamread" *clientName* *key*
   To write:							"write" *clientName* *key* *value*
   To oh-SAM write:						"ohsamwrite" *clientName* *key* *value*
   
   Manager Commands:
   To set the port to use for manager:	"managerport" *port#*
   To set the PCid to use for manager:	"managerpcid" *pcid*
   To set a location for a server:		"setloc" *serverName* *xfloat* *yfloat*
   To set drop rate for a server:		"drop" *drop%* *serverName*
   To set drop rate for server set:		"dropset" *drop%* *serverName*
   To kill a server:					"kill" *serverName*
   To kill all servers in a set:		"killset" *serverSetName*
   To revive a server:					"revive" *serverName*
   To revive all servers in a set:		"reviveset" *serverSetName*
   
   To set location for a client:		"clientloc" *client* *xfloat* *yfloat*
   To set droprate for a client:		"clientdrop" *client* *droprate*
   
   To do a reliable read:				"reliableread" *server* *key*
   
   To exit:								"end"
 */

// Make sure anything that the commands can throw is listed here
interface Command {
	void run(String[] args) throws NumberFormatException, UnknownHostException, IOException;
}

public class DynamicRuntimeTests {
	// An elegant mapping of commands to the method to run them
	private static final Map<String, Command> commands = new HashMap<>();
	static {
		commands.put("read",			(String[] args)		-> 	read(args));
		commands.put("ohsamread",		(String[] args)		-> 	ohsamRead(args));
		commands.put("write",			(String[] args)		-> 	write(args));
		commands.put("ohsamwrite",		(String[] args)		-> 	ohsamWrite(args));
		commands.put("addserver",		(String[] args)		-> 	addServer(args));
		commands.put("newserver",		(String[] args)		-> 	createServer(args));
		commands.put("newclient",		(String[] args)		-> 	createClient(args));
		commands.put("addserverset",	(String[] args)		-> 	addServerSet(args));
		commands.put("removeserver",	(String[] args)		-> 	removeServer(args));
		commands.put("newserverset",	(String[] args)		-> 	createServerSet(args));
		commands.put("manageclient",	(String[] args)		-> 	manageClient(args));
		commands.put("managerport",		(String[] args)		-> 	outport(args));
		commands.put("managerpcid",		(String[] args)		-> 	pcid(args));
		commands.put("setloc",			(String[] args)		-> 	setloc(args));
		commands.put("kill",			(String[] args)		-> 	kill(args));
		commands.put("killset",			(String[] args)		-> 	killSet(args));
		commands.put("revive",			(String[] args)		-> 	revive(args));
		commands.put("reviveset",		(String[] args)		-> 	reviveSet(args));
		commands.put("drop",			(String[] args)		-> 	drop(args));
		commands.put("dropset",			(String[] args)		-> 	dropSet(args));
		commands.put("clientloc",		(String[] args)		-> 	clientLoc(args));
		commands.put("clientdrop",		(String[] args)		-> 	clientDrop(args));
		commands.put("reliableread",	(String[] args)		-> 	reliableRead(args));
		
		
	}
	//NOT CASE SENSITIVE

	private static HashMap<String, Client> clients = new HashMap<String, Client>(1);
	private static HashMap<String, Server> servers = new HashMap<String, Server>(10);
	private static HashMap<String, HashSet<Server>> serverSet = new HashMap<String, HashSet<Server>>(2);
	private static long timer;
	private static int outport = 1998;
	private static DatagramSocket socket;
	private static int reqID = 0;
	private static int pcID = 99;
	
	private static void pcid(String[] input)
	{
		pcID = (int)Integer.valueOf(input[1]);
	}
	
	private static void outport(String[] input) throws SocketException
	{
		try
		{
			socket.close();
		}
		catch(Exception e) {}
		outport = (int)Integer.valueOf(input[1]);
		socket = new DatagramSocket(outport);
	}
	
	private static void setloc(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":set-location:" + pcID + ":" + input[2] + ":" + input[3]).getBytes();
		DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, servers.get(input[1]).getAddress(), servers.get(input[1]).getPort());
		socket.send(packet);
	}
	
	private static void clientLoc(String[] input)
	{
		clients.get(input[1]).setLoc(Float.parseFloat(input[2]), Float.parseFloat(input[3]));
	}
	
	private static void kill(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":wait").getBytes();
		DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, servers.get(input[1]).getAddress(), servers.get(input[1]).getPort());
		socket.send(packet);
	}
	
	private static void killSet(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":wait").getBytes();
		for (Server server:serverSet.get(input[1]))
		{
			DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, server.getAddress(), server.getPort());
			socket.send(packet);
		}
	}
	
	private static void revive(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":wake").getBytes();
		DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, servers.get(input[1]).getAddress(), servers.get(input[1]).getPort());
		socket.send(packet);
	}
	
	private static void reviveSet(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":wake").getBytes();
		for (Server server:serverSet.get(input[1]))
		{
			DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, server.getAddress(), server.getPort());
			socket.send(packet);
		}
	}
	
	private static void drop(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":drop:" + input[1]).getBytes();
		DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, servers.get(input[2]).getAddress(), servers.get(input[2]).getPort());
		socket.send(packet);
	}
	
	private static void dropSet(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":drop:" + input[1]).getBytes();
		for (Server server:serverSet.get(input[2]))
		{
			DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, server.getAddress(), server.getPort());
			socket.send(packet);
		}
	}
	
	private static void clientDrop(String[] input)
	{
		clients.get(input[1]).setDroprate(Integer.parseInt(input[2]));
	}
	
	private static void reliableRead(String[] input) throws IOException {
		Message response;
		DatagramPacket packet;
		byte[] messageBytes = (reqID++ + ":reliable-read:" + pcID + ":0.0:0.0:" + input[2]).getBytes();
		packet = new DatagramPacket(messageBytes, messageBytes.length, servers.get(input[1]).getAddress(), servers.get(input[1]).getPort());
		socket.send(packet);
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
			if (response.getFlag().equals("read-return"))
			{
				System.out.printf("[r]\t%s -> %s\n", input[2], response.getValue());
				return;
			}
		}
	}
	
	private static void read(String[] input) throws IOException {
		System.out.println("[r]\t" + input[2] + "->" + clients.get(input[1]).read(input[2]));
	}
	
	private static void ohsamRead(String[] input) throws IOException {
		System.out.println("[r]\t" + input[2] + "->" + clients.get(input[1]).ohsamRead(input[2]));
	}
	
	private static void write(String[] input) throws IOException {
		clients.get(input[1]).write(input[2], input[3]);
		System.out.println("[w]\t" + input[2] + "->" + input[3]);
	}
	
	private static void ohsamWrite(String[] input) throws IOException {
		clients.get(input[1]).ohsamWrite(input[2], input[3]);
		System.out.println("[w]\t" + input[2] + "->" + input[3]);
	}
	
	private static void addServer(String[] input) {
		clients.get(input[1]).addServer(servers.get(input[2]));
		System.err.println("[i]\tServer Added To Client:\t" + input[2] + "->" + input[1]);
	}
	
	private static void createServer(String[] input) throws NumberFormatException, UnknownHostException {
		servers.put(input[1], new Server(InetAddress.getByName(input[2]), (int)Integer.valueOf(input[3])));
		System.err.println("[i]\tServer Created: \t" + input[1]);
	}
	
	private static void createClient(String[] input) {
		clients.put(input[1], new Client((int)Integer.valueOf(input[2]), (int)Integer.valueOf(input[3]), serverSet.get(input[4]), Float.valueOf(input[5]), Float.valueOf(input[6])));
		System.err.println("[i]\tClient Created: \t" + input[1]);
	}
	
	private static void addServerSet(String[] input) {
		serverSet.get(input[1]).add(servers.get(input[2]));
		System.err.println("[i]\tServer Added To Set\t" + input[2] + "->" + input[1]);
	}
	
	private static void removeServer(String[] input) {
		clients.get(input[1]).removeServer(servers.get(input[2]));
		System.err.println("[i]\tServer Removed From Client:\t" + input[2] + "-<" + input[1]);
	}
	
	private static void createServerSet(String[] input)	{
		serverSet.put(input[1], new HashSet<Server>(5));
		System.err.println("[i]\tServerSet Created: \t" + input[1]);
	}
	
	private static void manageClient(String[] input) throws SocketException
	{
		clients.get(input[1]).beManaged();
	}
	
	
	public static void parseCommand(String input) throws NumberFormatException, UnknownHostException, IOException {
		String[] commandParsed = input.split(" ");
		commands.get(commandParsed[0]).run(commandParsed);
	}

	public static void runDynamicTests() throws UnknownHostException, SocketException 
	{
		try
		{
		socket = new DatagramSocket(outport);
		}
		catch (Exception e) {}
		Scanner input = new Scanner(System.in);
		String command;
		String[] commandParsed;
		while (true)
		{
			command = input.nextLine();
			command.toLowerCase();
			if (command.equals("end"))
			{
				input.close();
				return;
			}
			commandParsed = command.split(" ");
			try {
				timer = System.nanoTime();
				commands.get(commandParsed[0]).run(commandParsed);
				System.err.printf("Took %d ms\n", (System.nanoTime() - timer)/1000000);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.err.println("[e]\tFailed to convert port value to integer");
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.err.println("[e]\tCould not find host");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("[e]\tAn IO exception has occurred");
			} catch (Exception e) {
				if (!commands.containsKey(commandParsed[0])) System.err.println("[e]\tCommand not found");
				else System.err.println("[e]\tBad Input in " + commandParsed[0]);
				e.printStackTrace();
			}
		}
			
	}
}