/****************************************************************************** 
* $Id$
* 
* Copyright 2008 GrapevineIM (http://www.grapevine.im)
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License. 
* You may obtain a copy of the License at 
* 
* http://www.apache.org/licenses/LICENSE-2.0 
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions 
* and limitations under the License. 
* 
******************************************************************************/

package com.grapevineim.xmpp.ra;

import java.io.Serializable;
import java.util.HashMap;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.grapevineim.xmpp.ra.inbound.ActivationSpecImpl;
import com.grapevineim.xmpp.ra.inbound.EndpointConsumer;

/**
 *  
 * @author alex
 * 
 */
public class ResourceAdapterImpl implements ResourceAdapter, Serializable {

	private static final long serialVersionUID = -1;
	private static final Log LOG = LogFactory.getLog(ResourceAdapterImpl.class);
	private final HashMap<MessageEndpointFactory, EndpointConsumer> endpointConsumers = new HashMap<MessageEndpointFactory, EndpointConsumer>();
	private WorkManager workManager = null;

	public void start(BootstrapContext ctx)
			throws ResourceAdapterInternalException {

		// get the work manager
		this.workManager = ctx.getWorkManager();

	}

	public void stop() {

	}

	public void endpointActivation(
			MessageEndpointFactory messageEndpointFactory, ActivationSpec spec)
			throws NotSupportedException {
		LOG.info("[RA.endpointActivation()] Entered");

		try {
			EndpointConsumer ec = new EndpointConsumer(workManager,
					messageEndpointFactory, (ActivationSpecImpl) spec);
			synchronized (endpointConsumers) {
				endpointConsumers.put(messageEndpointFactory, ec);
			}
		} catch (Exception ex) {
			LOG
					.error("[RA.endpointActivation()] An Exception was caught while activating the endpoint");
			LOG
					.error("[RA.endpointActivation()] Please check the server logs for details");
			throw new NotSupportedException("Activation failed", ex);
		}
	}

	public void endpointDeactivation(
			MessageEndpointFactory messageEndpointFactory, ActivationSpec spec) {
		LOG.info("[RA.endpointdeactivation()] Entered");
		try {
			synchronized (endpointConsumers) {
				EndpointConsumer ec = endpointConsumers
						.remove(messageEndpointFactory);
				ec.cleanup();
			}
		} catch (Exception ex) {
			LOG
					.error("[RA.endpointActivation()] An Exception was caught while deactivating the endpoint");
		}
	}

	public XAResource[] getXAResources(ActivationSpec[] specs)
			throws ResourceException {

		return null;
	}

}
