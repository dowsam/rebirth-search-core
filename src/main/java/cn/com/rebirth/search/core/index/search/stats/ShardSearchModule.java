/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardSearchModule.java 2012-7-6 14:29:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.stats;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.search.slowlog.ShardSlowLogSearchService;

/**
 * The Class ShardSearchModule.
 *
 * @author l.xue.nong
 */
public class ShardSearchModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ShardSearchService.class).asEagerSingleton();
		bind(ShardSlowLogSearchService.class).asEagerSingleton();
	}
}
