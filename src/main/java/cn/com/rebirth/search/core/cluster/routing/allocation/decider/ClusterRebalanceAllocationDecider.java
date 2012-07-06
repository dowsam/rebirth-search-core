/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterRebalanceAllocationDecider.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;

/**
 * The Class ClusterRebalanceAllocationDecider.
 *
 * @author l.xue.nong
 */
public class ClusterRebalanceAllocationDecider extends AllocationDecider {

	/**
	 * The Enum ClusterRebalanceType.
	 *
	 * @author l.xue.nong
	 */
	public static enum ClusterRebalanceType {

		/** The always. */
		ALWAYS,

		/** The indices primaries active. */
		INDICES_PRIMARIES_ACTIVE,

		/** The indices all active. */
		INDICES_ALL_ACTIVE
	}

	/** The type. */
	private final ClusterRebalanceType type;

	/**
	 * Instantiates a new cluster rebalance allocation decider.
	 *
	 * @param settings the settings
	 */
	@Inject
	public ClusterRebalanceAllocationDecider(Settings settings) {
		super(settings);
		String allowRebalance = settings.get("cluster.routing.allocation.allow_rebalance", "indices_all_active");
		if ("always".equalsIgnoreCase(allowRebalance)) {
			type = ClusterRebalanceType.ALWAYS;
		} else if ("indices_primaries_active".equalsIgnoreCase(allowRebalance)
				|| "indicesPrimariesActive".equalsIgnoreCase(allowRebalance)) {
			type = ClusterRebalanceType.INDICES_PRIMARIES_ACTIVE;
		} else if ("indices_all_active".equalsIgnoreCase(allowRebalance)
				|| "indicesAllActive".equalsIgnoreCase(allowRebalance)) {
			type = ClusterRebalanceType.INDICES_ALL_ACTIVE;
		} else {
			logger.warn(
					"[cluster.routing.allocation.allow_rebalance] has a wrong value {}, defaulting to 'indices_all_active'",
					allowRebalance);
			type = ClusterRebalanceType.INDICES_ALL_ACTIVE;
		}
		logger.debug("using [cluster.routing.allocation.allow_rebalance] with [{}]", type.toString().toLowerCase());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canRebalance(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean canRebalance(ShardRouting shardRouting, RoutingAllocation allocation) {
		if (type == ClusterRebalanceType.INDICES_PRIMARIES_ACTIVE) {
			for (MutableShardRouting shard : allocation.routingNodes().unassigned()) {
				if (shard.primary()) {
					return false;
				}
			}
			for (RoutingNode node : allocation.routingNodes()) {
				List<MutableShardRouting> shards = node.shards();
				for (int i = 0; i < shards.size(); i++) {
					MutableShardRouting shard = shards.get(i);
					if (shard.primary() && !shard.active() && shard.relocatingNodeId() == null) {
						return false;
					}
				}
			}
			return true;
		}
		if (type == ClusterRebalanceType.INDICES_ALL_ACTIVE) {
			if (!allocation.routingNodes().unassigned().isEmpty()) {
				return false;
			}
			for (RoutingNode node : allocation.routingNodes()) {
				List<MutableShardRouting> shards = node.shards();
				for (int i = 0; i < shards.size(); i++) {
					MutableShardRouting shard = shards.get(i);
					if (!shard.active() && shard.relocatingNodeId() == null) {
						return false;
					}
				}
			}
		}

		return true;
	}
}
