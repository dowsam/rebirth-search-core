/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardIndexingModule.java 2012-7-6 14:29:02 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.indexing;

import cn.com.rebirth.core.inject.AbstractModule;

/**
 * The Class ShardIndexingModule.
 *
 * @author l.xue.nong
 */
public class ShardIndexingModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ShardIndexingService.class).asEagerSingleton();
	}
}
