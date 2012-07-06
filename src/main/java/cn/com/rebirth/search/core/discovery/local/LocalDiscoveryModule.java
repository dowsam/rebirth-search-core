/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core LocalDiscoveryModule.java 2012-3-29 15:01:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery.local;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.discovery.Discovery;


/**
 * The Class LocalDiscoveryModule.
 *
 * @author l.xue.nong
 */
public class LocalDiscoveryModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Discovery.class).to(LocalDiscovery.class).asEagerSingleton();
	}
}
