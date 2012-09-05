/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NioFsIndexStoreModule.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store.fs;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.index.store.IndexStore;

/**
 * The Class NioFsIndexStoreModule.
 *
 * @author l.xue.nong
 */
public class NioFsIndexStoreModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexStore.class).to(NioFsIndexStore.class).asEagerSingleton();
	}
}