/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DisableAllocationDecider.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

/**
 * The Class DisableAllocationDecider.
 *
 * @author l.xue.nong
 */
public class DisableAllocationDecider extends AllocationDecider {

	static {
		MetaData.addDynamicSettings("cluster.routing.allocation.disable_allocation",
				"cluster.routing.allocation.disable_replica_allocation");
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
			boolean disableAllocation = settings.getAsBoolean("cluster.routing.allocation.disable_allocation",
					DisableAllocationDecider.this.disableAllocation);
			if (disableAllocation != DisableAllocationDecider.this.disableAllocation) {
				logger.info("updating [cluster.routing.allocation.disable_allocation] from [{}] to [{}]",
						DisableAllocationDecider.this.disableAllocation, disableAllocation);
				DisableAllocationDecider.this.disableAllocation = disableAllocation;
			}

			boolean disableReplicaAllocation = settings.getAsBoolean(
					"cluster.routing.allocation.disable_replica_allocation",
					DisableAllocationDecider.this.disableReplicaAllocation);
			if (disableReplicaAllocation != DisableAllocationDecider.this.disableReplicaAllocation) {
				logger.info("updating [cluster.routing.allocation.disable_replica_allocation] from [{}] to [{}]",
						DisableAllocationDecider.this.disableReplicaAllocation, disableReplicaAllocation);
				DisableAllocationDecider.this.disableReplicaAllocation = disableReplicaAllocation;
			}
		}
	}

	/** The disable allocation. */
	private volatile boolean disableAllocation;

	/** The disable replica allocation. */
	private volatile boolean disableReplicaAllocation;

	/**
	 * Instantiates a new disable allocation decider.
	 *
	 * @param settings the settings
	 * @param nodeSettingsService the node settings service
	 */
	@Inject
	public DisableAllocationDecider(Settings settings, NodeSettingsService nodeSettingsService) {
		super(settings);
		this.disableAllocation = settings.getAsBoolean("cluster.routing.allocation.disable_allocation", false);
		this.disableReplicaAllocation = settings.getAsBoolean("cluster.routing.allocation.disable_replica_allocation",
				false);

		nodeSettingsService.addListener(new ApplySettings());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canAllocate(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.RoutingNode, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		if (disableAllocation) {
			return Decision.NO;
		}
		if (disableReplicaAllocation) {
			return shardRouting.primary() ? Decision.YES : Decision.NO;
		}
		return Decision.YES;
	}
}
