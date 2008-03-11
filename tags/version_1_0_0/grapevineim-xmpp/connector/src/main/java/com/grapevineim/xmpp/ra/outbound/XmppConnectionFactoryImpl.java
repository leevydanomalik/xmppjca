package com.grapevineim.xmpp.ra.outbound;

import java.io.PrintWriter;
import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionSpec;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.grapevineim.xmpp.XmppConnection;
import com.grapevineim.xmpp.XmppConnectionFactory;

public class XmppConnectionFactoryImpl implements XmppConnectionFactory, Serializable {
	
	private static final long serialVersionUID = -1;
	private static final Log LOG = LogFactory.getLog(XmppConnectionFactoryImpl.class);

	private final ManagedConnectionFactory mcf;
	private final ConnectionManager cm;
	private PrintWriter out;

	public XmppConnectionFactoryImpl(ManagedConnectionFactory mcf,
			ConnectionManager cm) {
		this.mcf = mcf;
		this.cm = cm;
	}

	public XmppConnection createConnection(ConnectionSpec properties)
			throws ResourceException {
		LOG.debug("createConnection(ConnectionSpec)");
		XmppConnectionRequestInfo info = (XmppConnectionRequestInfo) properties;
		return (XmppConnection) cm.allocateConnection(this.mcf, info);
	}

	public void setLogWriter(PrintWriter out) throws ResourceException {
		this.out = out;
	}

	public PrintWriter getLogWriter() throws ResourceException {
		return this.out;
	}
}
