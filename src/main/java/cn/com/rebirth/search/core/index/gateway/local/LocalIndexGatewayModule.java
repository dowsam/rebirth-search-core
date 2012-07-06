/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core LocalIndexGatewayModule.java 2012-3-29 15:00:57 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexGateway.class).to(LocalIndexGateway.class).asEagerSingleton();
	}
}
