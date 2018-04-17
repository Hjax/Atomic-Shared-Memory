package util.messages;

import dataserver.DataServer;
import util.Address;

/**
 * reqid:reliable-read:pcid:xpos:ypos:key
 * @author Christian
 *
 */

public class ReliableReadMessage extends Message {

	public ReliableReadMessage(Address sender, Address recipient, int reqid, int pcid, float xpos, float ypos, String key) {
		super(sender, recipient, reqid + "", DataServer.RELIABLE_READ_FLAG, pcid + "", xpos + "", ypos + "", key);
	}
	
	public ReliableReadMessage(Address sender, Address recipient, String message) {
		super(sender, recipient, message);
	}

	public String getKey() {
		return super.get(5);
	}
}
