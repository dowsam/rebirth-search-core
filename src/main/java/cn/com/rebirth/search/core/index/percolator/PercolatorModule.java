/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PercolatorModule.java 2012-7-6 14:30:11 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(PercolatorExecutor.class).asEagerSingleton();
		bind(PercolatorService.class).asEagerSingleton();
	}
}
