package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import dataserver.MemoryDataServer;
import util.Address;

/**
 * 
 * This Java Project includes an implementation of a server that receives and sends messages (through 
 * abstract methods) and stores data (through abstract methods). The environment this project is meant to
 * be run is a testing environment that includes a client or several clients that can manipulate the meta
 * state of the server. This meta state includes: awake/asleep; simulated location (and therefore ping); 
 * simulated packet loss for received messages; simulated packet loss going to the client (which should be
 * kept consistent across all servers for consistent results); the other servers in the network this server
 * knows about (which should not be changed while the server is actively handling messages); and the
 * ability to clear the contents of the server. See MessageParser class for the specifics on these messages.
 * 
 * 
 * 
 * @author Christian
 *
 */
public class Main {
	
	public static void main(String[] args) {
		
		// address the developer last had on their machine so they could run the project from the IDE and not
		// the terminal
		
		String arg;
		
		if (args.length > 0)
			arg = args[0];
		// this else happens if you're running it without any arguments
		// this just sets a server to run on localhost at port 2000
		else {
			args = new String[2];
			arg = "-server";
			args[1] = "localhost:2000";
		}
		
		if (arg.equals("-server")) {
			
			String address = args[1].split(":")[0];
			int port = Integer.parseInt(args[1].split(":")[1]);
			
			String[] addresses;
			
			if (args.length > 2)
				addresses = args[2].split(";");
			else
				addresses = null;
			
			process(address, port, addresses);
			
		}
		else if (arg.equals("-bats")){
			
			File directory = new File(args[1]);
			
			File srcDirectory = new File(args[2]);
			
			File out;
			PrintWriter printer = null;
			
			for (int i = 3; i < args.length; i++) {
				try {
					
					out = new File(directory, "server_" + (i - 3) + ".bat");
				
					if (out.exists())
						out.delete();
					
					out.createNewFile();
					
					System.out.println("Making file " + out + "...");
					
					printer = new PrintWriter(new FileWriter(out));
					
					printer.println("title server_" + (i - 3));
					printer.println("cd " + srcDirectory);
					printer.print("java -cp bin main.Main -server " + args[i] + " ");
					
					String append = "";
					
					// appends all the addresses that are not the address of the server
					for (int j = 3; j < args.length; j++)
						if (i != j)
							append = append + ";" + args[j];
					
					printer.println(append.substring(1));
					printer.print("pause");
					
					printer.close();
				} catch (IOException e) {
					if (printer != null)
						printer.close();
					e.printStackTrace();
					continue;
				}
				
			}
			
		}
		else if (arg.equals("-help") || arg.equals("-h") || arg.equals("help")) {
			System.out.println(helpString());
			System.exit(0);
			return;
		}
		else {
			System.out.println(helpString());
			System.out.println("You entered a bad input! Try again.");
		}

	}
	
	private static Address getAddressFromPair(String pair) {
		InetAddress inet;
		int port;
		
		if (pair.length() == 0)
			return null;
		
		String[] parts = pair.split(":");
		try {
			inet = InetAddress.getByName(parts[0]);
		} catch (UnknownHostException e) {
			System.out.println("ERROR: '" + parts[0] + "' is not a valid IP Address");
			return null;
		}
		port = Integer.parseInt(parts[1]);
		
		return new Address(inet, port);
	}
	
	/**
	 * This (poorly named) method starts everything up: the server, the message listener thread, all of it
	 * @param address
	 * @param port
	 * @param addressesStr
	 */
	public static void process(String address, int port, String[] addressesStr) {
		
		MemoryDataServer server;
		
		
		Address[] addresses;
		
		// if we provided this server addresses to add on initialization, add them
		if (addressesStr != null) {
			
			addresses = new Address[addressesStr.length];
			
			for (int i = 0; i < addresses.length; i++)
				addresses[i] = getAddressFromPair(addressesStr[i]);
			
		}
		// there's also the option of not adding servers, in which case we add none
		else
			addresses = null;
		
		
		
		
		try {
			// server id is 0 because client does not currently distinguish between servers using an id
			int server_id = 0;
			
			server = new MemoryDataServer(server_id, port, address, addresses);
			
			Thread serverThread = server.startMessageListenerThread();
			
			// if the server thread is done, then our server is done
			// it's on a while(true) loop though, so good luck with that
			serverThread.join();
			
			// shut it down when we're done
			server.close();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
			// socket was already bound; try again with the next socket
			process(address, port + 1, addressesStr);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static String helpString() {
		String out =
				"Run this program from the Atomic-Shared-Memory/server directory in the form: java -cp bin main.Main <-server | -make-bats | -h>"
						+ "\n\t" + "-help	:	display this help message"
						+ "\n\t" + "-server	:	server_ip:server_port other_known_server_ip_1:other_known_server_port1;other_known_server_ip_2;other_known_server_port_2;..."
						+ "\n\t" + "-server	:	server_ip:server_port"
						+ "\n\t" + "			Providing no list of server ips to teach it creates a server that does not know about any other server in the network"
						+ "\n\t" + "-bats	:	<srcDirectory> <directory> <ip1>:<port1> <ip2>:<port2> ..."
						+ "\n\t" + "			This command is how you can generate all the .bat files you need for running all servers, provided you have all the IPs."
						+ "\n\t" + "			Note that if you are using this codebase for simulation and testing and will be adding/dropping servers, you can "
						+ "\n\t" + " 			simply use add-server and remove-server messages from the client."
						+ "\n\t" + "			<srcDirectory> should be where you want all the .bat files to go."
						+ "\n\t" + "			<directory> should be the absolute path of the Atomic-Shared-Memory/server directory"
						+ "\n\t" + "<noArgs>:	starts a server on localhost at port 2000 without any other known servers"; 
		
		return out;	
	}
}
