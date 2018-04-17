package dataserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import util.Address;
import util.messages.Message;


/**
 * A subclass of MessageListenerThread.
 * 
 * This is a non-abstract object that specifies the abstract listen() operations of the superclass to listen to a port
 * for UDP messages.
 * 
 * @author Christian
 *
 */
public class SocketListenerThread extends MessageListenerThread {


	/**
	 * The socket being listened to for UDP messages
	 */
	protected DatagramSocket soc;
	private int timeout = -1;

	/** controls how much information is displayed from this object
	 * 
	 */
	private boolean verbose = false;


	SocketListenerThread(DataServer server, DatagramSocket soc, int timeout) {
		super(server);

		try {
			this.soc = soc;
			this.soc.setSoTimeout(timeout);
			this.timeout = timeout;
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected Message listen() {
		try {

			DatagramPacket packet = new DatagramPacket(new byte[1028], 1028);
			Address localAddress = new Address(this.soc.getLocalAddress(), this.soc.getLocalPort());

			this.soc.receive(packet);

			if (packet != null) {
				String s = "";
				for (byte b : packet.getData())
					if (b != 0)
						s = s + (char) b;
				return Message.construct(new Address(packet.getAddress(), packet.getPort()), localAddress, s.trim());
			}

		} catch (SocketTimeoutException toe) {
			if (verbose)
				System.out.println("DataServer " + this.server.id + ": timeout (" + timeout + ")");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}


}
