package com.grapevineim.xmpp.ra.outbound;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

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
	private final Set<XmppConnection> connections;
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
		this.connections = new HashSet<XmppConnection>();
		this.metaData = new XmppManagedConnectionMetaData(this);
	}

	public Object getConnection(Subject subject,
			ConnectionRequestInfo connectionRequestInfo)
			throws ResourceException {
		LOG.debug("getConnection(Subject, ConnectionRequestInfo)");
		try {
			XmppConnection conn = new XmppConnectionImpl(this,
					(XmppConnectionRequestInfo) connectionRequestInfo);
			addXmppConnection(conn);
			return conn;
		} catch (Exception e) {
			LOG.error("Could not create XmppConnectionImpl", e);
			throw new ResourceException(e.getMessage());
		}
	}

	public void destroy() throws ResourceException {
		LOG.debug("destroy()");
		closeConnections();
	}

	public void cleanup() throws ResourceException {
		LOG.debug("cleanup()");
		closeConnections();
	}

	private void closeConnections() {
		LOG.debug("closeConnections()");
		synchronized (connections) {
			for (XmppConnection connection : connections) {
				try {
					connection.close();
				} catch (Exception e) {
					LOG.error("Could not close connection", e);
				}
			}
			connections.clear();
		}
	}

	public void associateConnection(Object connection) throws ResourceException {
		LOG.debug("associateConnection(Object)");
		if (connection instanceof XmppConnectionImpl) {
			XmppConnectionImpl xmppConnectionImpl = (XmppConnectionImpl) connection;
			XmppManagedConnection mc = (XmppManagedConnection) xmppConnectionImpl.getManagedConnection();
			mc.removeXmppConnection(xmppConnectionImpl);
			this.addXmppConnection(xmppConnectionImpl);
		} else {
			throw new ResourceException(
					"Connection is not of the expected type");
		}
	}

	public void addConnectionEventListener(ConnectionEventListener listener) {
		LOG.debug("addConnectionEventListener(ConnectionEventListener)");
		synchronized (connections) {
			for (XmppConnection connection : connections) {
				try {
					((XmppConnectionImpl) connection)
							.addConnectionEventListener(listener);
				} catch (Exception e) {
					LOG.error("Could not add connection event listener", e);
				}
			}
		}
	}

	public void removeConnectionEventListener(ConnectionEventListener listener) {
		LOG.debug("removeConnectionEventListener(ConnectionEventListener)");
		synchronized (connections) {
			for (XmppConnection connection : connections) {
				try {
					((XmppConnectionImpl) connection)
							.removeConnectionEventListener(listener);
				} catch (Exception e) {
					LOG.error("Could not add connection event listener", e);
				}
			}
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

	private void addXmppConnection(XmppConnection con) {
		LOG.debug("addXmppConnection(XmppConnection)");
		synchronized (connections) {
			connections.add(con);
		}
	}

	public void removeXmppConnection(XmppConnection con) {
		LOG.debug("removeXmppConnection(XmppConnection)");
		synchronized (connections) {
			connections.remove(con);
		}
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
