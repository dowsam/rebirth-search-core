/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalDiscoveryModule.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.local;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.discovery.Discovery;

/**
 * The Class LocalDiscoveryModule.
 *
 * @author l.xue.nong
 */
public class LocalDiscoveryModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Discovery.class).to(LocalDiscovery.class).asEagerSingleton();
	}
}
