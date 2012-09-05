/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexCacheModule.java 2012-7-6 14:29:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.index.cache.bloom.BloomCacheModule;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCacheModule;
import cn.com.rebirth.search.core.index.cache.filter.FilterCacheModule;
import cn.com.rebirth.search.core.index.cache.id.IdCacheModule;
import cn.com.rebirth.search.core.index.cache.query.parser.QueryParserCacheModule;

/**
 * The Class IndexCacheModule.
 *
 * @author l.xue.nong
 */
public class IndexCacheModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new index cache module.
	 *
	 * @param settings the settings
	 */
	public IndexCacheModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		new FilterCacheModule(settings).configure(binder());
		new FieldDataCacheModule(settings).configure(binder());
		new IdCacheModule(settings).configure(binder());
		new QueryParserCacheModule(settings).configure(binder());
		new BloomCacheModule(settings).configure(binder());

		bind(IndexCache.class).asEagerSingleton();
	}
}
