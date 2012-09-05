/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClientTransportModule.java 2012-7-6 14:29:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.transport;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.client.transport.support.InternalTransportAdminClient;
import cn.com.rebirth.search.core.client.transport.support.InternalTransportClient;
import cn.com.rebirth.search.core.client.transport.support.InternalTransportClusterAdminClient;
import cn.com.rebirth.search.core.client.transport.support.InternalTransportIndicesAdminClient;

/**
 * The Class ClientTransportModule.
 *
 * @author l.xue.nong
 */
public class ClientTransportModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(InternalTransportClient.class).asEagerSingleton();
		bind(InternalTransportAdminClient.class).asEagerSingleton();
		bind(InternalTransportIndicesAdminClient.class).asEagerSingleton();
		bind(InternalTransportClusterAdminClient.class).asEagerSingleton();
		bind(TransportClientNodesService.class).asEagerSingleton();
	}
}
