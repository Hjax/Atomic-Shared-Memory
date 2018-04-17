package util.messages;

import dataserver.DataServer;
import util.Address;

/**
 * reqid:read-return:pcid:xpos:ypos:seqid:val
 * @author Christian
 *
 */
public class ReadReturnMessage extends Message {
		
	public ReadReturnMessage(Address sender, Address recipient, int reqid, int pcid, float x, float y, int seqid, String val) {
		super(sender, recipient, reqid + "", DataServer.READ_RECEIPT_FLAG, pcid + "", x + "", y + "", seqid + "", val);
	}
	
	public int getSeqID() {
		return Integer.parseInt(this.parts[5]);
	}
	public String getVal() {
		return this.parts[6];
	}
	
}
