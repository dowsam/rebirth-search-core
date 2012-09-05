/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NettyHttpServerTransportModule.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http.netty;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.http.HttpServerTransport;

/**
 * The Class NettyHttpServerTransportModule.
 *
 * @author l.xue.nong
 */
public class NettyHttpServerTransportModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(HttpServerTransport.class).to(NettyHttpServerTransport.class).asEagerSingleton();
	}
}
