/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardIndexingModule.java 2012-3-29 15:01:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.indexing;

import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class ShardIndexingModule.
 *
 * @author l.xue.nong
 */
public class ShardIndexingModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ShardIndexingService.class).asEagerSingleton();
	}
}
