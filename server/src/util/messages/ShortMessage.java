package util.messages;

import util.Address;

/**
 * This class handles a few control messages that were messy to make into standard-format messages.
 * This class is meant to override some methods inherited from Message and make sure it's clear you shouldn't
 * be calling some methods from that class.
 * 
 * This is bad OOP practice, but this class isn't so complex that it will cause problems down the road.
 * @author Christian
 *
 */
public final class ShortMessage extends Message {

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
