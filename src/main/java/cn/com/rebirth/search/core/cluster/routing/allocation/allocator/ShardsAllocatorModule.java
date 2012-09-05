/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardsAllocatorModule.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation.allocator;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.gateway.none.NoneGatewayAllocator;

/**
 * The Class ShardsAllocatorModule.
 *
 * @author l.xue.nong
 */
public class ShardsAllocatorModule extends AbstractModule {

	/** The settings. */
	private Settings settings;

	/** The shards allocator. */
	private Class<? extends ShardsAllocator> shardsAllocator;

	/** The gateway allocator. */
	private Class<? extends GatewayAllocator> gatewayAllocator = NoneGatewayAllocator.class;

	/**
	 * Instantiates a new shards allocator module.
	 *
	 * @param settings the settings
	 */
	public ShardsAllocatorModule(Settings settings) {
		this.settings = settings;
	}

	/**
	 * Sets the gateway allocator.
	 *
	 * @param gatewayAllocator the new gateway allocator
	 */
	public void setGatewayAllocator(Class<? extends GatewayAllocator> gatewayAllocator) {
		this.gatewayAllocator = gatewayAllocator;
	}

	/**
	 * Sets the shards allocator.
	 *
	 * @param shardsAllocator the new shards allocator
	 */
	public void setShardsAllocator(Class<? extends ShardsAllocator> shardsAllocator) {
		this.shardsAllocator = shardsAllocator;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(GatewayAllocator.class).to(gatewayAllocator).asEagerSingleton();
		bind(ShardsAllocator.class).to(shardsAllocator == null ? EvenShardsCountAllocator.class : shardsAllocator)
				.asEagerSingleton();
	}
}
