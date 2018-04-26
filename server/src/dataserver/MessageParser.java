package dataserver;


import java.util.Random;

import util.messages.*;


/**
 * This object is meant to interpret the messages received by the MessageListenerThread
 * @author Christian
 *
 */
public class MessageParser {

	public final DataServer server;

	public MessageParser(DataServer server) {
		this.server = server;
	}

	/**
	 * Parses the message fed to it and performs the action designated by the message (or tells the server to do it)
	 * @param message
	 */
	protected void parse(Message message) {

		// if by some miracle it's null, we leave. This should never happen, but you never know
		if (message == null) {
			System.out.println("message null. Returning.");
			return;
		}


		System.out.println("Message from\t" + message.sender().addr() + ":" + message.sender().port() + "\t:\t" + message);

		String flag = message.getFlag();


		// if there's no flag, we have no idea what the message is, and it's certainly not a message we're interested in. discard it
		if (flag == null) {
			System.out.println("Flag null. No operations performed. Message: " + message);
			return;
		}

		/* ===============================================================================
		 * === SUPERUSER MESSAGES ========================================================
		 * ===============================================================================
		 * 
		 * These are messages meant to be used for checking and setting the state of the servers
		 */
		if (message instanceof ReliableReadMessage) {
			this.server.read(
					((ReliableReadMessage) message).getKey(), 
					((ReliableReadMessage) message).sender(),
					((ReliableReadMessage) message).getReqID(),
					((ReliableReadMessage) message).getPCID(),
					((ReliableReadMessage) message).getFlag(),
					this.server.getLocation().x,
					this.server.getLocation().y);
			return;
		}
		else if (message instanceof SetLocationMessage) {
			this.server.setLocation(
					((SetLocationMessage) message).getX(),
					((SetLocationMessage) message).getY());
			return;
		}
		// sets droprate
		else if (flag.equals("drop")) {
			this.server.droprate = Integer.parseInt(message.get(2));
			return;
		}

		/* ###############################################################################
		 * ### WAKE MESSAGE ##############################################################
		 * ###############################################################################
		 * 
		 * This controls whether the server is awake or not.
		 * ALL MESSAGES AFTER THIS CASE ARE DROPPED IF THE SERVER IS ASLEEP
		 */

		// Check to see if the server is asleep. If it is, we just leave.
		else if (!this.server.awake) {
			if (flag.equals("wake"))
				this.server.wake();
			else
				return;
		}

		/* ===============================================================================
		 * === SHORT MESSAGES ============================================================
		 * ===============================================================================
		 * 
		 * These are messages that are used by the control client for testing
		 */

		// Control messages -- not affected by drop rate, but affected by server being awake or not
		else if (flag.equals("respond")) {
			this.server.send(new Message(
					message.recipient(), 
					message.sender(),
					"response"));
			return;
		}

		// Replies back to server 
		// TODO deprecate this
		else if (flag.equals("echo")) {
			this.server.send(new Message(
					message.recipient(), 
					message.sender(),  
					message.toString()));
			return;
		}

		// Sleep functions
		else if (flag.equals("wait")) {
			this.server.sleep();
			return;
		}



		/* ===============================================================================
		 * === STANDARD MESSAGES =========================================================
		 * ===============================================================================
		 * 
		 * These are messages that would be used by a regular client. We outsource this to 
		 * another object for the purpose of not blocking our MessageParser with Thread.sleep()'s
		 */

		// else, on to the messages affected by drop rate...
		else {

			try {

				// 1/droprate probability of just ignoring this message
				if (new Random().nextInt(100) < this.server.droprate)
					return;

				else
					new ClientPingSimulator(this.server, message, this.server.getPing(message.getX(), message.getY())).start();
				
			} catch (IndexOutOfBoundsException e) {
				System.out.println("ERROR: Something was out of bounds for the message " + message);
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println("ERROR: Something was null when handling the message " + message);
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("ERROR: Something went wrong when handling the message " + message);
				e.printStackTrace();
			}
		}

		// we should be done now
		return;

	}

	public static class ClientPingSimulator extends Thread {
		public final long ping;
		public final Message message;
		public final DataServer server;

		public ClientPingSimulator(DataServer server, Message message, long ping) {
			this.ping = ping;
			this.message = message;
			this.server = server;
		}


		@Override
		public void run() {
			try {
				Thread.sleep(this.ping);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Message message = this.message;


			// get these messages from clients
			if (message instanceof WriteRequestMessage) {
				this.server.write(
						((WriteRequestMessage) message).getKey(), 
						((WriteRequestMessage) message).getVal(), 
						((WriteRequestMessage) message).getSeqId(), 
						((WriteRequestMessage) message).sender(),
						((WriteRequestMessage) message).getReqID(),
						((WriteRequestMessage) message).getX(),
						((WriteRequestMessage) message).getY());
			}
			else if (message instanceof ReadRequestMessage) {
				this.server.read(
						((ReadRequestMessage) message).getKey(), 
						((ReadRequestMessage) message).sender(),
						((ReadRequestMessage) message).getReqID(),
						((ReadRequestMessage) message).getPCID(),
						((ReadRequestMessage) message).getFlag(),
						((ReadRequestMessage) message).getX(),
						((ReadRequestMessage) message).getY());

			}
			else if(message instanceof OhSamReadRequestMessage) {
				this.server.read(
						((OhSamReadRequestMessage) message).getKey(), 
						((OhSamReadRequestMessage) message).sender(),
						((OhSamReadRequestMessage) message).getReqID(),
						((OhSamReadRequestMessage) message).getPCID(),
						((OhSamReadRequestMessage) message).getFlag(),
						((OhSamReadRequestMessage) message).getX(),
						((OhSamReadRequestMessage) message).getY());
			}

			// get these messages from servers
			else if (message instanceof OhSamRelayMessage) {
				this.server.addRelay((OhSamRelayMessage) message);

				// TODO should sending the message be handled here, or inside the OhSamRequest object?
				// pros to handling here: don't have to pass as many arguments around between methods
				// cons to handling here: more work for this thread = slower thread = slower throughput of messages
				
				/*
				int reqid = message.getReqID();
				int clientid = ((OhSamRelayMessage) message).getClientID();
				
				System.out.println(this.server.getNumRelays(reqid, clientid) + " and " + this.server.quorum());
				*/

			}
			else {
				
				System.out.println("Could not recognize message: " + message);
			}
		}


	}

}
