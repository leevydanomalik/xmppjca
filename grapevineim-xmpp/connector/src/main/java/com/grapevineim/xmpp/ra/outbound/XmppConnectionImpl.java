package com.grapevineim.xmpp.ra.outbound;

import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.ToContainsFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.grapevineim.xmpp.XmppConnection;
import com.grapevineim.xmpp.XmppMessage;
import com.grapevineim.xmpp.XmppMessageListener;

/**
 * Application-level connection handle that is used by a client component to
 * access an EIS instance.
 */

public class XmppConnectionImpl implements XmppConnection, ConnectionListener {

	private final ManagedConnection managedConnection;
	private final XMPPConnection connection;
	private final Set<XmppMessageListener> messageListeners;
	private final Set<ConnectionEventListener> connectionEventListeners;
	private final String PRESENCE_MESSAGE = "Type 'help' for a list of commands"; 
	private final String PRESENCE_STATUS = "available";

	private static final Log LOG = LogFactory.getLog(XmppConnectionImpl.class);

	public XmppConnectionImpl(ManagedConnection managedConnection,
			XmppConnectionRequestInfo connectionRequestInfo)
			throws ResourceException {
		LOG.info("Constructor");
		this.messageListeners = new HashSet<XmppMessageListener>();
		this.connectionEventListeners = new HashSet<ConnectionEventListener>();
		this.managedConnection = managedConnection;
		this.connection = connect(connectionRequestInfo);
		addMessagePacketListener(this.connection, new MessagePacketProcessor(
				connectionRequestInfo), connectionRequestInfo.getUsername());
		addPresencePacketListener(this.connection,
				new PresencePacketProcessor(), connectionRequestInfo
						.getUsername());
		login(this.connection, connectionRequestInfo.getUsername(),
				connectionRequestInfo.getPassword());
		setPresence(this.connection, PRESENCE_STATUS, PRESENCE_MESSAGE);
		acceptSubscriptionsManually(this.connection);
	}

	private void acceptSubscriptionsManually(XMPPConnection conn)
			throws ResourceException {
		try {
			Roster roster = conn.getRoster();
			roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
		} catch (Exception e) {
			LOG.error("Could not set subscription mode.", e);
			throw new ResourceException("Could not set subscription mode.", e);
		}
	}

	public void sendMessage(XmppMessage xmppMessage) throws ResourceException {
		LOG.info("sendMessage(XmppMessage)");
		try {
			ChatManager chatManager = connection.getChatManager();
			Chat chat = null;

			// get the chat if the threadID exists
			if (xmppMessage.getThreadId() != null) {
				chat = chatManager.getThreadChat(xmppMessage.getThreadId());
			}

			// if chat is still null then create a new one
			if (chat == null) {
				chat = chatManager.createChat(xmppMessage.getTo(), null);
			}

			// send the message
			chat.sendMessage(xmppMessage.getBody());

		} catch (Exception e) {
			LOG.error("Could not sendMessage.", e);
			throw new ResourceException("Could not sendMessage.", e);
		}
	}

	private void sendMessage(String to, String message)
			throws ResourceException {
		LOG.info("sendMessage(String, String)");
		try {
			ChatManager chatManager = connection.getChatManager();

			// create the chat
			Chat chat = chatManager.createChat(to, null);

			// send the message
			chat.sendMessage(message);

		} catch (Exception e) {
			LOG.error("Could not sendMessage.", e);
			throw new ResourceException("Could not sendMessage.", e);
		}
	}

	public ManagedConnection getManagedConnection() {
		return managedConnection;
	}

	private XMPPConnection connect(XmppConnectionRequestInfo info)
			throws ResourceException {

		try {
			ConnectionConfiguration config = new ConnectionConfiguration(info
					.getHost(), info.getPort().intValue(), info.getDomain());

			XMPPConnection conn = new XMPPConnection(config);
			conn.connect();
			return conn;
		} catch (XMPPException xe) {
			LOG.error("Could not connect.", xe);
			throw new ResourceException("Could not connect", xe);
		}
	}

	private void addMessagePacketListener(XMPPConnection conn,
			PacketListener listener, String toContains)
			throws ResourceException {
		try {
			conn.addPacketListener(listener, new AndFilter(
					new PacketTypeFilter(Message.class), new ToContainsFilter(
							toContains)));
		} catch (Exception e) {
			LOG.error("Could not set PacketListener.", e);
			throw new ResourceException("Could not set PacketListener", e);
		}
	}

	private void addPresencePacketListener(XMPPConnection conn,
			PacketListener listener, String toContains)
			throws ResourceException {
		try {
			conn.addPacketListener(listener, new AndFilter(
					new PacketTypeFilter(Presence.class), new ToContainsFilter(
							toContains)));
		} catch (Exception e) {
			LOG.error("Could not set PacketListener.", e);
			throw new ResourceException("Could not set PacketListener", e);
		}
	}

	public void login(XMPPConnection conn, String username, String password)
			throws ResourceException {
		try {
			conn.login(username, password);
		} catch (XMPPException xe) {
			LOG.error("Could not login.", xe);
			throw new ResourceException("Could not login", xe);
		}
	}

	public void setPresence(XMPPConnection conn, String type, String status)
			throws ResourceException {
		try {
			// Create a new presence.
			Presence presence = new Presence(Presence.Type.valueOf(type));
			presence.setStatus(status);
			// Send the packet
			conn.sendPacket(presence);
		} catch (Exception e) {
			LOG.error("Could not set presence.", e);
			throw new ResourceException("Could not set presence", e);
		}
	}

	public void close() throws ResourceException {
		try {
			if (this.connection.isConnected()) {
				this.connection.disconnect(new Presence(
						Presence.Type.unavailable));
			}
		} catch (Exception e) {
			LOG.error("Could not disconnect", e);
			throw new ResourceException("Could not disconnect", e);
		}
	}

	public String getUser() {
		return this.connection.getUser();
	}

	public void addMessageListener(XmppMessageListener listener)
			throws ResourceException {
		synchronized (messageListeners) {
			messageListeners.add(listener);
		}
	}

	public void removeMessageListener(XmppMessageListener listener)
			throws ResourceException {
		synchronized (messageListeners) {
			messageListeners.remove(listener);
		}
	}

	public void addConnectionEventListener(ConnectionEventListener listener)
			throws ResourceException {
		synchronized (connectionEventListeners) {
			connectionEventListeners.add(listener);
		}
	}

	public void removeConnectionEventListener(ConnectionEventListener listener)
			throws ResourceException {
		synchronized (connectionEventListeners) {
			connectionEventListeners.remove(listener);
		}
	}

	public void connectionClosed() {
		LOG.debug("connectionClosed()");
		ConnectionEvent ce = new ConnectionEvent(this.managedConnection,
				ConnectionEvent.CONNECTION_CLOSED);
		dispatchConnectionEvent(ce);
	}

	public void connectionClosedOnError(Exception e) {
		LOG.debug("connectionClosedOnError()");
		ConnectionEvent ce = new ConnectionEvent(this.managedConnection,
				ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);
		dispatchConnectionEvent(ce);
	}

	public void reconnectingIn(int secs) {
		LOG.debug("reconnectingIn(" + secs + " seconds)");
	}

	public void reconnectionFailed(Exception e) {
		LOG.debug("reconnectionFailed()");
		ConnectionEvent ce = new ConnectionEvent(this.managedConnection,
				ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);
		dispatchConnectionEvent(ce);
	}

	public void reconnectionSuccessful() {
		LOG.debug("reconnectionSuccessful()");
	}

	private void dispatchConnectionEvent(ConnectionEvent ce) {
		for (ConnectionEventListener listener : connectionEventListeners) {
			switch (ce.getId()) {
			case ConnectionEvent.CONNECTION_CLOSED:
				listener.connectionClosed(ce);
				break;
			case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
				listener.localTransactionStarted(ce);
				break;
			case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
				listener.localTransactionCommitted(ce);
				break;
			case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
				listener.localTransactionRolledback(ce);
				break;
			case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
				listener.connectionErrorOccurred(ce);
				break;
			default:
				throw new IllegalArgumentException("ILLEGAL_EVENT_TYPE"
						+ ce.getId());
			}
		}
	}

	private void dispatchXmppMessage(XmppMessage xmppMessage) {
		synchronized (messageListeners) {
			for (XmppMessageListener listener : messageListeners) {
				try {
					listener.onMessage(xmppMessage);
				} catch (Exception e) {
					LOG.error("Could not dispatch message to listener", e);
				}
			}
		}
	}

	private void addRosterEntry(String jid) {
		try {
			Roster roster = this.connection.getRoster();
			roster.createEntry(jid, jid, null);
			sendMessage(jid, "Welcome!");
		} catch (Exception e) {
			LOG.error("Could not create roster entry for " + jid, e);
		}
	}

	private void removeRosterEntry(String jid) {
		try {
			Roster roster = this.connection.getRoster();
			RosterEntry entry = roster.getEntry(jid);
			if (entry != null) {
				roster.removeEntry(entry);
			}
		} catch (Exception e) {
			LOG.error("Could not remove roster entry for " + jid, e);
		}
	}

	class MessagePacketProcessor implements PacketListener {

		private final XmppConnectionRequestInfo connectionRequestInfo;

		public MessagePacketProcessor(
				XmppConnectionRequestInfo connectionRequestInfo) {
			this.connectionRequestInfo = connectionRequestInfo;
		}

		public void processPacket(Packet packet) {
			try {
				if (packet instanceof Message) {
					Message message = (Message) packet;
					if (message.getBody() != null
							&& !"".equals(message.getBody())) {
						XmppMessage xmppMessage = new XmppMessage();
						xmppMessage.setFrom(message.getFrom());
						xmppMessage.setTo(message.getTo());
						xmppMessage.setPacketId(message.getPacketID());
						xmppMessage.setBody(message.getBody());
						xmppMessage.setSubject(message.getSubject());
						xmppMessage.setThreadId(message.getThread());
						xmppMessage.setType(message.getType().toString());
						xmppMessage
								.setConnectionSpec(this.connectionRequestInfo);
						dispatchXmppMessage(xmppMessage);
					}
					LOG.debug("Discarding packet (body is empty): " + packet);
				} else {
					LOG.debug("Discarding packet (not recognized): " + packet);
				}
			} catch (Exception e) {
				LOG.error("Could not process packet received", e);
			}
		}
	}

	class PresencePacketProcessor implements PacketListener {

		public PresencePacketProcessor() {
		}

		public void processPacket(Packet packet) {
			try {
				if (packet instanceof Presence) {
					Presence presence = (Presence) packet;
					LOG.debug("Received presence packet: " + presence);
					if (Presence.Type.subscribe.equals(presence.getType())) {
						addRosterEntry(presence.getFrom());
					} else if (Presence.Type.unsubscribe.equals(presence
							.getType())) {
						removeRosterEntry(presence.getFrom());
					}
				}
			} catch (Exception e) {
				LOG.error("Could not process packet received", e);
			}
		}
	}
}
