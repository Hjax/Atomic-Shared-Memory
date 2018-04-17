package util.messages;

import java.net.InetAddress;
import java.net.UnknownHostException;

import dataserver.DataServer;
import util.Address;

/**
 * reqid:flag:pcid:xpos:ypos:clientid:seqid:key:value:ip:port:clientxpos:clientypos
 * @author Christian
 *
 */
public class OhSamRelayMessage extends Message {

	
	private final Address clientAddress;
	
	public OhSamRelayMessage(Address sender, Address recipient, int reqid, int pcid, float xpos, float ypos, int clientid, int seqid, String key, String value, Address returnAddress, float clientxpos, float clientypos) {
		super(sender, recipient, reqid + ":" + DataServer.OHSAM_RELAY_FLAG + ":" + pcid + ":" + xpos + ":" + ypos + ":" + clientid + ":" + seqid + ":" + key + ":" + value 
				+ ":" + returnAddress.addr().getHostAddress() + ":" + returnAddress.port() + ":" + clientxpos + ":" + clientypos);
		this.clientAddress = returnAddress;
	}

	public OhSamRelayMessage(Address recipient, Address sender, String message) throws UnknownHostException {
		super(recipient, sender, message);
		this.clientAddress = new Address(InetAddress.getByName(this.parts[9]), Integer.parseInt(this.parts[10]));
	}

	public int getClientID() {
		return Integer.parseInt(this.parts[5]);
	}
	public int getSeqID() {
		return Integer.parseInt(this.parts[6]);
	}
	public String getKey() {
		return this.parts[7];
	}
	public String getValue() {
		return this.parts[8];
	}
	public Address getAddress() {
		return this.clientAddress;
	}
	public float clientX() {
		return Float.parseFloat(this.parts[11]);
	}
	public float clientY() {
		return Float.parseFloat(this.parts[12]);
	}
}
