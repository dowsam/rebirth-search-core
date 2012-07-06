/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryParserCacheModule.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.query.parser;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Scopes;
import cn.com.rebirth.search.core.index.cache.query.parser.resident.ResidentQueryParserCache;

/**
 * The Class QueryParserCacheModule.
 *
 * @author l.xue.nong
 */
public class QueryParserCacheModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new query parser cache module.
	 *
	 * @param settings the settings
	 */
	public QueryParserCacheModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(QueryParserCache.class).to(
				settings.getAsClass("index.cache.query.parser.type", ResidentQueryParserCache.class,
						"cn.com.rebirth.search.core.index.cache.query.parser.", "QueryParserCache")).in(
				Scopes.SINGLETON);
	}
}
