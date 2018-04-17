package dataserver;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedHashMap;

import util.Address;
import util.Location;
import util.messages.*;

/**
 * This is the abstract object that outlines what a DataServer might look like. The abstract portion of this object is
 * relates to storing and retrieving data (timestamps associated with keys and values associated with keys). The method 
 * by which the data is retrieved is subject to change due to the nature of storing our data; it is possible to store
 * data to and read data from the disk or memory, so the abstraction has been made to accommodate subclasses that specify
 * which method is preferred.
 * 
 * @author Christian
 *
 */
public abstract class DataServer {

	// The abstract methods of this object
	
	protected abstract void commitData(String key, String value, int timestamp);
	protected abstract String getData(String key);
	protected abstract int getTime(String key);


	public final static String 
	WRITE_RECEIPT_FLAG = "write-return",
	WRITE_REQUEST_FLAG = "write-request",
	RELIABLE_READ_FLAG = "reliable-read",
	READ_RECEIPT_FLAG = "read-return",
	READ_REQUEST_FLAG = "read-request",
	OHSAM_WRITE_REQUEST_FLAG = "ohsam-write-request",
	OHSAM_RELAY_FLAG = "ohsam-relay",
	OHSAM_RETURN_FLAG = "ohsam-return",
	OHSAM_READ_REQUEST_FLAG = "ohsam-read-request",
	WAIT_COMMAND_FLAG = "wait", 
	WAKE_COMMAND_FLAG = "wake",
	SET_LOCATION_FLAG = "set-location";



	public final Address localAddress;

	
	protected boolean awake = true;

	/**
	 * This is the list of other data servers in the network
	 */
	public final int port, id;
	public int droprate = 0;

	protected final Address[] addresses;
	public int quorum() {
		return (int) (this.addresses.length / 2) + 1;
	}

	private Location location = new Location(0, 0);
	public void setLocation(float x, float y) {
		this.location = new Location(x, y);
	}
	public Location getLocation() {
		return this.location;
	}

	public DatagramSocket soc;

	private final static int REFRESH = 10000000;

	/**
	 * The primary constructor for a DataServer object.
	 * @param serverid	The identification number that distinguishes this DataServer from other DataServers
	 * @param ADDRESSES	The other servers in the network; this object is stored as a volatile array and can be updated
	 * @param port	The port at the local address that this object should listen to for UDP messages
	 * @throws UnknownHostException 
	 */
	public DataServer(int serverid, int port, String address, Address[] addresses) throws SocketException, UnknownHostException {


		this.id = serverid;
		this.port = port;
		this.addresses = addresses;


		try {
			this.soc = new DatagramSocket(port, InetAddress.getByName(address));
			System.out.println("Data Server " + this.id + " created: "
					+ "\n\t" + "Port: " + this.soc.getLocalPort()
					+ "\n\t" + "Addr: " + this.soc.getLocalAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.localAddress = new Address(this.soc.getLocalAddress(), this.soc.getLocalPort());

	}

	/**
	 * Delegates port listening to a thread so that methods in this object can be called while it is listening.
	 * Note that the object "thread" is a MessageListenerThread, which is a superclass abstraction of the class
	 * SocketListenerThread. For now, the DataServer will receive messages through UDP messages on a port at the
	 * local address, but it has been expressed to the group that this might not be the case for future versions
	 * of the project.
	 * 
	 * The reasoning behind making this a thread as opposed to a while loop is because this object might listen to
	 * several different objects at the same time that should be checked simultaneously instead of in sequence
	 * 
	 * TODO change this if the method of receiving messages changes
	 */
	public Thread startMessageListenerThread() throws BindException {

		MessageListenerThread thread = new SocketListenerThread(this, this.soc, DataServer.REFRESH);
		thread.start();
		return thread;

	}

	/**
	 * It might be a little silly to have an if/else statement inside the read method after the message parser already evaluates the if/else,
	 * but having a bunch of different read methods is cluttery and we're not *really* worried about performance that much to be cutting if/else statements
	 * @param key
	 * @param returnAddress
	 * @param reqid
	 * @param clientid
	 * @param flag
	 */
	protected void read(String key, Address returnAddress, int reqid, int clientid, String flag, float clientxpos, float clientypos) {

		String value = this.getData(key);
		int seqid = this.getTime(key);


		// uses OHSAM_READ_REQUEST_FLAG and READ_REQUEST_FLAG instead of OHSAM_ALGORITHM_FLAG and ABD_ALGORITHM_FLAG because this is the only place those algorithm
		// flags would be used and there is no reason to have multiple message types for the same algorithm. 
		
		if (flag.equals(DataServer.OHSAM_READ_REQUEST_FLAG)) {

			this.addRelay(clientid, reqid, seqid, key, value, returnAddress);
			for (Address recipient : this.addresses)
				this.send(new OhSamRelayMessage(this.localAddress, recipient, reqid, this.id, this.location.x, this.location.y, clientid, seqid, key, value, returnAddress, clientxpos, clientypos));
			
		}

		else if (flag.equals(DataServer.READ_REQUEST_FLAG) || flag.equals(DataServer.RELIABLE_READ_FLAG)) {
			
			// 
			if (value == null && seqid <= 0) {
				this.send(new ReadReturnMessage(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, 
						reqid, this.id, clientxpos, clientypos, 0, 
						"null"));
			}
			else if (value != null && seqid <= 0)
				this.send(new ReadReturnMessage(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, 
						reqid, this.id, clientxpos, clientypos, 0, 
						"data-sync-error: value associated with '" + key + "' existed as data, but timestamp existed below 0"));
			else if (value == null && seqid > 0)
				this.send(new ReadReturnMessage(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, 
						reqid, this.id, clientxpos, clientypos, 0, 
						"data-sync-error: value associated with '" + key + "' did not exist as data, but timestamp existed above 0"));
			else
				this.send(new ReadReturnMessage(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, reqid, this.id, clientxpos, clientypos, seqid, value));
		}
	}

	/**
	 * Does this server's write algorithm
	 * 
	 * @param key	The key of the key value pair
	 * @param value	The value of the key value pair
	 * @param timestamp	The timestamp showing the freshness of this value
	 * @param returnAddress	The IP/port combination this message came from; used for sending receipts
	 * @param reqid The request id of the message. Note that this is not necessary for the ABD read, but is necessary for placing the ohsam handler in its map
	 */
	protected void write(String key, String value, int timestamp, Address returnAddress, int reqid, float clientxpos, float clientypos) {
		this.commitData(key, value, timestamp);// no matter what, send a receipt
		WriteReturnMessage message = new WriteReturnMessage(
				new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), 
				returnAddress, 
				reqid, 
				this.id,
				clientxpos,
				clientypos,
				key);

		this.send(message);


	}


	public void close() {
		this.soc.close();
	}
	public void sleep() {
		this.awake = false;
	}
	public void wake() {
		this.awake = true;
	}


	/**
	 * This object keeps track of all important information for an OhSamRequest.
	 * @author Christian
	 *
	 */
	public final class OhSamRequest {
		public final int clientid, reqid;
		protected int count = 0;

		/**
		 * A hashset is used because set objects have an O(1) complexity for contains(arg) and add(arg)
		 * This is useful the set has hundreds or thousands of server addresses stored
		 */
		private HashSet<Address> addresses = new HashSet<Address>();

		public OhSamRequest(int clientid, int reqid) {
			this.clientid = clientid;
			this.reqid = reqid;
		}

		int reqID() {
			return this.reqid;
		}
		void addRelay(Address address) {
			if (!this.addresses.contains(address)) {
				this.addresses.add(address);
				this.count++;
			}
			
			
		}
		int count() {
			return this.count;
		}
	}
	private LinkedHashMap<Integer, OhSamRequest> requests = new LinkedHashMap<Integer, OhSamRequest>();
	public final int getNumRelays(int reqid, int pcid) {
		OhSamRequest request = this.requests.get(pcid);
		if (request == null) {
			System.out.println("Queried for the OhSamRequest." + reqid + "." + pcid + " but it doesn't exist yet!");
			return 0;
		}
		else if (request.reqID() != reqid) {
			System.out.println("Queried for the OhSamRequest." + reqid + "." + pcid + " but its reqid isn't the same as the request! (" + request.reqID() + ")! Returning 0.");
			return 0;
		}
		else
			return request.count;
	}
	public final void addRelay(int pcid, int reqid, int newSeqid, String key, String value, Address address) {
		// first, get the request (if there is one)
		OhSamRequest request = this.requests.get(pcid);

		// FIRST PART: Seeing if we need to update an OhSamRequest.
		// 1. If there is no current OhSamRequest for the client
		// 2. If our OhSamRequest is old and the client doesn't want it anymore
		// 3. If our OhSamRequest is current and we want to update it
		
		// if there is no request, make a new one and add it in
		if (!this.requests.containsKey(pcid)) {
			request = new OhSamRequest(pcid, reqid);
			this.requests.put(pcid, request);
		}

		// if the client reqid is NEWER (of higher value) than the reqid of the request we're currently handling,
		// then we know the client already got all the responses it needs from other servers and has moved on to
		// another request
		else if (reqid > request.reqid) {
			request = new OhSamRequest(pcid, reqid);
			request.addRelay(address);
			this.requests.put(pcid, request);
		}

		// else, we have a valid request id and we're good.
		else
			request.addRelay(address);

		// SECOND PART: Updating the server if the relay has given us something useful
		
		int oldSeqid = this.getTime(key);

		// seqids with higher value are considered "newer"
		if (newSeqid > oldSeqid)
			this.commitData(key, value, newSeqid);




	}

	public final double DISTANCE_PING_RATIO = 10.0; // 1ms ping / unit distance
	/**
	 * 
	 * @param targetX
	 * @param targetY
	 * @return the approximate ping (to the nearest millisecond) between the location of this server and the location of the target server
	 */
	public long getPing(float targetX, float targetY) {

		float serverX = this.location.x;
		float serverY = this.location.y;

		// use the distance function to get distance
		// 	   distance = sqrt	   ((x1 - x2)^2                             + (y1 - y2)^2)
		double distance = Math.sqrt((serverX - targetX)*(serverX - targetX) + (serverY - targetY)*(serverY - targetY));


		// returns double rounded to nearest long, which is good enough
		
		System.out.println("Ping: " + distance);
		
		return (long) (distance * DISTANCE_PING_RATIO / 2);
	}

	
	/**
	 * Sends a message. The recipient of this message is stored in the Message object
	 * The message is sent via a MessageSender object for two reasons:
	 * 1. The networking hooplah by which the message is sent might change in the future
	 * 2. In order to simulate ping, there is a delay on sending messages out. To keep from sleeping the whole main thread, the MessageSender object only sleeps its own thread.
	 * @param message The message to be sent
	 */
	protected void send(Message message) {		
		System.out.println("Sending to\t" + message.recipient().addr() + "\t:\t" + message.toString());
		new SocketMessageSender(this.soc, message, this).start();
	}	
	private abstract class MessageSender extends Thread {

		protected final Address recipient;
		protected final DatagramSocket soc;
		protected final DataServer server;
		protected final Message m;

		MessageSender(DatagramSocket soc, Message m, DataServer server) {
			this.recipient = m.recipient();
			this.server = server;
			this.soc = soc;
			this.m = m;
		}

		public void run() {
			Long delay = this.server.getPing(m.getX(), m.getY());
			if (delay != null)
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					System.out.println(this + " could not wait for " + delay + "ms!");
					e.printStackTrace();
				}
			this.send();
		}

		abstract void send();

		@Override
		public String toString() {
			return "MessageSender." + this.recipient + ".[" + this.m + "]";
		}

	}
	private class SocketMessageSender extends MessageSender {

		SocketMessageSender(DatagramSocket soc, Message m, DataServer server) {
			super(soc, m, server);
		}

		@Override
		void send() {
			try {
				this.soc.send(new DatagramPacket(this.m.toString().getBytes(), this.m.toString().getBytes().length, this.recipient.addr(), this.recipient.port()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
