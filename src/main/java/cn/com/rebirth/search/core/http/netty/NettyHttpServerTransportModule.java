/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NettyHttpServerTransportModule.java 2012-4-25 10:02:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.http.netty;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.http.HttpServerTransport;


/**
 * The Class NettyHttpServerTransportModule.
 *
 * @author l.xue.nong
 */
public class NettyHttpServerTransportModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(HttpServerTransport.class).to(NettyHttpServerTransport.class).asEagerSingleton();
	}
}
