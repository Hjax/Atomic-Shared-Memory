package util.messages;

import util.Address;

/**
 * reqid:write-request:pcid:xpos:ypos:seqid:key:val
 * @author Christian
 *
 */
public class WriteRequestMessage extends Message {
	
	public WriteRequestMessage(Address recipient, Address sender, String message) {
		super(recipient, sender, message);
	}
	
	public int getSeqId() {
		return Integer.parseInt(super.get(5));
	}
	public String getKey() {
		return super.get(6);
	}
	public String getVal() {
		return super.get(7);
	}
	

}
