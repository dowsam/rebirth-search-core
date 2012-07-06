/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RobinIndexEngineModule.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine.robin;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.engine.IndexEngine;

/**
 * The Class RobinIndexEngineModule.
 *
 * @author l.xue.nong
 */
public class RobinIndexEngineModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexEngine.class).to(RobinIndexEngine.class).asEagerSingleton();
	}
}
