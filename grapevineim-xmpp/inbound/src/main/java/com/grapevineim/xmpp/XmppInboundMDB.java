package com.grapevineim.xmpp;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.grapevineim.xmpp.XmppMessage;
import com.grapevineim.xmpp.XmppMessageListener;

public class XmppInboundMDB implements MessageDrivenBean, XmppMessageListener {

	private static final long serialVersionUID = -1;
	private static final Log LOG = LogFactory.getLog(XmppInboundMDB.class);

	private MessageDrivenContext mdc;

	public XmppInboundMDB() {
		LOG.info("<MDB> In XmppInboundMDB.XmppMessageBean()");
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
		LOG.info("<MDB> In XmppInboundMDB.setMessageDrivenContext()");
		this.mdc = mdc;
	}

	public void ejbCreate() {
		LOG.info("<MDB> In XmppInboundMDB.ejbCreate()");
	}

	public void onMessage(XmppMessage message) {
		try {
			LOG.info("<MDB> ---- Inbound MDB got a message: " + message.getBody());
			enqueue(message);
		} catch (Exception e) {
			LOG.info("<MDB> Could not receive message: ", e);
			mdc.setRollbackOnly();
			throw new EJBException(e);
		}
	}

	public void ejbRemove() {
		LOG.info("<MDB> In XmppInboundMDB.remove()");
	}

	private void enqueue(XmppMessage m) {		 
		Connection connection = null;
		try {
			Context ctx = new InitialContext();
            ConnectionFactory     connectionFactory = (ConnectionFactory)ctx.lookup("jms/XmppQueueConnectionFactory");
            Queue queue = (Queue)ctx.lookup("jms/xmpp/messagequeue");
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(queue);
            ObjectMessage objMessage = session.createObjectMessage(m);
            messageProducer.send(objMessage);
		} catch (Exception e) {
			LOG.error("<MDB> Error", e);
		}
		finally {
			try {
				if(connection != null) {
					connection.close();
				}
			}
			catch(Exception e) {
				LOG.error("<MDB> Error", e);
			}
		}
	}
}
