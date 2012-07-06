/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexAliasesServiceModule.java 2012-7-6 14:28:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.aliases;

import cn.com.rebirth.search.commons.inject.AbstractModule;

/**
 * The Class IndexAliasesServiceModule.
 *
 * @author l.xue.nong
 */
public class IndexAliasesServiceModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexAliasesService.class).asEagerSingleton();
	}
}