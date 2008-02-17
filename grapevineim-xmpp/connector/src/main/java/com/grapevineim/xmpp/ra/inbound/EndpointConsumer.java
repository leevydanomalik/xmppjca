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
import com.grapevineim.xmpp.ra.outbound.XmppConnectionRequestInfo;

public class EndpointConsumer implements XmppMessageListener {
	private static final Log LOG = LogFactory.getLog(EndpointConsumer.class);

	private final MessageEndpointFactory messageEndpointFactory;
	private final WorkManager workManager;
	private final XmppConnection connection;

	public EndpointConsumer(WorkManager workManager,
			MessageEndpointFactory messageEndpointFactory, ActivationSpecImpl as)
			throws Exception {
		this.workManager = workManager;
		this.messageEndpointFactory = messageEndpointFactory;
		this.connection = connect(as);
		this.connection.addMessageListener(this);
	}

	public void onMessage(XmppMessage xmppMessage) {
		try {
			workManager.scheduleWork(new DeliveryThread(messageEndpointFactory,
					xmppMessage));
		} catch (WorkException we) {
			LOG.error("Could not schedule work", we);
		}
	}

	private XmppConnection connect(ActivationSpecImpl activationSpec)
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
			LOG.error("<MDB> Error", e);
			throw e;
		}
	}

	private void disconnect() throws Exception {
		try {
			connection.close();
		} catch (Exception e) {
			LOG.error("<MDB> Error", e);
			throw e;
		}
	}

	public void cleanup() throws Exception {
		disconnect();
	}
}
