/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BloomCacheModule.java 2012-3-29 15:02:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.cache.bloom;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Scopes;
import cn.com.rebirth.search.core.index.cache.bloom.simple.SimpleBloomCache;


/**
 * The Class BloomCacheModule.
 *
 * @author l.xue.nong
 */
public class BloomCacheModule extends AbstractModule {

	
	/**
	 * The Class BloomCacheSettings.
	 *
	 * @author l.xue.nong
	 */
	public static final class BloomCacheSettings {

		
		/** The Constant TYPE. */
		public static final String TYPE = "index.cache.bloom.type";
	}

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new bloom cache module.
	 *
	 * @param settings the settings
	 */
	public BloomCacheModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(BloomCache.class).to(
				settings.getAsClass(BloomCacheSettings.TYPE, SimpleBloomCache.class,
						"cn.com.summall.search.core.index.cache.bloom.", "BloomCache")).in(Scopes.SINGLETON);
	}
}