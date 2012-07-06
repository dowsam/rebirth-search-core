/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NioFsIndexStoreModule.java 2012-3-29 15:01:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store.fs;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.store.IndexStore;


/**
 * The Class NioFsIndexStoreModule.
 *
 * @author l.xue.nong
 */
public class NioFsIndexStoreModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexStore.class).to(NioFsIndexStore.class).asEagerSingleton();
	}
}