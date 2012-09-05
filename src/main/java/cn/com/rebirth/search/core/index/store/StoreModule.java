/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StoreModule.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.jmx.JmxService;

/**
 * The Class StoreModule.
 *
 * @author l.xue.nong
 */
public class StoreModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/** The index store. */
	private final IndexStore indexStore;

	/**
	 * Instantiates a new store module.
	 *
	 * @param settings the settings
	 * @param indexStore the index store
	 */
	public StoreModule(Settings settings, IndexStore indexStore) {
		this.indexStore = indexStore;
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(DirectoryService.class).to(indexStore.shardDirectory()).asEagerSingleton();
		bind(Store.class).asEagerSingleton();
		if (JmxService.shouldExport(settings)) {
			bind(StoreManagement.class).asEagerSingleton();
		}
	}
}
