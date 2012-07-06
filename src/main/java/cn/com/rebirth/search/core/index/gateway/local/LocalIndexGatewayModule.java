/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalIndexGatewayModule.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway.local;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;

/**
 * The Class LocalIndexGatewayModule.
 *
 * @author l.xue.nong
 */
public class LocalIndexGatewayModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexGateway.class).to(LocalIndexGateway.class).asEagerSingleton();
	}
}
