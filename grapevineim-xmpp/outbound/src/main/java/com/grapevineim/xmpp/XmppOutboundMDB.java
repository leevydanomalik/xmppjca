package com.grapevineim.xmpp;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XmppOutboundMDB implements MessageDrivenBean,
		MessageListener {

	private static final Log LOG = LogFactory
			.getLog(XmppOutboundMDB.class);

	private MessageDrivenContext mdc;

	public XmppOutboundMDB() {
		LOG.info("<MDB> In XmppOutboundMDB.XmppMessageBean()");
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
		LOG.info("<MDB> In XmppOutboundMDB.setMessageDrivenContext()");
		this.mdc = mdc;
	}

	public void ejbCreate() {
		LOG.info("<MDB> In XmppOutboundMDB.ejbCreate()");		
	}

	public void onMessage(Message message) {
		try {
			LOG.info("<MDB> ---- Outbound MDB got a message: " + message);
			if (message instanceof ObjectMessage) {
				XmppMessage xmppMessage = (XmppMessage) ((ObjectMessage) message)
						.getObject();
				xmppMessage.setBody("hello");
				xmppMessage.setPacketId(null);
				String to = xmppMessage.getFrom();
				xmppMessage.setFrom(xmppMessage.getTo());
				xmppMessage.setTo(to);
				reply(xmppMessage);
			}

		} catch (Exception e) {
			LOG.info("<MDB> Could not receive message: ", e);
			mdc.setRollbackOnly();
			throw new EJBException(e);
		}
	}

	public void ejbRemove() {
		LOG.info("<MDB> In XmppOutboundMDB.remove()");
	}

	private void reply(XmppMessage m) {
		XmppConnection connection = null;
		try {
			Context ctx = new InitialContext();
			XmppConnectionFactory connectionFactory = (XmppConnectionFactory) ctx
					.lookup("ra/XmppMessagingConnector");
			connection = connectionFactory.createConnection(m.getConnectionSpec());
			connection.sendMessage(m);
		} catch (Exception e) {
			LOG.error("<MDB> Error", e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				LOG.error("<MDB> Error", e);
			}
		}
	}	
}
