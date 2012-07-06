/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FsIndexGatewayModule.java 2012-3-29 15:01:22 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway.fs;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;


/**
 * The Class FsIndexGatewayModule.
 *
 * @author l.xue.nong
 */
public class FsIndexGatewayModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexGateway.class).to(FsIndexGateway.class).asEagerSingleton();
	}
}
