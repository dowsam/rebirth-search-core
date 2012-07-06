/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesQueriesModule.java 2012-7-6 14:29:11 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndicesQueriesRegistry.class).asEagerSingleton();
	}
}