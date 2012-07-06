/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexSettingsProvider.java 2012-7-6 14:29:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.settings;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.Provider;

/**
 * The Class IndexSettingsProvider.
 *
 * @author l.xue.nong
 */
public class IndexSettingsProvider implements Provider<Settings> {

	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	/**
	 * Instantiates a new index settings provider.
	 *
	 * @param indexSettingsService the index settings service
	 */
	@Inject
	public IndexSettingsProvider(IndexSettingsService indexSettingsService) {
		this.indexSettingsService = indexSettingsService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.Provider#get()
	 */
	@Override
	public Settings get() {
		return indexSettingsService.getSettings();
	}
}