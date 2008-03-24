package com.grapevineim.xmpp.ra.inbound;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.grapevineim.xmpp.XmppConnection;
import com.grapevineim.xmpp.XmppConnectionFactory;
import com.grapevineim.xmpp.XmppMessage;
import com.grapevineim.xmpp.XmppMessageListener;
import com.grapevineim.xmpp.ra.outbound.XmppConnectionImpl;
import com.grapevineim.xmpp.ra.outbound.XmppConnectionRequestInfo;

public class XmppMessageListenerImpl implements XmppMessageListener {
	
	private static final Log LOG = LogFactory.getLog(XmppMessageListenerImpl.class);
	private static final String PRESENCE_STATUS_AVAILABLE ="available";
	private static final String PRESENCE_STATUS_UNAVAILABLE = "unavailable";
	private static final String INSTRUCTIONS = "Type 'help' for a list of commands";

	private final MessageEndpointFactory messageEndpointFactory;
	private final WorkManager workManager;
	private final XmppConnection connection;
	
	public XmppMessageListenerImpl(WorkManager workManager,
			MessageEndpointFactory messageEndpointFactory, ActivationSpecImpl as)
			throws Exception {
		this.workManager = workManager;
		this.messageEndpointFactory = messageEndpointFactory;
		this.connection = getConnection(as);
		this.connection.open();
		this.connection.addMessageListener(this);
		this.connection.setPresence(PRESENCE_STATUS_AVAILABLE, INSTRUCTIONS);
	}

	public void onMessage(XmppMessage xmppMessage) {
		try {
			workManager.scheduleWork(new DeliveryThread(messageEndpointFactory,
					xmppMessage));
		} catch (WorkException we) {
			LOG.error("Could not schedule work", we);
		}
	}

	/**
	 * By connection in this fashion, it will consumer a ManagedConnection from the pool. This is what I want.
	 * @param activationSpec
	 * @return
	 * @throws Exception
	 */
	private XmppConnection getConnection(ActivationSpecImpl activationSpec)
			throws Exception {
		try {
			Context ctx = new InitialContext();
			XmppConnectionFactory connectionFactory = (XmppConnectionFactory) ctx
					.lookup("eis/ra/XmppMessagingConnector");
			XmppConnectionRequestInfo connectionRequestInfo = new XmppConnectionRequestInfo();
			connectionRequestInfo.setUsername(activationSpec.getUsername());
			connectionRequestInfo.setPassword(activationSpec.getPassword());
			connectionRequestInfo.setHost(activationSpec.getHost());
			connectionRequestInfo.setPort(activationSpec.getPort());
			connectionRequestInfo.setDomain(activationSpec.getDomain());
			return connectionFactory.createConnection(connectionRequestInfo);
		} catch (Exception e) {
			LOG.error("Could not get connection", e);
			throw e;
		}
	}

	public void cleanup() throws Exception {
		try {
			this.connection.setPresence(PRESENCE_STATUS_UNAVAILABLE, "");
			this.connection.removeMessageListener(this);		
			this.connection.close();
		}
		catch(Exception e) {
			LOG.error("Could not cleanup", e);
			throw e;
		}
	}
}
