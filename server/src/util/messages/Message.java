package util.messages;

import java.net.UnknownHostException;

import dataserver.DataServer;
import util.Address;

/**
 * This object should act like an envelope: it has a recipient address, a sender's address, 
 * and a message wrapped inside.
 * 
 * @author Christian
 *
 */
public class Message {
	
	public static final String DELIMITER = ":";
	
	String[] parts;
	String message;
	Address sender, recipient;
	
	
	public Message(Address sender, Address recipient, String ... messageParts) {
		
		
		this.sender = sender;
		this.recipient = recipient;
		this.parts = messageParts;
		
		
		
		this.message = "";
		for (String part : parts)
			this.message = this.message + Message.DELIMITER + part;
		
		this.message = this.message.substring(Message.DELIMITER.length());
		
			
		
	}
	
	public Message(Address sender, Address recipient, String messageParts) { 
		this.sender = sender;
		this.recipient = recipient;
		this.parts = messageParts.split(Message.DELIMITER);
		this.message = messageParts;
	}
	
	
	@Override
	public String toString() {
		return this.message;
	}
	
	public String get(int index) {
		if (index >= this.parts.length || index < 0)
			return null;
		else
			return this.parts[index];
	}
	
	
	/*
	 * #####################################################################
	 * ##### STANDARDIZED INDEXES ##########################################
	 * #####################################################################
	 * 
	 * Every message type has the same first 5 indexes. The Message superclass takes care of all of them
	 * so its children can inherit them and reduce class size
	 */
	public int getReqID() {
		return Integer.parseInt(this.get(0));
	}
	public String getFlag() {
		return this.get(1);
	}
	public int getPCID() {
		return Integer.parseInt(this.get(2));
	}
	public float getX() {
		return new Float(this.parts[3]);
	}
	public float getY() {
		return new Float(this.parts[4]);
	}
	// all messages have same first 5 indexes; after this, it's up for grabs
	
	
	/**
	 * @return the IP/port that sent this message
	 * 
	 */
	public Address sender() {
		return this.sender;
	}
	
	/**
	 * @return the IP/port that will receive this message
	 */
	public Address recipient() {
		return this.recipient;
	}
	
	
	/**
	 * Takes a message that contains a flag denoting the message type and returns a message of the 
	 * appropriate class. Useful for case-checking in MessageParser
	 * @param recipient whatever address is going to receive / has received this message
	 * @param sender whatever address sent / will send this message
	 * @param message the messsage string
	 * @return
	 */
	public static Message construct(Address recipient, Address sender, String message) {
		Message out = new Message(recipient, sender, message);
		String flag = out.getFlag();
		if (flag == null) 
			return out;
		else if (flag.equals(DataServer.RELIABLE_READ_FLAG))
			return new ReliableReadMessage(recipient, sender, message);
		else if (flag.equals(DataServer.READ_REQUEST_FLAG))
			return new ReadRequestMessage(recipient, sender, message);
		else if (flag.equals(DataServer.OHSAM_READ_REQUEST_FLAG))
			return new OhSamReadRequestMessage(recipient, sender, message);
		else if (flag.equals(DataServer.WRITE_REQUEST_FLAG)
				|| flag.equals(DataServer.OHSAM_WRITE_REQUEST_FLAG))
			return new WriteRequestMessage(recipient, sender, message);
		else if (flag.equals(DataServer.SET_LOCATION_FLAG))
			return new SetLocationMessage(recipient, sender, message);
		else if (flag.equals(DataServer.OHSAM_RELAY_FLAG))
			try {
				return new OhSamRelayMessage(recipient, sender, message);
			} catch (UnknownHostException e) {
				System.out.println("ERROR: " + message + " did not have a valid IP!");
				e.printStackTrace();
				return out;
			}
		
		// These are all unique messages used for testing that were really messy with the standard message format
		else if (message.split(Message.DELIMITER).length == 2)
			return new ShortMessage(recipient, sender, message);
		
		// If it's none of these, then a standard message object suffices
		else
			return out;
	}
}
