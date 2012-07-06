/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesAnalysisModule.java 2012-7-6 14:29:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.analysis;

import cn.com.rebirth.search.commons.inject.AbstractModule;

/**
 * The Class IndicesAnalysisModule.
 *
 * @author l.xue.nong
 */
public class IndicesAnalysisModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndicesAnalysisService.class).asEagerSingleton();
	}
}