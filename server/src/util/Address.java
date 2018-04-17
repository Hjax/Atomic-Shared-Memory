package util;

import java.net.InetAddress;

public class Address {
	
	InetAddress addr;
	int port;
	
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
}
