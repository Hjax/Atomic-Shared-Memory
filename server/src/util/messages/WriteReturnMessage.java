package util.messages;

import dataserver.DataServer;
import util.Address;

/**
 * reqid:write-return:pcid:xpos:ypos:key
 * @author Christian
 *
 */
public class WriteReturnMessage extends Message {
	
	
	public WriteReturnMessage(Address sender, Address recipient, int reqid, int pcid, float xpos, float ypos, String key) {
		super(sender, recipient, reqid + "", DataServer.WRITE_RECEIPT_FLAG, pcid + "", xpos + "", ypos + "", key);
	}
	
	public String getKey() {
		return this.parts[5];
	}
	
}
