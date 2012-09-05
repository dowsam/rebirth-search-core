/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DummyRiverModule.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river.dummy;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.river.River;

/**
 * The Class DummyRiverModule.
 *
 * @author l.xue.nong
 */
public class DummyRiverModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(River.class).to(DummyRiver.class).asEagerSingleton();
	}
}
