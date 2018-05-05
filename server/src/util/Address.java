package util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This is a quality-of-life class that is simply a wrapper for IP/port pairs.
 * @author Christian
 *
 */
public class Address {
	
	InetAddress addr;
	int port;
	
	public Address(String addr, String port) {
		try {
			this.addr = InetAddress.getByName(addr);
			
			this.port = Integer.parseInt(port);
			
		} catch (UnknownHostException e) {
			System.out.println("ERROR: '" + addr + "' is not a valid IP Address");
		}
	}
	
	public Address(InetAddress addr, int port) {
		this.addr = addr;
		this.port = port;
	}
	
	
	
	public InetAddress addr() {
		return this.addr;
	}
	public int port() {
		return this.port;
	}
	
	@Override
	public String toString() {
		return this.addr + "." + this.port;
	}
}
