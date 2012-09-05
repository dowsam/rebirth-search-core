/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RobinEngineModule.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine.robin;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.index.engine.Engine;

/**
 * The Class RobinEngineModule.
 *
 * @author l.xue.nong
 */
public class RobinEngineModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Engine.class).to(RobinEngine.class).asEagerSingleton();
	}
}
