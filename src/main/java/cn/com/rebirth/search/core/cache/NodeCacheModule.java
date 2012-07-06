/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeCacheModule.java 2012-3-29 15:00:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cache;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.cache.memory.ByteBufferCache;


/**
 * The Class NodeCacheModule.
 *
 * @author l.xue.nong
 */
public class NodeCacheModule extends AbstractModule {

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new node cache module.
	 *
	 * @param settings the settings
	 */
	public NodeCacheModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(NodeCache.class).asEagerSingleton();
		bind(ByteBufferCache.class).asEagerSingleton();
	}
}
