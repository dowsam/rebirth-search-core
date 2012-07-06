/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IdCacheModule.java 2012-3-29 15:01:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.cache.id;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Scopes;
import cn.com.rebirth.search.core.index.cache.id.simple.SimpleIdCache;


/**
 * The Class IdCacheModule.
 *
 * @author l.xue.nong
 */
public class IdCacheModule extends AbstractModule {

	
	/**
	 * The Class IdCacheSettings.
	 *
	 * @author l.xue.nong
	 */
	public static final class IdCacheSettings {

		
		/** The Constant ID_CACHE_TYPE. */
		public static final String ID_CACHE_TYPE = "index.cache.id.type";
	}

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new id cache module.
	 *
	 * @param settings the settings
	 */
	public IdCacheModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IdCache.class).to(
				settings.getAsClass(IdCacheSettings.ID_CACHE_TYPE, SimpleIdCache.class,
						"cn.com.summall.search.core.index.cache.id.", "IdCache")).in(Scopes.SINGLETON);
	}
}
