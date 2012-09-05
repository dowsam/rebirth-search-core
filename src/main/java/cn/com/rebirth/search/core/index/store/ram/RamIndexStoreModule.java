/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RamIndexStoreModule.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store.ram;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.index.store.IndexStore;

/**
 * The Class RamIndexStoreModule.
 *
 * @author l.xue.nong
 */
public class RamIndexStoreModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexStore.class).to(RamIndexStore.class).asEagerSingleton();
	}
}
