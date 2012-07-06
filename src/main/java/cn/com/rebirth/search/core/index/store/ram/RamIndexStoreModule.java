/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RamIndexStoreModule.java 2012-3-29 15:02:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store.ram;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.store.IndexStore;


/**
 * The Class RamIndexStoreModule.
 *
 * @author l.xue.nong
 */
public class RamIndexStoreModule extends AbstractModule {

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(IndexStore.class).to(RamIndexStore.class).asEagerSingleton();
    }
}
