/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RobinEngineModule.java 2012-3-29 15:01:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine.robin;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.engine.Engine;

/**
 * The Class RobinEngineModule.
 *
 * @author l.xue.nong
 */
public class RobinEngineModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Engine.class).to(RobinEngine.class).asEagerSingleton();
	}
}
