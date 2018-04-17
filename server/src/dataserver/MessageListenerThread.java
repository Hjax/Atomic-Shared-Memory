package dataserver;

import util.messages.Message;

/**
 * This class is the parent class for all objects that listen for incoming messages.
 * This abstraction is necessary because the group was informed that listening to sockets might not be the final
 * method of receiving messages, so this abstraction was created to deal with that.
 * @author Christian
 *
 */
public abstract class MessageListenerThread extends Thread {
	volatile boolean cont;
	protected DataServer server;
	protected MessageParser parser;

	/**
	 * The primary constructor for the MessageListenerThread object.
	 * @param server	The server to listen for and notify when a new message is received
	 */
	protected MessageListenerThread (DataServer server) {
		this.server = server;
		this.parser = new MessageParser(this.server);
	}

	protected abstract Message listen();

	public void run() {
		this.cont = true;

		Message message;

		while(this.cont) {
			if ( (message = this.listen()) != null)
				this.parser.parse(message);
		}

	}


}
