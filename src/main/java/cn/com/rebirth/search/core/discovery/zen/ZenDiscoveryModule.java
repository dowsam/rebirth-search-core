/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ZenDiscoveryModule.java 2012-3-29 15:01:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery.zen;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.discovery.Discovery;
import cn.com.rebirth.search.core.discovery.zen.ping.ZenPingService;


/**
 * The Class ZenDiscoveryModule.
 *
 * @author l.xue.nong
 */
public class ZenDiscoveryModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
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
