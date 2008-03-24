package com.grapevineim.xmpp.ra.outbound;

import java.security.Principal;

import javax.resource.spi.ManagedConnectionMetaData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The ManagedConnectionMetaData interface provides information about the
 * underlying EIS instance associated with a ManagedConnection instance. An
 * application server uses this information to get runtime information about a
 * connected EIS instance.
 */

public class XmppManagedConnectionMetaData implements ManagedConnectionMetaData {
	private final String userName;

	private final static Log LOG = LogFactory.getLog(XmppManagedConnectionMetaData.class);

	public XmppManagedConnectionMetaData(String userName) {
		this.userName = userName;
	}

	public String getEISProductName() {
		return "XMPPConnector";
	}

	public String getEISProductVersion() {
		return "1.0";
	}

	public int getMaxConnections() {
		return 0; // unlimited
	}

	public String getUserName() {	
		return userName;
	}
}
