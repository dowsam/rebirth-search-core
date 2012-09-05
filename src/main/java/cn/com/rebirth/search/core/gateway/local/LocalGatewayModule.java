/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalGatewayModule.java 2012-7-6 14:29:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.local;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.core.inject.Module;
import cn.com.rebirth.core.inject.PreProcessModule;
import cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocatorModule;
import cn.com.rebirth.search.core.gateway.Gateway;
import cn.com.rebirth.search.core.gateway.local.state.meta.LocalGatewayMetaState;
import cn.com.rebirth.search.core.gateway.local.state.meta.TransportNodesListGatewayMetaState;
import cn.com.rebirth.search.core.gateway.local.state.shards.LocalGatewayShardsState;
import cn.com.rebirth.search.core.gateway.local.state.shards.TransportNodesListGatewayStartedShards;

/**
 * The Class LocalGatewayModule.
 *
 * @author l.xue.nong
 */
public class LocalGatewayModule extends AbstractModule implements PreProcessModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Gateway.class).to(LocalGateway.class).asEagerSingleton();
		bind(LocalGatewayShardsState.class).asEagerSingleton();
		bind(TransportNodesListGatewayMetaState.class).asEagerSingleton();
		bind(LocalGatewayMetaState.class).asEagerSingleton();
		bind(TransportNodesListGatewayStartedShards.class).asEagerSingleton();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.PreProcessModule#processModule(cn.com.rebirth.search.commons.inject.Module)
	 */
	@Override
	public void processModule(Module module) {
		if (module instanceof ShardsAllocatorModule) {
			((ShardsAllocatorModule) module).setGatewayAllocator(LocalGatewayAllocator.class);
		}
	}
}
