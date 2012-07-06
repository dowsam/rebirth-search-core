/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardSearchModule.java 2012-3-29 15:01:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.stats;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.search.slowlog.ShardSlowLogSearchService;


/**
 * The Class ShardSearchModule.
 *
 * @author l.xue.nong
 */
public class ShardSearchModule extends AbstractModule {

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(ShardSearchService.class).asEagerSingleton();
        bind(ShardSlowLogSearchService.class).asEagerSingleton();
    }
}
