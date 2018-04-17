package util.messages;

import util.Address;

/**
 * reqid:ohsam-read-request:pcid:xpos:ypos:key
 * @author Christian
 *
 */

public class OhSamReadRequestMessage extends Message {
	public OhSamReadRequestMessage(Address sender, Address recipient, String message) {
		super(sender, recipient, message);
	}
	
	public String getKey() {
		return this.parts[5];
	}
	
	
}
