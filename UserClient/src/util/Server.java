package util;

import java.net.InetAddress;

public class Server {
	
	private InetAddress address;
	private int port;
	
	public Server(InetAddress ADDRESS, int PORT)
	{
		address = ADDRESS;
		port = PORT;
	}
	
	
	//setters
	
	public void setAddress(InetAddress ADDRESS)
	{
		address = ADDRESS;
	}
	
	public void setPort(int PORT)
	{
		port = PORT;
	}
	
	
	//getters
	
	public InetAddress getAddress()
	{
		return address;
	}
	
	public int getPort()
	{
		return port;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other == this)
		{
			return true;
		}
		Server tempOther = (Server) other;
		if (address.equals(tempOther.getAddress()) && port == tempOther.getPort())
			{return true;}
		else
			{return false;}
	}
	
	@Override
	public int hashCode()
	{
		return 37 * port + address.toString().hashCode();
	}

}
