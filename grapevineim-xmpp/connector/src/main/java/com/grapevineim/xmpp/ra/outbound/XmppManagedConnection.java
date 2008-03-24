package com.grapevineim.xmpp.ra.outbound;

import java.io.PrintWriter;
import java.security.Principal;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapter;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XmppManagedConnection implements ManagedConnection {
	private final ManagedConnectionMetaData metaData;
	private final XmppConnectionImpl connection;
	private final ConnectionRequestInfo connectionRequestInfo;
	private PrintWriter out;

	private static final Log LOG = LogFactory
			.getLog(XmppManagedConnection.class);

	public XmppManagedConnection(XmppManagedConnectionFactory mcf,
			Subject subject, ConnectionRequestInfo connectionRequestInfo)
			throws ResourceException {
		this.metaData = new XmppManagedConnectionMetaData(getUserName(subject));
		this.connectionRequestInfo = connectionRequestInfo;
		this.connection = getConnection(mcf.getResourceAdapter(),connectionRequestInfo);		
	}
	
	private String getUserName(Subject subject) {
		if( subject != null ) {
			// return the first principal's name
			for(Principal principal : subject.getPrincipals()) {
				return principal.getName();
			}
		}
		return "unknown";
	}

	public Object getConnection(Subject subject,
			ConnectionRequestInfo connectionRequestInfo)
			throws ResourceException {
		LOG.debug("getConnection()");
		if(this.connectionRequestInfo.equals(connectionRequestInfo)) {
			return this.connection;
		}
		else {
			throw new NotSupportedException("Getting a connection with a different connectionRequestInfo is not supported");
		}
	}
	
	private XmppConnectionImpl getConnection(ResourceAdapter ra, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
		try {
			return new XmppConnectionImpl(ra, this,
					(XmppConnectionRequestInfo) connectionRequestInfo);
		} catch (Exception e) {
			LOG.error("Could not create XmppConnectionImpl", e);
			throw new ResourceException(
					"Could not create XmppConnectionImpl", e.getMessage());
		}
	}

	public void destroy() throws ResourceException {
		LOG.debug("destroy()");
	}

	public void cleanup() throws ResourceException {
		LOG.debug("cleanup()");
	}

	public void associateConnection(Object connection) throws ResourceException {
		LOG.debug("associateConnection(Object)");
		throw new NotSupportedException("associateConnection() not supported");
	}

	public void addConnectionEventListener(ConnectionEventListener listener) {
		LOG.debug("addConnectionEventListener(ConnectionEventListener)");
		try {
			this.connection.addConnectionEventListener(listener);		
		} catch (Exception e) {
			LOG.error("Could not add connection event listener", e);
		}
	}

	public void removeConnectionEventListener(ConnectionEventListener listener) {
		LOG.debug("removeConnectionEventListener(ConnectionEventListener)");
		try {
			this.connection.removeConnectionEventListener(listener);
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
}
