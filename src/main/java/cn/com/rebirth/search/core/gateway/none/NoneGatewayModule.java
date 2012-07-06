/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NoneGatewayModule.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.gateway.none;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.PreProcessModule;
import cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocatorModule;
import cn.com.rebirth.search.core.gateway.Gateway;


/**
 * The Class NoneGatewayModule.
 *
 * @author l.xue.nong
 */
public class NoneGatewayModule extends AbstractModule implements PreProcessModule {

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.PreProcessModule#processModule(cn.com.summall.search.commons.inject.Module)
     */
    @Override
    public void processModule(Module module) {
        if (module instanceof ShardsAllocatorModule) {
            ((ShardsAllocatorModule) module).setGatewayAllocator(NoneGatewayAllocator.class);
        }
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(Gateway.class).to(NoneGateway.class).asEagerSingleton();
    }
}
