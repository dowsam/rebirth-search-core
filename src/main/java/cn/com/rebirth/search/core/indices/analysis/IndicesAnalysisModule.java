/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesAnalysisModule.java 2012-3-29 15:01:01 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndicesAnalysisService.class).asEagerSingleton();
	}
}