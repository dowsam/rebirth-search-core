/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexSettingsModule.java 2012-7-6 14:30:36 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.settings;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.Index;

/**
 * The Class IndexSettingsModule.
 *
 * @author l.xue.nong
 */
public class IndexSettingsModule extends AbstractModule {

	/** The index. */
	private final Index index;

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new index settings module.
	 *
	 * @param index the index
	 * @param settings the settings
	 */
	public IndexSettingsModule(Index index, Settings settings) {
		this.index = index;
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		IndexSettingsService indexSettingsService = new IndexSettingsService(index, settings);
		bind(IndexSettingsService.class).toInstance(indexSettingsService);
		bind(Settings.class).annotatedWith(IndexSettings.class).toProvider(
				new IndexSettingsProvider(indexSettingsService));
	}
}
