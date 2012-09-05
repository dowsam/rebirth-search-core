/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardsAllocators.java 2012-7-6 14:30:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation.allocator;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation;
import cn.com.rebirth.search.core.gateway.none.NoneGatewayAllocator;

/**
 * The Class ShardsAllocators.
 *
 * @author l.xue.nong
 */
public class ShardsAllocators extends AbstractComponent implements ShardsAllocator {

	/** The gateway allocator. */
	private final GatewayAllocator gatewayAllocator;

	/** The allocator. */
	private final ShardsAllocator allocator;

	/**
	 * Instantiates a new shards allocators.
	 */
	public ShardsAllocators() {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	/**
	 * Instantiates a new shards allocators.
	 *
	 * @param settings the settings
	 */
	public ShardsAllocators(Settings settings) {
		this(settings, new NoneGatewayAllocator(), new EvenShardsCountAllocator(settings));
	}

	/**
	 * Instantiates a new shards allocators.
	 *
	 * @param settings the settings
	 * @param gatewayAllocator the gateway allocator
	 * @param allocator the allocator
	 */
	@Inject
	public ShardsAllocators(Settings settings, GatewayAllocator gatewayAllocator, ShardsAllocator allocator) {
		super(settings);
		this.gatewayAllocator = gatewayAllocator;
		this.allocator = allocator;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocator#applyStartedShards(cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation)
	 */
	@Override
	public void applyStartedShards(StartedRerouteAllocation allocation) {
		gatewayAllocator.applyStartedShards(allocation);
		allocator.applyStartedShards(allocation);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocator#applyFailedShards(cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation)
	 */
	@Override
	public void applyFailedShards(FailedRerouteAllocation allocation) {
		gatewayAllocator.applyFailedShards(allocation);
		allocator.applyFailedShards(allocation);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocator#allocateUnassigned(cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean allocateUnassigned(RoutingAllocation allocation) {
		boolean changed = false;
		changed |= gatewayAllocator.allocateUnassigned(allocation);
		changed |= allocator.allocateUnassigned(allocation);
		return changed;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocator#rebalance(cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean rebalance(RoutingAllocation allocation) {
		return allocator.rebalance(allocation);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocator#move(cn.com.rebirth.search.core.cluster.routing.MutableShardRouting, cn.com.rebirth.search.core.cluster.routing.RoutingNode, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean move(MutableShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		return allocator.move(shardRouting, node, allocation);
	}
}
