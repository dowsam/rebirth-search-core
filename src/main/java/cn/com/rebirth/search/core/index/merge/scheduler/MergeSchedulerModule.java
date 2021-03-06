/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MergeSchedulerModule.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.merge.scheduler;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;

/**
 * The Class MergeSchedulerModule.
 *
 * @author l.xue.nong
 */
public class MergeSchedulerModule extends AbstractModule {

	/**
	 * The Class MergeSchedulerSettings.
	 *
	 * @author l.xue.nong
	 */
	public static class MergeSchedulerSettings {

		/** The Constant TYPE. */
		public static final String TYPE = "index.merge.scheduler.type";
	}

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new merge scheduler module.
	 *
	 * @param settings the settings
	 */
	public MergeSchedulerModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(MergeSchedulerProvider.class).to(
				settings.getAsClass(MergeSchedulerSettings.TYPE, ConcurrentMergeSchedulerProvider.class,
						"cn.com.rebirth.search.core.index.scheduler.", "MergeSchedulerProvider")).asEagerSingleton();
	}
}
