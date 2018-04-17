package util.messages;

import util.Address;

/**
 * reqid:read-request:pcid:xpos:ypos:key
 * @author Christian
 *
 */
public class ReadRequestMessage extends Message {
	
	public ReadRequestMessage(Address recipient, Address sender, String message) {
		super(recipient, sender, message);
	}
	
	public String getKey() {
		return super.get(5);
	}
	
	
}
