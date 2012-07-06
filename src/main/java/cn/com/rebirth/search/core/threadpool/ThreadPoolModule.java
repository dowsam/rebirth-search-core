/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ThreadPoolModule.java 2012-3-29 15:01:04 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ThreadPool.class).asEagerSingleton();
	}
}
