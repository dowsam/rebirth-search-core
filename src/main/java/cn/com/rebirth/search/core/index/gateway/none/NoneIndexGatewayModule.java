/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoneIndexGatewayModule.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway.none;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;

/**
 * The Class NoneIndexGatewayModule.
 *
 * @author l.xue.nong
 */
public class NoneIndexGatewayModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexGateway.class).to(NoneIndexGateway.class).asEagerSingleton();
	}
}
