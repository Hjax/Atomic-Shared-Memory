package util.messages;

import util.Address;

public class ShortMessage extends Message {

	public ShortMessage(Address sender, Address recipient, String messageParts) {
		super(sender, recipient, messageParts);
	}

	@Override
	public int getPCID() {
		return -1;
	}
	@Override
	public float getX() {
		return -1;
	}
	@Override
	public float getY() {
		return -1;
	}
	
}
