/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MmapFsIndexStoreModule.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store.fs;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.store.IndexStore;


/**
 * The Class MmapFsIndexStoreModule.
 *
 * @author l.xue.nong
 */
public class MmapFsIndexStoreModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexStore.class).to(MmapFsIndexStore.class).asEagerSingleton();
	}
}