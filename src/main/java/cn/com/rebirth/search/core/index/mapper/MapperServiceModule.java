/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MapperServiceModule.java 2012-3-29 15:02:54 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;

import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class MapperServiceModule.
 *
 * @author l.xue.nong
 */
public class MapperServiceModule extends AbstractModule {

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(MapperService.class).asEagerSingleton();
    }
}
