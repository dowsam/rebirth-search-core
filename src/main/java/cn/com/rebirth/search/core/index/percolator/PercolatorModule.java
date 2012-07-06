/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PercolatorModule.java 2012-3-29 15:00:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.percolator;

import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class PercolatorModule.
 *
 * @author l.xue.nong
 */
public class PercolatorModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(PercolatorExecutor.class).asEagerSingleton();
		bind(PercolatorService.class).asEagerSingleton();
	}
}
