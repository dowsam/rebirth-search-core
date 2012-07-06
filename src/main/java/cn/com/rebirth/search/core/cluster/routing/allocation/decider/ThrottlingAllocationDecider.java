/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ThrottlingAllocationDecider.java 2012-3-29 15:02:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.ShardRoutingState;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;


/**
 * The Class ThrottlingAllocationDecider.
 *
 * @author l.xue.nong
 */
public class ThrottlingAllocationDecider extends AllocationDecider {

	static {
		MetaData.addDynamicSettings("cluster.routing.allocation.node_initial_primaries_recoveries",
				"cluster.routing.allocation.node_concurrent_recoveries");
	}

	
	/** The primaries initial recoveries. */
	private volatile int primariesInitialRecoveries;

	
	/** The concurrent recoveries. */
	private volatile int concurrentRecoveries;

	
	/**
	 * Instantiates a new throttling allocation decider.
	 *
	 * @param settings the settings
	 * @param nodeSettingsService the node settings service
	 */
	@Inject
	public ThrottlingAllocationDecider(Settings settings, NodeSettingsService nodeSettingsService) {
		super(settings);

		this.primariesInitialRecoveries = settings.getAsInt(
				"cluster.routing.allocation.node_initial_primaries_recoveries",
				settings.getAsInt("cluster.routing.allocation.node_initial_primaries_recoveries", 4));
		this.concurrentRecoveries = settings.getAsInt("cluster.routing.allocation.concurrent_recoveries",
				settings.getAsInt("cluster.routing.allocation.node_concurrent_recoveries", 2));
		logger.debug("using node_concurrent_recoveries [{}], node_initial_primaries_recoveries [{}]",
				concurrentRecoveries, primariesInitialRecoveries);

		nodeSettingsService.addListener(new ApplySettings());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.allocation.decider.AllocationDecider#canAllocate(cn.com.summall.search.core.cluster.routing.ShardRouting, cn.com.summall.search.core.cluster.routing.RoutingNode, cn.com.summall.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		if (shardRouting.primary()) {
			boolean primaryUnassigned = false;
			for (MutableShardRouting shard : allocation.routingNodes().unassigned()) {
				if (shard.shardId().equals(shardRouting.shardId())) {
					primaryUnassigned = true;
				}
			}
			if (primaryUnassigned) {
				
				
				int primariesInRecovery = 0;
				List<MutableShardRouting> shards = node.shards();
				for (int i = 0; i < shards.size(); i++) {
					MutableShardRouting shard = shards.get(i);
					if (shard.state() == ShardRoutingState.INITIALIZING && shard.primary()) {
						primariesInRecovery++;
					}
				}
				if (primariesInRecovery >= primariesInitialRecoveries) {
					return Decision.THROTTLE;
				} else {
					return Decision.YES;
				}
			}
		}

		

		
		int currentRecoveries = 0;
		List<MutableShardRouting> shards = node.shards();
		for (int i = 0; i < shards.size(); i++) {
			MutableShardRouting shard = shards.get(i);
			if (shard.state() == ShardRoutingState.INITIALIZING || shard.state() == ShardRoutingState.RELOCATING) {
				currentRecoveries++;
			}
		}

		if (currentRecoveries >= concurrentRecoveries) {
			return Decision.THROTTLE;
		} else {
			return Decision.YES;
		}
	}

	
	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements NodeSettingsService.Listener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.node.settings.NodeSettingsService.Listener#onRefreshSettings(cn.com.summall.search.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			int primariesInitialRecoveries = settings.getAsInt(
					"cluster.routing.allocation.node_initial_primaries_recoveries",
					ThrottlingAllocationDecider.this.primariesInitialRecoveries);
			if (primariesInitialRecoveries != ThrottlingAllocationDecider.this.primariesInitialRecoveries) {
				logger.info(
						"updating [cluster.routing.allocation.node_initial_primaries_recoveries] from [{}] to [{}]",
						ThrottlingAllocationDecider.this.primariesInitialRecoveries, primariesInitialRecoveries);
				ThrottlingAllocationDecider.this.primariesInitialRecoveries = primariesInitialRecoveries;
			}

			int concurrentRecoveries = settings.getAsInt("cluster.routing.allocation.node_concurrent_recoveries",
					ThrottlingAllocationDecider.this.concurrentRecoveries);
			if (concurrentRecoveries != ThrottlingAllocationDecider.this.concurrentRecoveries) {
				logger.info("updating [cluster.routing.allocation.node_concurrent_recoveries] from [{}] to [{}]",
						ThrottlingAllocationDecider.this.concurrentRecoveries, concurrentRecoveries);
				ThrottlingAllocationDecider.this.concurrentRecoveries = concurrentRecoveries;
			}
		}
	}
}
