/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AwarenessAllocationDecider.java 2012-7-6 14:30:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.HashMap;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

import com.google.common.collect.Maps;

/**
 * The Class AwarenessAllocationDecider.
 *
 * @author l.xue.nong
 */
public class AwarenessAllocationDecider extends AllocationDecider {

	static {
		MetaData.addDynamicSettings("cluster.routing.allocation.awareness.attributes",
				"cluster.routing.allocation.awareness.force.*");
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
			String[] awarenessAttributes = settings.getAsArray("cluster.routing.allocation.awareness.attributes", null);
			if (awarenessAttributes != null) {
				logger.info("updating [cluster.routing.allocation.awareness.attributes] from [{}] to [{}]",
						AwarenessAllocationDecider.this.awarenessAttributes, awarenessAttributes);
				AwarenessAllocationDecider.this.awarenessAttributes = awarenessAttributes;
			}
			Map<String, String[]> forcedAwarenessAttributes = new HashMap<String, String[]>(
					AwarenessAllocationDecider.this.forcedAwarenessAttributes);
			Map<String, Settings> forceGroups = settings.getGroups("cluster.routing.allocation.awareness.force.");
			if (!forceGroups.isEmpty()) {
				for (Map.Entry<String, Settings> entry : forceGroups.entrySet()) {
					String[] aValues = entry.getValue().getAsArray("values");
					if (aValues.length > 0) {
						forcedAwarenessAttributes.put(entry.getKey(), aValues);
					}
				}
			}
			AwarenessAllocationDecider.this.forcedAwarenessAttributes = forcedAwarenessAttributes;
		}
	}

	/** The awareness attributes. */
	private String[] awarenessAttributes;

	/** The forced awareness attributes. */
	private Map<String, String[]> forcedAwarenessAttributes;

	/**
	 * Instantiates a new awareness allocation decider.
	 *
	 * @param settings the settings
	 * @param nodeSettingsService the node settings service
	 */
	@Inject
	public AwarenessAllocationDecider(Settings settings, NodeSettingsService nodeSettingsService) {
		super(settings);
		this.awarenessAttributes = settings.getAsArray("cluster.routing.allocation.awareness.attributes");

		forcedAwarenessAttributes = Maps.newHashMap();
		Map<String, Settings> forceGroups = settings.getGroups("cluster.routing.allocation.awareness.force.");
		for (Map.Entry<String, Settings> entry : forceGroups.entrySet()) {
			String[] aValues = entry.getValue().getAsArray("values");
			if (aValues.length > 0) {
				forcedAwarenessAttributes.put(entry.getKey(), aValues);
			}
		}

		nodeSettingsService.addListener(new ApplySettings());
	}

	/**
	 * Awareness attributes.
	 *
	 * @return the string[]
	 */
	public String[] awarenessAttributes() {
		return this.awarenessAttributes;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canAllocate(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.RoutingNode, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		return underCapacity(shardRouting, node, allocation, true) ? Decision.YES : Decision.NO;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider#canRemain(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.cluster.routing.RoutingNode, cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean canRemain(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		return underCapacity(shardRouting, node, allocation, false);
	}

	/**
	 * Under capacity.
	 *
	 * @param shardRouting the shard routing
	 * @param node the node
	 * @param allocation the allocation
	 * @param moveToNode the move to node
	 * @return true, if successful
	 */
	private boolean underCapacity(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation,
			boolean moveToNode) {
		if (awarenessAttributes.length == 0) {
			return true;
		}

		IndexMetaData indexMetaData = allocation.metaData().index(shardRouting.index());
		int shardCount = indexMetaData.numberOfReplicas() + 1;
		for (String awarenessAttribute : awarenessAttributes) {

			if (!node.node().attributes().containsKey(awarenessAttribute)) {
				return false;
			}

			TObjectIntHashMap<String> nodesPerAttribute = allocation.routingNodes().nodesPerAttributesCounts(
					awarenessAttribute);

			TObjectIntHashMap<String> shardPerAttribute = new TObjectIntHashMap<String>();
			for (RoutingNode routingNode : allocation.routingNodes()) {
				for (int i = 0; i < routingNode.shards().size(); i++) {
					MutableShardRouting nodeShardRouting = routingNode.shards().get(i);
					if (nodeShardRouting.shardId().equals(shardRouting.shardId())) {

						if (nodeShardRouting.relocating()) {
							RoutingNode relocationNode = allocation.routingNodes().node(
									nodeShardRouting.relocatingNodeId());
							shardPerAttribute.adjustOrPutValue(
									relocationNode.node().attributes().get(awarenessAttribute), 1, 1);
						} else if (nodeShardRouting.started()) {
							shardPerAttribute.adjustOrPutValue(routingNode.node().attributes().get(awarenessAttribute),
									1, 1);
						}
					}
				}
			}
			if (moveToNode) {
				if (shardRouting.assignedToNode()) {
					String nodeId = shardRouting.relocating() ? shardRouting.relocatingNodeId() : shardRouting
							.currentNodeId();
					if (!node.nodeId().equals(nodeId)) {

						shardPerAttribute.adjustOrPutValue(allocation.routingNodes().node(nodeId).node().attributes()
								.get(awarenessAttribute), -1, 0);
						shardPerAttribute.adjustOrPutValue(node.node().attributes().get(awarenessAttribute), 1, 1);
					}
				} else {
					shardPerAttribute.adjustOrPutValue(node.node().attributes().get(awarenessAttribute), 1, 1);
				}
			}

			int numberOfAttributes = nodesPerAttribute.size();
			String[] fullValues = forcedAwarenessAttributes.get(awarenessAttribute);
			if (fullValues != null) {
				for (String fullValue : fullValues) {
					if (!shardPerAttribute.contains(fullValue)) {
						numberOfAttributes++;
					}
				}
			}

			int averagePerAttribute = shardCount / numberOfAttributes;
			int totalLeftover = shardCount % numberOfAttributes;
			int requiredCountPerAttribute;
			if (averagePerAttribute == 0) {

				totalLeftover = 0;
				requiredCountPerAttribute = 1;
			} else {
				requiredCountPerAttribute = averagePerAttribute;
			}
			int leftoverPerAttribute = totalLeftover == 0 ? 0 : 1;

			int currentNodeCount = shardPerAttribute.get(node.node().attributes().get(awarenessAttribute));

			if (currentNodeCount > (requiredCountPerAttribute + leftoverPerAttribute)) {
				return false;
			}

			if (currentNodeCount <= requiredCountPerAttribute) {
				continue;
			}
		}

		return true;
	}
}
