/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesQueriesModule.java 2012-3-29 15:02:27 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices.query;

import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class IndicesQueriesModule.
 *
 * @author l.xue.nong
 */
public class IndicesQueriesModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndicesQueriesRegistry.class).asEagerSingleton();
	}
}