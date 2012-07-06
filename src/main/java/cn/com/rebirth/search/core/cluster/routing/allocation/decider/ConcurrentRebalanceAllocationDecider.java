/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ConcurrentRebalanceAllocationDecider.java 2012-7-6 14:30:01 l.xue.nong$$
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
 * The Class ConcurrentRebalanceAllocationDecider.
 *
 * @author l.xue.nong
 */
public class ConcurrentRebalanceAllocationDecider extends AllocationDecider {

	static {
		MetaData.addDynamicSettings("cluster.routing.allocation.cluster_concurrent_rebalance");
	}

	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements NodeSettingsService.Listener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.node.settings.NodeSettingsService.Listener#onRefreshSettings(cn.com.rebirth.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			int clusterConcurrentRebalance = settings.getAsInt(
					"cluster.routing.allocation.cluster_concurrent_rebalance",
					ConcurrentRebalanceAllocationDecider.this.clusterConcurrentRebalance);
			if (clusterConcurrentRebalance != ConcurrentRebalanceAllocationDecider.this.clusterConcurrentRebalance) {
				logger.info("updating [cluster.routing.allocation.cluster_concurrent_rebalance] from [{}], to [{}]",
						ConcurrentRebalanceAllocationDecider.this.clusterConcurrentRebalance,
						clusterConcurrentRebalance);
				ConcurrentRebalanceAllocationDecider.this.clusterConcurrentRebalance = clusterConcurrentRebalance;
			}
		}
	}

	/** The cluster concurrent rebalance. */
	private volatile int clusterConcurrentRebalance;

	/**
	 * Instantiates a new concurrent rebalance allocation decider.
	 *
	 * @param settings the settings
	 * @param nodeSettingsService the node settings service
	 */
	@Inject
	public ConcurrentRebalanceAllocationDecider(Settings settings, NodeSettingsService nodeSettingsService) {
		super(settings);
		this.clusterConcurrentRebalance = settings.getAsInt("cluster.routing.allocation.cluster_concurrent_rebalance",
				2);
		logger.debug("using [cluster_concurrent_rebalance] with [{}]", clusterConcurrentRebalance);
		nodeSettingsService.addListener(new ApplySettings());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canRebalance(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean canRebalance(ShardRouting shardRouting, RoutingAllocation allocation) {
		if (clusterConcurrentRebalance == -1) {
			return true;
		}
		int rebalance = 0;
		for (RoutingNode node : allocation.routingNodes()) {
			List<MutableShardRouting> shards = node.shards();
			for (int i = 0; i < shards.size(); i++) {
				if (shards.get(i).state() == ShardRoutingState.RELOCATING) {
					rebalance++;
				}
			}
		}
		if (rebalance >= clusterConcurrentRebalance) {
			return false;
		}
		return true;
	}
}