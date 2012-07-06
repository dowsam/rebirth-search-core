/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ThreadPoolModule.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.threadpool;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;

/**
 * The Class ThreadPoolModule.
 *
 * @author l.xue.nong
 */
public class ThreadPoolModule extends AbstractModule {

	/**
	 * Instantiates a new thread pool module.
	 *
	 * @param settings the settings
	 */
	public ThreadPoolModule(Settings settings) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ThreadPool.class).asEagerSingleton();
	}
}
