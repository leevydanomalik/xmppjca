package com.grapevineim.xmpp.ra.outbound;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides information about an EIS instance connected through a
 * Connection instance.
 */

public class XmppConnectionMetaData implements ConnectionMetaData {

	private final XmppConnectionImpl conn;

	private static final Log LOG = LogFactory
			.getLog(XmppConnectionMetaData.class);

	public XmppConnectionMetaData(XmppConnectionImpl conn) {
		this.conn = conn;
	}

	public String getEISProductName() throws ResourceException {
		return "XMPP Connector";
	}

	public String getEISProductVersion() {
		return "1.0";
	}

	public String getUserName() {
		return conn.getUser();
	}
}
