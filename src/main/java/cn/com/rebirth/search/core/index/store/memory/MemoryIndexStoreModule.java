/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MemoryIndexStoreModule.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store.memory;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.store.IndexStore;

/**
 * The Class MemoryIndexStoreModule.
 *
 * @author l.xue.nong
 */
public class MemoryIndexStoreModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new memory index store module.
	 *
	 * @param settings the settings
	 */
	public MemoryIndexStoreModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexStore.class).to(ByteBufferIndexStore.class).asEagerSingleton();
	}
}