/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilterCacheModule.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.filter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.core.inject.Scopes;
import cn.com.rebirth.search.core.index.cache.filter.weighted.WeightedFilterCache;

/**
 * The Class FilterCacheModule.
 *
 * @author l.xue.nong
 */
public class FilterCacheModule extends AbstractModule {

	/**
	 * The Class FilterCacheSettings.
	 *
	 * @author l.xue.nong
	 */
	public static final class FilterCacheSettings {

		/** The Constant FILTER_CACHE_TYPE. */
		public static final String FILTER_CACHE_TYPE = "index.cache.filter.type";
	}

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new filter cache module.
	 *
	 * @param settings the settings
	 */
	public FilterCacheModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(FilterCache.class).to(
				settings.getAsClass(FilterCacheSettings.FILTER_CACHE_TYPE, WeightedFilterCache.class,
						"cn.com.rebirth.search.core.index.cache.filter.", "FilterCache")).in(Scopes.SINGLETON);
	}
}
