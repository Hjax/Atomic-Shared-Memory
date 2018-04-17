package dataserver;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import util.Address;

/**
 * A subclass of DataServer.
 * 
 * This is a non-abstract object that specifies the abstract read() and write() operations of the superclass to read
 * and write to memory instead of the disk.
 * 
 * @author Christian
 *
 */
public class MemoryDataServer extends DataServer {

	
	
	
	
	/**
	 * The primary constructor for a MemoryDataServer object.
	 * @param serverid	The identification number that distinguishes this DataServer from other DataServers
	 * @param ADDRESSES	The other servers in the network; this object is stored as a volatile array and can be updated
	 * @param port	The port at the local address that this object should listen to for UDP messages
	 * @throws UnknownHostException 
	 */
	public MemoryDataServer(int serverid, int port, String address, Address[] addresses) throws SocketException, UnknownHostException {
		super(serverid, port, address, addresses);
	}
	
	
	/**
	 * The map that stores all data associated with keys put in this object
	 */
	private HashMap<String, String> DATA = new HashMap<String, String>();
	
	@Override
	protected String getData(String key) {
		return this.DATA.get(key);
	}
	
	
	/**
	 * The map that stores all the timestamps associated with keys put in this object
	 */
	private HashMap<String, Integer> TIME = new HashMap<String, Integer>();
	
	@Override
	protected int getTime(String key) {
		if (this.TIME.containsKey(key))
			return this.TIME.get(key);
		else
			return 0;
	}

	/**
	 * This method is how the MemoryDataServer commits data to its stores. This implementation is the easiest: it simply stores the keys and values in a HashMap
	 */
	@Override
	protected void commitData(String key, String value, int timestamp) {
		
		if (this.DATA.containsKey(key)) {
			int localStamp = this.TIME.get(key);
			int newStamp = timestamp;
			
			if (localStamp < newStamp) { // if the local stamp is older than the new stamp, we update
				//System.out.println("We're updating!");
				this.TIME.put(key, timestamp);
				this.DATA.put(key, value);
			}
		}
		else {
			this.DATA.put(key, value);
			this.TIME.put(key, timestamp);
		}
		
		
		
	}

	



	

	
	
	

}
