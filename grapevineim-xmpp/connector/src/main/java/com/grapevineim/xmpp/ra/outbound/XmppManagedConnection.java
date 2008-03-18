package com.grapevineim.xmpp.ra.outbound;

import java.io.PrintWriter;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.grapevineim.xmpp.XmppConnection;

public class XmppManagedConnection implements ManagedConnection {
	private final XmppManagedConnectionFactory mcf;
	private XmppConnection connection;
	private final ConnectionRequestInfo connectionRequestInfo;
	private final ManagedConnectionMetaData metaData;
	private final Subject subject;
	private PrintWriter out;

	private static final Log LOG = LogFactory
			.getLog(XmppManagedConnection.class);

	public XmppManagedConnection(XmppManagedConnectionFactory mcf,
			Subject subject, ConnectionRequestInfo connectionRequestInfo)
			throws ResourceException {
		this.mcf = mcf;
		this.subject = subject;
		this.connectionRequestInfo = connectionRequestInfo;
		this.metaData = new XmppManagedConnectionMetaData(this);
	}

	public Object getConnection(Subject subject,
			ConnectionRequestInfo connectionRequestInfo)
			throws ResourceException {
		LOG.debug("getConnection(Subject, ConnectionRequestInfo)");
		try {
			if (this.connection == null) {
				this.connection = new XmppConnectionImpl(this,
						(XmppConnectionRequestInfo) connectionRequestInfo);
			}
			return this.connection;
		} catch (Exception e) {
			LOG.error("Could not create XmppConnectionImpl", e);
			throw new ResourceException(e.getMessage());
		}
	}
		
	public void destroy() throws ResourceException {
		LOG.debug("destroy()");
		closeConnection();
	}

	public void cleanup() throws ResourceException {
		LOG.debug("cleanup()");
		closeConnection();
	}

	private void closeConnection() {
		LOG.debug("closeConnection()");
		if (this.connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				LOG.error("Could not close connection", e);
			}
			this.connection = null;
		}
	}

	public void associateConnection(Object connection) throws ResourceException {
		LOG.debug("associateConnection(Object)");
		if (connection instanceof XmppConnection) {
			XmppConnection xmppConnection = (XmppConnection) connection;
			this.connection = xmppConnection;
		} else {
			throw new ResourceException(
					"Connection is not of the expected type");
		}
	}

	public void addConnectionEventListener(ConnectionEventListener listener) {
		LOG.debug("addConnectionEventListener(ConnectionEventListener)");
		try {
			((XmppConnectionImpl) this.connection)
					.addConnectionEventListener(listener);
		} catch (Exception e) {
			LOG.error("Could not add connection event listener", e);
		}
	}

	public void removeConnectionEventListener(ConnectionEventListener listener) {
		LOG.debug("removeConnectionEventListener(ConnectionEventListener)");
		try {
			((XmppConnectionImpl) this.connection)
					.removeConnectionEventListener(listener);
		} catch (Exception e) {
			LOG.error("Could not add connection event listener", e);
		}
	}

	public XAResource getXAResource() throws ResourceException {
		throw new NotSupportedException("NO_XATRANSACTION");
	}

	public LocalTransaction getLocalTransaction() throws ResourceException {
		throw new NotSupportedException("NO_TRANSACTION");
	}

	public ManagedConnectionMetaData getMetaData() throws ResourceException {
		return this.metaData;
	}

	public void setLogWriter(PrintWriter out) throws ResourceException {
		this.out = out;
	}

	public PrintWriter getLogWriter() throws ResourceException {
		return out;
	}

	public XmppManagedConnectionFactory getManagedConnectionFactory() {
		return this.mcf;
	}

	public Subject getSubject() {
		return subject;
	}

	public ConnectionRequestInfo getConnectionRequestInfo() {
		return this.connectionRequestInfo;
	}
}
