package com.grapevineim.xmpp.ra.inbound;

import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.grapevineim.xmpp.XmppConnectionSpec;

public class ActivationSpecImpl extends XmppConnectionSpec implements ActivationSpec, Serializable {
	
	private static final Log LOG = LogFactory.getLog(ActivationSpecImpl.class);
	private static final long serialVersionUID = -1;

	private ResourceAdapter resourceAdapter = null;

	public ActivationSpecImpl() {
	}

	public void validate() throws InvalidPropertyException {
		checkProperty(getHost());
		checkProperty(getDomain());
		checkProperty(getUsername());
		checkProperty(getPassword());
		if (getPort() == null || getPort().intValue() < 1024) {
			LOG.error("port is invalid");
			throw new InvalidPropertyException("port is invalid");
		}
	}

	private void checkProperty(String s) throws InvalidPropertyException {
		if (StringUtils.isBlank(s)) {
			LOG.error(s + " is blank");
			throw new InvalidPropertyException(s + " is blank");
		}
	}

	public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
		this.resourceAdapter = ra;
	}

	public ResourceAdapter getResourceAdapter() {
		return resourceAdapter;
	}
}
