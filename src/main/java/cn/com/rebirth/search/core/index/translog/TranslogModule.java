/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TranslogModule.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.translog;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Scopes;
import cn.com.rebirth.search.core.index.translog.fs.FsTranslog;

/**
 * The Class TranslogModule.
 *
 * @author l.xue.nong
 */
public class TranslogModule extends AbstractModule {

	/**
	 * The Class TranslogSettings.
	 *
	 * @author l.xue.nong
	 */
	public static class TranslogSettings {

		/** The Constant TYPE. */
		public static final String TYPE = "index.translog.type";
	}

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new translog module.
	 *
	 * @param settings the settings
	 */
	public TranslogModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Translog.class).to(settings.getAsClass(TranslogSettings.TYPE, FsTranslog.class)).in(Scopes.SINGLETON);
		bind(TranslogService.class).asEagerSingleton();
	}
}
