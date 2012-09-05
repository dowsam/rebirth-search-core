/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ZenDiscoveryModule.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.zen;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.discovery.Discovery;
import cn.com.rebirth.search.core.discovery.zen.ping.ZenPingService;

/**
 * The Class ZenDiscoveryModule.
 *
 * @author l.xue.nong
 */
public class ZenDiscoveryModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ZenPingService.class).asEagerSingleton();
		bindDiscovery();
	}

	/**
	 * Bind discovery.
	 */
	protected void bindDiscovery() {
		bind(Discovery.class).to(ZenDiscovery.class).asEagerSingleton();
	}
}
