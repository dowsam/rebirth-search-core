/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BlobReuseExistingGatewayAllocator.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.blobstore;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.RoutingNodes;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation;
import cn.com.rebirth.search.core.cluster.routing.allocation.allocator.GatewayAllocator;
import cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecider;
import cn.com.rebirth.search.core.gateway.Gateway;
import cn.com.rebirth.search.core.index.gateway.CommitPoint;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.StoreFileMetaData;
import cn.com.rebirth.search.core.indices.store.TransportNodesListShardStoreMetaData;
import cn.com.rebirth.search.core.node.Node;
import cn.com.rebirth.search.core.node.internal.InternalNode;
import cn.com.rebirth.search.core.transport.ConnectTransportException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The Class BlobReuseExistingGatewayAllocator.
 *
 * @author l.xue.nong
 */
public class BlobReuseExistingGatewayAllocator extends AbstractComponent implements GatewayAllocator {

	/** The node. */
	private final Node node;

	/** The list shard store meta data. */
	private final TransportNodesListShardStoreMetaData listShardStoreMetaData;

	/** The list timeout. */
	private final TimeValue listTimeout;

	/** The cached commit points. */
	private final ConcurrentMap<ShardId, CommitPoint> cachedCommitPoints = ConcurrentCollections.newConcurrentMap();

	/** The cached stores. */
	private final ConcurrentMap<ShardId, Map<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData>> cachedStores = ConcurrentCollections
			.newConcurrentMap();

	/**
	 * Instantiates a new blob reuse existing gateway allocator.
	 *
	 * @param settings the settings
	 * @param node the node
	 * @param transportNodesListShardStoreMetaData the transport nodes list shard store meta data
	 */
	@Inject
	public BlobReuseExistingGatewayAllocator(Settings settings, Node node,
			TransportNodesListShardStoreMetaData transportNodesListShardStoreMetaData) {
		super(settings);
		this.node = node;
		this.listShardStoreMetaData = transportNodesListShardStoreMetaData;

		this.listTimeout = componentSettings.getAsTime("list_timeout", TimeValue.timeValueSeconds(30));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.GatewayAllocator#applyStartedShards(cn.com.rebirth.search.core.cluster.routing.allocation.StartedRerouteAllocation)
	 */
	@Override
	public void applyStartedShards(StartedRerouteAllocation allocation) {
		for (ShardRouting shardRouting : allocation.startedShards()) {
			cachedCommitPoints.remove(shardRouting.shardId());
			cachedStores.remove(shardRouting.shardId());
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.GatewayAllocator#applyFailedShards(cn.com.rebirth.search.core.cluster.routing.allocation.FailedRerouteAllocation)
	 */
	@Override
	public void applyFailedShards(FailedRerouteAllocation allocation) {
		cachedCommitPoints.remove(allocation.failedShard().shardId());
		cachedStores.remove(allocation.failedShard().shardId());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.allocation.allocator.GatewayAllocator#allocateUnassigned(cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation)
	 */
	@Override
	public boolean allocateUnassigned(RoutingAllocation allocation) {
		boolean changed = false;

		DiscoveryNodes nodes = allocation.nodes();
		RoutingNodes routingNodes = allocation.routingNodes();

		if (nodes.dataNodes().isEmpty()) {
			return changed;
		}

		if (!routingNodes.hasUnassigned()) {
			return changed;
		}

		Iterator<MutableShardRouting> unassignedIterator = routingNodes.unassigned().iterator();
		while (unassignedIterator.hasNext()) {
			MutableShardRouting shard = unassignedIterator.next();

			boolean canBeAllocatedToAtLeastOneNode = false;
			for (DiscoveryNode discoNode : nodes.dataNodes().values()) {
				RoutingNode node = routingNodes.node(discoNode.id());
				if (node == null) {
					continue;
				}

				if (allocation.deciders().canAllocate(shard, node, allocation).allocate()) {
					canBeAllocatedToAtLeastOneNode = true;
					break;
				}
			}

			if (!canBeAllocatedToAtLeastOneNode) {
				continue;
			}

			Map<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData> shardStores = buildShardStores(
					nodes, shard);

			long lastSizeMatched = 0;
			DiscoveryNode lastDiscoNodeMatched = null;
			RoutingNode lastNodeMatched = null;

			for (Map.Entry<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData> nodeStoreEntry : shardStores
					.entrySet()) {
				DiscoveryNode discoNode = nodeStoreEntry.getKey();
				TransportNodesListShardStoreMetaData.StoreFilesMetaData storeFilesMetaData = nodeStoreEntry.getValue();
				logger.trace("{}: checking node [{}]", shard, discoNode);

				if (storeFilesMetaData == null) {

					continue;
				}

				RoutingNode node = routingNodes.node(discoNode.id());
				if (node == null) {
					continue;
				}

				if (allocation.deciders().canAllocate(shard, node, allocation) == AllocationDecider.Decision.NO) {
					continue;
				}

				if (storeFilesMetaData.allocated()) {
					continue;
				}

				if (shard.primary()) {
					try {
						CommitPoint commitPoint = cachedCommitPoints.get(shard.shardId());
						if (commitPoint == null) {
							commitPoint = ((BlobStoreGateway) ((InternalNode) this.node).injector().getInstance(
									Gateway.class)).findCommitPoint(shard.index(), shard.id());
							if (commitPoint != null) {
								cachedCommitPoints.put(shard.shardId(), commitPoint);
							} else {
								cachedCommitPoints.put(shard.shardId(), CommitPoint.NULL);
							}
						} else if (commitPoint == CommitPoint.NULL) {
							commitPoint = null;
						}

						if (commitPoint == null) {
							break;
						}

						long sizeMatched = 0;
						for (StoreFileMetaData storeFileMetaData : storeFilesMetaData) {
							CommitPoint.FileInfo fileInfo = commitPoint.findPhysicalIndexFile(storeFileMetaData.name());
							if (fileInfo != null) {
								if (fileInfo.isSame(storeFileMetaData)) {
									logger.trace("{}: [{}] reusing file since it exists on remote node and on gateway",
											shard, storeFileMetaData.name());
									sizeMatched += storeFileMetaData.length();
								} else {
									logger.trace(
											"{}: [{}] ignore file since it exists on remote node and on gateway but is different",
											shard, storeFileMetaData.name());
								}
							} else {
								logger.trace("{}: [{}] exists on remote node, does not exists on gateway", shard,
										storeFileMetaData.name());
							}
						}
						if (sizeMatched > lastSizeMatched) {
							lastSizeMatched = sizeMatched;
							lastDiscoNodeMatched = discoNode;
							lastNodeMatched = node;
							logger.trace(shard + ": node elected for pre_allocation [{}], total_size_matched [{}]",
									discoNode, new ByteSizeValue(sizeMatched));
						} else {
							logger.trace(shard + ": node ignored for pre_allocation [" + discoNode
									+ "], total_size_matched [{}] smaller than last_size_matched [{}]",
									new ByteSizeValue(sizeMatched), new ByteSizeValue(lastSizeMatched));
						}
					} catch (Exception e) {

						logger.debug("Failed to guess allocation of primary based on gateway for " + shard, e);
					}
				} else {

					MutableShardRouting primaryShard = routingNodes.findPrimaryForReplica(shard);
					if (primaryShard != null && primaryShard.active()) {
						DiscoveryNode primaryNode = nodes.get(primaryShard.currentNodeId());
						if (primaryNode != null) {
							TransportNodesListShardStoreMetaData.StoreFilesMetaData primaryNodeStore = shardStores
									.get(primaryNode);
							if (primaryNodeStore != null && primaryNodeStore.allocated()) {
								long sizeMatched = 0;

								for (StoreFileMetaData storeFileMetaData : storeFilesMetaData) {
									if (primaryNodeStore.fileExists(storeFileMetaData.name())
											&& primaryNodeStore.file(storeFileMetaData.name())
													.isSame(storeFileMetaData)) {
										sizeMatched += storeFileMetaData.length();
									}
								}
								if (sizeMatched > lastSizeMatched) {
									lastSizeMatched = sizeMatched;
									lastDiscoNodeMatched = discoNode;
									lastNodeMatched = node;
								}
							}
						}
					}
				}
			}

			if (lastNodeMatched != null) {
				if (allocation.deciders().canAllocate(shard, lastNodeMatched, allocation) == AllocationDecider.Decision.THROTTLE) {
					if (logger.isTraceEnabled()) {
						logger.debug("[" + shard.index() + "][" + shard.id() + "]: throttling allocation [" + shard
								+ "] to [{}] in order to reuse its unallocated persistent store with total_size [{}]",
								lastDiscoNodeMatched, new ByteSizeValue(lastSizeMatched));
					}

					unassignedIterator.remove();
					routingNodes.ignoredUnassigned().add(shard);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("[" + shard.index() + "][" + shard.id() + "]: allocating [" + shard
								+ "] to [{}] in order to reuse its unallocated persistent store with total_size [{}]",
								lastDiscoNodeMatched, new ByteSizeValue(lastSizeMatched));
					}

					changed = true;
					lastNodeMatched.add(shard);
					unassignedIterator.remove();
				}
			}
		}

		return changed;
	}

	/**
	 * Builds the shard stores.
	 *
	 * @param nodes the nodes
	 * @param shard the shard
	 * @return the map
	 */
	private Map<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData> buildShardStores(
			DiscoveryNodes nodes, MutableShardRouting shard) {
		Map<DiscoveryNode, TransportNodesListShardStoreMetaData.StoreFilesMetaData> shardStores = cachedStores
				.get(shard.shardId());
		Set<String> nodesIds;
		if (shardStores == null) {
			shardStores = Maps.newHashMap();
			cachedStores.put(shard.shardId(), shardStores);
			nodesIds = nodes.dataNodes().keySet();
		} else {
			nodesIds = Sets.newHashSet();

			for (Iterator<DiscoveryNode> it = shardStores.keySet().iterator(); it.hasNext();) {
				DiscoveryNode node = it.next();
				if (!nodes.nodeExists(node.id())) {
					it.remove();
				}
			}

			for (DiscoveryNode node : nodes.dataNodes().values()) {
				if (!shardStores.containsKey(node)) {
					nodesIds.add(node.id());
				}
			}
		}

		if (!nodesIds.isEmpty()) {
			TransportNodesListShardStoreMetaData.NodesStoreFilesMetaData nodesStoreFilesMetaData = listShardStoreMetaData
					.list(shard.shardId(), false, nodesIds, listTimeout).actionGet();
			if (logger.isTraceEnabled()) {
				if (nodesStoreFilesMetaData.failures().length > 0) {
					StringBuilder sb = new StringBuilder(shard + ": failures when trying to list stores on nodes:");
					for (int i = 0; i < nodesStoreFilesMetaData.failures().length; i++) {
						Throwable cause = ExceptionsHelper.unwrapCause(nodesStoreFilesMetaData.failures()[i]);
						if (cause instanceof ConnectTransportException) {
							continue;
						}
						sb.append("\n    -> ").append(nodesStoreFilesMetaData.failures()[i].getDetailedMessage());
					}
					logger.trace(sb.toString());
				}
			}

			for (TransportNodesListShardStoreMetaData.NodeStoreFilesMetaData nodeStoreFilesMetaData : nodesStoreFilesMetaData) {
				if (nodeStoreFilesMetaData.storeFilesMetaData() != null) {
					shardStores.put(nodeStoreFilesMetaData.node(), nodeStoreFilesMetaData.storeFilesMetaData());
				}
			}
		}

		return shardStores;
	}
}
