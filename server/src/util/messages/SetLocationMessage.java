package util.messages;

import util.Address;

/**
 * reqid:flag:pcid:x-coordinate:y-coordinate
 * 
 * this class isn't actually any different from the "message" class...
 * 
 * @author Christian
 *
 */
public class SetLocationMessage extends Message {

	
	
	public SetLocationMessage(Address sender, Address recipient, String messageParts) {
		super(sender, recipient, messageParts);
	}
	
	
}
