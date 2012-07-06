/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DummyRiverModule.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river.dummy;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.river.River;


/**
 * The Class DummyRiverModule.
 *
 * @author l.xue.nong
 */
public class DummyRiverModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(River.class).to(DummyRiver.class).asEagerSingleton();
	}
}
