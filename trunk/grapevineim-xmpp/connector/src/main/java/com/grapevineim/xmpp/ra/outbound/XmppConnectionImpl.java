package com.grapevineim.xmpp.ra.outbound;

import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;

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
import com.grapevineim.xmpp.ra.ResourceAdapterImpl;

/**
 * Application-level connection handle that is used by a client component to
 * access an EIS instance.
 */

public class XmppConnectionImpl implements XmppConnection {

	private final Set<XmppMessageListener> messageListeners;
	private final XMPPConnection connection;
	private final ConnectionListenerImpl connectionListener;
	private final XmppConnectionRequestInfo connectionRequestInfo;
	private final MessagePacketProcessor messagePacketProcessor;
	private final PresencePacketProcessor presencePacketProcessor;
	private final WorkManager workManager;

	private static final Log LOG = LogFactory.getLog(XmppConnectionImpl.class);

	public XmppConnectionImpl(ResourceAdapter ra,
			ManagedConnection managedConnection,
			XmppConnectionRequestInfo connectionRequestInfo)
			throws ResourceException {
		LOG.info("Constructor");
		this.workManager = ((ResourceAdapterImpl) ra).getWorkManager();
		this.messageListeners = new HashSet<XmppMessageListener>();
		this.connection = getConnection(connectionRequestInfo);
		this.connectionRequestInfo = connectionRequestInfo;
		this.connectionListener = new ConnectionListenerImpl(managedConnection);
		this.messagePacketProcessor = new MessagePacketProcessor(
				this.connectionRequestInfo);
		this.presencePacketProcessor = new PresencePacketProcessor(
				this.connection);
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

	public void setPresence(String type, String status)
			throws ResourceException {
		LOG.info("setPresence(String,String)");
		try {
			this.setPresence(this.connection, type, status);
		} catch (Exception e) {
			LOG.error("Could not setPresence.", e);
			throw new ResourceException("Could not setPresence.", e);
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

	private XMPPConnection getConnection(
			XmppConnectionRequestInfo connectionRequestInfo)
			throws ResourceException {

		try {
			ConnectionConfiguration config = new ConnectionConfiguration(
					connectionRequestInfo.getHost(), connectionRequestInfo
							.getPort().intValue(), connectionRequestInfo
							.getDomain());
			return new XMPPConnection(config);

		} catch (Exception e) {
			LOG.error("Could not create connection.", e);
			throw new ResourceException("Could not connect", e);
		}
	}

	public void open() throws ResourceException {
		try {
			if (!this.connection.isConnected()) {
				this.connection.connect();
				this.connection.addConnectionListener(this.connectionListener);

				addMessagePacketListener(this.connection,
						this.messagePacketProcessor, connectionRequestInfo
								.getUsername());

				addPresencePacketListener(this.connection,
						this.presencePacketProcessor,
						this.connectionRequestInfo.getUsername());

				login(this.connection,
						this.connectionRequestInfo.getUsername(),
						this.connectionRequestInfo.getPassword());

				acceptSubscriptionsManually(this.connection);
			}
		} catch (Exception e) {
			LOG.error("Could not open connection.", e);
			throw new ResourceException("Could not open connection", e);
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

	private void login(XMPPConnection conn, String username, String password)
			throws ResourceException {
		try {
			conn.login(username, password);
		} catch (XMPPException xe) {
			LOG.error("Could not login.", xe);
			throw new ResourceException("Could not login", xe);
		}
	}

	private void setPresence(XMPPConnection conn, String type, String status)
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
				this.connection
						.removeConnectionListener(this.connectionListener);
				this.connection
						.removePacketListener(this.messagePacketProcessor);
				this.connection
						.removePacketListener(this.presencePacketProcessor);
				this.connection.disconnect();
				this.connectionListener.connectionClosed();
			}
		} catch (Exception e) {
			LOG.error("Could not disconnect", e);
			throw new ResourceException("Could not disconnect", e);
		}
	}

	public void addMessageListener(XmppMessageListener l)
			throws ResourceException {
		synchronized (messageListeners) {
			messageListeners.add(l);
		}
	}

	public void removeMessageListener(XmppMessageListener l)
			throws ResourceException {
		synchronized (messageListeners) {
			messageListeners.remove(l);
		}
	}

	public void addConnectionEventListener(ConnectionEventListener listener) {
		this.connectionListener.addConnectionEventListener(listener);
	}

	public void removeConnectionEventListener(ConnectionEventListener listener) {
		this.connectionListener.removeConnectionEventListener(listener);
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

		private void dispatchXmppMessage(XmppMessage xmppMessage) {
			try {
				Worker worker = new Worker(xmppMessage);	
				workManager.doWork(worker);
			}
			catch(Exception e) {
				LOG.error("Could not schedule work", e);
			}
		}

		class Worker implements Work {

			private final XmppMessage xmppMessage;

			public Worker(XmppMessage xmppMessage) {
				this.xmppMessage = xmppMessage;
			}

			public void release() {

			}

			public void run() {
				Set<XmppMessageListener> copy = new HashSet<XmppMessageListener>();
				synchronized (messageListeners) {
					copy.addAll(messageListeners);
				}

				for (XmppMessageListener listener : copy) {
					try {
						listener.onMessage(this.xmppMessage);
					} catch (Exception e) {
						LOG
								.error(
										"Could not dispatch message to XmppMessageListenerImpl",
										e);
					}
				}
			}
		}
	}

	class PresencePacketProcessor implements PacketListener {

		private final XMPPConnection connection;

		public PresencePacketProcessor(XMPPConnection connection) {
			this.connection = connection;
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
	}

	class ConnectionListenerImpl implements ConnectionListener {

		private final ManagedConnection managedConnection;
		private final Set<ConnectionEventListener> connectionEventListeners;

		public ConnectionListenerImpl(ManagedConnection mc) {
			this.managedConnection = mc;
			this.connectionEventListeners = new HashSet<ConnectionEventListener>();
		}

		public void addConnectionEventListener(ConnectionEventListener listener) {
			synchronized (this.connectionEventListeners) {
				this.connectionEventListeners.add(listener);
			}
		}

		public void removeConnectionEventListener(
				ConnectionEventListener listener) {
			synchronized (this.connectionEventListeners) {
				this.connectionEventListeners.remove(listener);
			}
		}

		public void connectionClosed() {
			LOG.debug("connectionClosed()");
			dispatchConnectionEvent(new ConnectionEvent(this.managedConnection,
					ConnectionEvent.CONNECTION_CLOSED));
		}

		public void connectionClosedOnError(Exception e) {
			LOG.debug("connectionClosedOnError()");
			dispatchConnectionEvent(new ConnectionEvent(this.managedConnection,
					ConnectionEvent.CONNECTION_ERROR_OCCURRED, e));
		}

		public void reconnectingIn(int secs) {
			LOG.debug("reconnectingIn(" + secs + " seconds)");
		}

		public void reconnectionFailed(Exception e) {
			LOG.debug("reconnectionFailed()");
			dispatchConnectionEvent(new ConnectionEvent(this.managedConnection,
					ConnectionEvent.CONNECTION_ERROR_OCCURRED, e));
		}

		public void reconnectionSuccessful() {
			LOG.debug("reconnectionSuccessful()");
		}

		private void dispatchConnectionEvent(ConnectionEvent ce) {
			try {
				Worker worker = new Worker(this, ce);
				workManager.scheduleWork(worker);
			} catch (Exception e) {
				LOG.error("Could not schedule work", e);
			}
		}

		class Worker implements Work {

			private final ConnectionListenerImpl connectionListenerImpl;
			private final ConnectionEvent connectionEvent;

			public Worker(ConnectionListenerImpl connectionListenerImpl,
					ConnectionEvent connectionEvent) {
				this.connectionListenerImpl = connectionListenerImpl;
				this.connectionEvent = connectionEvent;
			}

			public void release() {

			}

			public void run() {
				Set<ConnectionEventListener> listeners = new HashSet<ConnectionEventListener>();
				synchronized (this.connectionListenerImpl.connectionEventListeners) {
					listeners
							.addAll(this.connectionListenerImpl.connectionEventListeners);
				}
				for (ConnectionEventListener listener : listeners) {
					switch (connectionEvent.getId()) {
					case ConnectionEvent.CONNECTION_CLOSED:
						listener.connectionClosed(connectionEvent);
						break;
					case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
						listener.localTransactionStarted(connectionEvent);
						break;
					case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
						listener.localTransactionCommitted(connectionEvent);
						break;
					case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
						listener.localTransactionRolledback(connectionEvent);
						break;
					case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
						listener.connectionErrorOccurred(connectionEvent);
						break;
					default:
						throw new IllegalArgumentException("ILLEGAL_EVENT_TYPE"
								+ connectionEvent.getId());
					}
				}
			}
		}
	}
}
