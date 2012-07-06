/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NoneIndexGatewayModule.java 2012-3-29 15:02:08 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexGateway.class).to(NoneIndexGateway.class).asEagerSingleton();
	}
}
