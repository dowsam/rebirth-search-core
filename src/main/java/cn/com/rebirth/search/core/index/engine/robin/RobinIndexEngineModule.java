/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RobinIndexEngineModule.java 2012-3-29 15:01:07 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexEngine.class).to(RobinIndexEngine.class).asEagerSingleton();
	}
}
