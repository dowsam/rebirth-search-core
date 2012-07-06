/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardGetModule.java 2012-3-29 15:00:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.get;

import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class ShardGetModule.
 *
 * @author l.xue.nong
 */
public class ShardGetModule extends AbstractModule {

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(ShardGetService.class).asEagerSingleton();
    }
}
