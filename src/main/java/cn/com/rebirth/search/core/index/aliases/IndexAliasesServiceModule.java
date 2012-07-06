/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexAliasesServiceModule.java 2012-3-29 15:02:12 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexAliasesService.class).asEagerSingleton();
	}
}