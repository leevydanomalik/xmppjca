package com.grapevineim.xmpp.ra.outbound;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XmppManagedConnectionFactory implements ManagedConnectionFactory,
		Serializable {

	private static final long serialVersionUID = -1;
	private static final Log LOG = LogFactory
			.getLog(XmppManagedConnectionFactory.class);
	private PrintWriter out;

	public XmppManagedConnectionFactory() {
	}

	public Object createConnectionFactory(ConnectionManager cxManager)
			throws ResourceException {
		LOG.debug("createConnectionFactory(ConnectionManager)");
		try {
			return new XmppConnectionFactoryImpl(this, cxManager);
		} catch (Exception e) {
			LOG.error("Could not create XmppConnectionFactoryImpl", e);
			throw new ResourceException(e);
		}
	}

	public Object createConnectionFactory() throws ResourceException {
		LOG.debug("createConnectionFactory()");
		try {
			return new XmppConnectionFactoryImpl(this, null);
		} catch (Exception e) {
			LOG.error("Could not create XmppConnectionFactoryImpl", e);
			throw new ResourceException(e);
		}
	}

	public ManagedConnection createManagedConnection(Subject subject,
			ConnectionRequestInfo cxRequestInfo) throws ResourceException {
		LOG.debug("createManagedConnection(Subject, ConnectionRequestInfo)");
		return new XmppManagedConnection(this, subject, cxRequestInfo);
	}

	public ManagedConnection matchManagedConnections(Set connectionSet,
			Subject subject, ConnectionRequestInfo cxRequestInfo)
			throws ResourceException {

		for (XmppManagedConnection conn : (Set<XmppManagedConnection>) connectionSet) {
			if (conn.getConnectionRequestInfo().equals(cxRequestInfo)) {
				LOG.debug("matchManagedConnections --> MATCHED");
				return conn;
			}
		}
		LOG.debug("matchManagedConnections --> NOT MATCHED");
		return null;
	}

	public void setLogWriter(PrintWriter out) throws ResourceException {
		this.out = out;
	}

	public PrintWriter getLogWriter() throws ResourceException {
		return this.out;
	}
}
