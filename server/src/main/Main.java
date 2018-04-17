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
 * Version 1.0.0 - One-To-Many capable
 * Implemented a -h for command line running
 * Implemented sleep functions for testing
 * 		* wait puts the server to sleep until it is woken up
 * 		* wake wakes the server up from being asleep
 * 
 * Version 0.1.1
 * Ability to give the server an IP address from command line arguments
 * 
 * 
 * Version 0.1.0
 * Storage of data through memory, not disk
 * Reception of messages through listening to a port and receiving UDP packets
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
		
		String arg = args[0];
		
		if (arg.equals("-h") || arg.equals("-h") || arg.equals("help")) {
			System.out.println(helpString());
			System.exit(0);
			return;
		}
		else if (arg.equals("-server")) {
			
		
			String address = args[1].split(":")[0];
			int port = Integer.parseInt(args[1].split(":")[1]);
			String[] addresses = args[2].split(";");
			process(address, port, addresses);
		}
		else if (arg.equals("-make-bats")){
			File directory = new File(args[1]);
			File srcDirectory = new File(args[2]);
			
			
			for (int i = 3; i < args.length; i++) {
				try {
					
					File out = new File(directory, "server" + (i - 3) + ".bat");
				
					if (out.exists())
						out.delete();
					
					out.createNewFile();
					
					System.out.println("Making file " + out + "...");
					
					PrintWriter printer = new PrintWriter(new FileWriter(out));
					
					printer.println("cd " + srcDirectory);
					printer.print("java -cp bin main.Main -server " + args[i] + " ");
					
					String append = "";
					
					for (int j = 3; j < args.length; j++)
						if (i != j)
							append = append + ";" + args[j];
					
					printer.println(append.substring(1));
					
					printer.print("pause");
					
					printer.close();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				
			}
			
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
	public static void process(String address, int port, String[] addressesStr) {
		
		MemoryDataServer server;
		
		Address[] addresses = new Address[addressesStr.length];
		
		for (int i = 0; i < addresses.length; i++)
			addresses[i] = getAddressFromPair(addressesStr[i]);
		
		
		try {
			server = new MemoryDataServer(0, port, address, addresses);
			Thread serverThread = server.startMessageListenerThread();
			serverThread.join();
			server.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
			process(address, port + 1, addressesStr);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static String helpString() {
		String out =
				"Run this executable from the Atomic-Shared-Memory/server directory in the form: java -cp bin main.Main <-server | -make-bats | -h>"
						+ "\n" + "-h			:	display this help message"
						+ "\n" + "-server		:	server_ip:server_port other_known_server_ip_1:other_known_server_port1;other_known_server_ip_2;other_known_server_port_2;..."
						+ "\n" + "-make-bats	:	<srcDirectory> <directory> <ip1>:<port1> <ip2>:<port2> ..."
						+ "\n" + "					This command is how you can generate all the .bat files you need for running all servers, provided you have all the IPs."
						+ "\n" + "					<srcDirectory> should be where you want all the .bat files to go."
						+ "\n" + "					<directory> should be the absolute path of the Atomic-Shared-Memory/server directory";
		
		
		return out;	
	}
}
