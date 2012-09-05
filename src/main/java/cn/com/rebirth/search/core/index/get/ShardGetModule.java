/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardGetModule.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.get;

import cn.com.rebirth.core.inject.AbstractModule;

/**
 * The Class ShardGetModule.
 *
 * @author l.xue.nong
 */
public class ShardGetModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ShardGetService.class).asEagerSingleton();
	}
}
