/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldDataCacheModule.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.field.data;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Scopes;
import cn.com.rebirth.search.core.index.cache.field.data.resident.ResidentFieldDataCache;

/**
 * The Class FieldDataCacheModule.
 *
 * @author l.xue.nong
 */
public class FieldDataCacheModule extends AbstractModule {

	/**
	 * The Class FieldDataCacheSettings.
	 *
	 * @author l.xue.nong
	 */
	public static final class FieldDataCacheSettings {

		/** The Constant FIELD_DATA_CACHE_TYPE. */
		public static final String FIELD_DATA_CACHE_TYPE = "index.cache.field.type";
	}

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new field data cache module.
	 *
	 * @param settings the settings
	 */
	public FieldDataCacheModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(FieldDataCache.class).to(
				settings.getAsClass(FieldDataCacheSettings.FIELD_DATA_CACHE_TYPE, ResidentFieldDataCache.class,
						"cn.com.rebirth.search.core.index.cache.field.data.", "FieldDataCache")).in(Scopes.SINGLETON);
	}
}
