/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardsLimitAllocationDecider.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;

/**
 * The Class ShardsLimitAllocationDecider.
 *
 * @author l.xue.nong
 */
public class ShardsLimitAllocationDecider extends AllocationDecider {

	/** The Constant INDEX_TOTAL_SHARDS_PER_NODE. */
	public static final String INDEX_TOTAL_SHARDS_PER_NODE = "index.routing.allocation.total_shards_per_node";

	static {
		IndexMetaData.addDynamicSettings(INDEX_TOTAL_SHARDS_PER_NODE);
	}

	/**
	 * Instantiates a new shards limit allocation decider.
	 *
	 * @param settings the settings
	 */
	@Inject
	public ShardsLimitAllocationDecider(Settings settings) {
		super(settings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canAllocate(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.RoutingNode, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		IndexMetaData indexMd = allocation.routingNodes().metaData().index(shardRouting.index());
		int totalShardsPerNode = indexMd.settings().getAsInt(INDEX_TOTAL_SHARDS_PER_NODE, -1);
		if (totalShardsPerNode <= 0) {
			return Decision.YES;
		}

		int nodeCount = 0;
		List<MutableShardRouting> shards = node.shards();
		for (int i = 0; i < shards.size(); i++) {
			MutableShardRouting nodeShard = shards.get(i);
			if (!nodeShard.index().equals(shardRouting.index())) {
				continue;
			}

			if (nodeShard.relocating()) {
				continue;
			}
			nodeCount++;
		}
		if (nodeCount >= totalShardsPerNode) {
			return Decision.NO;
		}
		return Decision.YES;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canRemain(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.RoutingNode, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean canRemain(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		IndexMetaData indexMd = allocation.routingNodes().metaData().index(shardRouting.index());
		int totalShardsPerNode = indexMd.settings().getAsInt(INDEX_TOTAL_SHARDS_PER_NODE, -1);
		if (totalShardsPerNode <= 0) {
			return true;
		}

		int nodeCount = 0;
		List<MutableShardRouting> shards = node.shards();
		for (int i = 0; i < shards.size(); i++) {
			MutableShardRouting nodeShard = shards.get(i);
			if (!nodeShard.index().equals(shardRouting.index())) {
				continue;
			}

			if (nodeShard.relocating()) {
				continue;
			}
			nodeCount++;
		}
		if (nodeCount > totalShardsPerNode) {
			return false;
		}
		return true;
	}
}
