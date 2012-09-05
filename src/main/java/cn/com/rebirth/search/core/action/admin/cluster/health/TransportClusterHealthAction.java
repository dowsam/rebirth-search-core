/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportClusterHealthAction.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.health;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.IndexShardRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.RoutingTableValidation;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportClusterHealthAction.
 *
 * @author l.xue.nong
 */
public class TransportClusterHealthAction extends
		TransportMasterNodeOperationAction<ClusterHealthRequest, ClusterHealthResponse> {

	/** The cluster name. */
	private final ClusterName clusterName;

	/**
	 * Instantiates a new transport cluster health action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param clusterName the cluster name
	 */
	@Inject
	public TransportClusterHealthAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, ClusterName clusterName) {
		super(settings, transportService, clusterService, threadPool);
		this.clusterName = clusterName;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return ClusterHealthAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected ClusterHealthRequest newRequest() {
		return new ClusterHealthRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected ClusterHealthResponse newResponse() {
		return new ClusterHealthResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterHealthResponse masterOperation(ClusterHealthRequest request, ClusterState unusedState)
			throws RebirthException {
		int waitFor = 5;
		if (request.waitForStatus() == null) {
			waitFor--;
		}
		if (request.waitForRelocatingShards() == -1) {
			waitFor--;
		}
		if (request.waitForActiveShards() == -1) {
			waitFor--;
		}
		if (request.waitForNodes().isEmpty()) {
			waitFor--;
		}
		if (request.indices().length == 0) {
			waitFor--;
		}
		if (waitFor == 0) {

			ClusterState clusterState = clusterService.state();
			return clusterHealth(request, clusterState);
		}
		long endTime = System.currentTimeMillis() + request.timeout().millis();
		while (true) {
			int waitForCounter = 0;
			ClusterState clusterState = clusterService.state();
			ClusterHealthResponse response = clusterHealth(request, clusterState);
			if (request.waitForStatus() != null && response.status().value() <= request.waitForStatus().value()) {
				waitForCounter++;
			}
			if (request.waitForRelocatingShards() != -1
					&& response.relocatingShards() <= request.waitForRelocatingShards()) {
				waitForCounter++;
			}
			if (request.waitForActiveShards() != -1 && response.activeShards() >= request.waitForActiveShards()) {
				waitForCounter++;
			}
			if (request.indices().length > 0) {
				try {
					clusterState.metaData().concreteIndices(request.indices());
					waitForCounter++;
				} catch (IndexMissingException e) {
					response.status = ClusterHealthStatus.RED;

				}
			}
			if (!request.waitForNodes().isEmpty()) {
				if (request.waitForNodes().startsWith(">=")) {
					int expected = Integer.parseInt(request.waitForNodes().substring(2));
					if (response.numberOfNodes() >= expected) {
						waitForCounter++;
					}
				} else if (request.waitForNodes().startsWith("ge(")) {
					int expected = Integer.parseInt(request.waitForNodes().substring(3,
							request.waitForNodes().length() - 1));
					if (response.numberOfNodes() >= expected) {
						waitForCounter++;
					}
				} else if (request.waitForNodes().startsWith("<=")) {
					int expected = Integer.parseInt(request.waitForNodes().substring(2));
					if (response.numberOfNodes() <= expected) {
						waitForCounter++;
					}
				} else if (request.waitForNodes().startsWith("le(")) {
					int expected = Integer.parseInt(request.waitForNodes().substring(3,
							request.waitForNodes().length() - 1));
					if (response.numberOfNodes() <= expected) {
						waitForCounter++;
					}
				} else if (request.waitForNodes().startsWith(">")) {
					int expected = Integer.parseInt(request.waitForNodes().substring(1));
					if (response.numberOfNodes() > expected) {
						waitForCounter++;
					}
				} else if (request.waitForNodes().startsWith("gt(")) {
					int expected = Integer.parseInt(request.waitForNodes().substring(3,
							request.waitForNodes().length() - 1));
					if (response.numberOfNodes() > expected) {
						waitForCounter++;
					}
				} else if (request.waitForNodes().startsWith("<")) {
					int expected = Integer.parseInt(request.waitForNodes().substring(1));
					if (response.numberOfNodes() < expected) {
						waitForCounter++;
					}
				} else if (request.waitForNodes().startsWith("lt(")) {
					int expected = Integer.parseInt(request.waitForNodes().substring(3,
							request.waitForNodes().length() - 1));
					if (response.numberOfNodes() < expected) {
						waitForCounter++;
					}
				} else {
					int expected = Integer.parseInt(request.waitForNodes());
					if (response.numberOfNodes() == expected) {
						waitForCounter++;
					}
				}
			}
			if (waitForCounter == waitFor) {
				return response;
			}
			if (System.currentTimeMillis() > endTime) {
				response.timedOut = true;
				return response;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				response.timedOut = true;

				return response;
			}
		}
	}

	/**
	 * Cluster health.
	 *
	 * @param request the request
	 * @param clusterState the cluster state
	 * @return the cluster health response
	 */
	private ClusterHealthResponse clusterHealth(ClusterHealthRequest request, ClusterState clusterState) {
		RoutingTableValidation validation = clusterState.routingTable().validate(clusterState.metaData());
		ClusterHealthResponse response = new ClusterHealthResponse(clusterName.value(), validation.failures());
		response.numberOfNodes = clusterState.nodes().size();
		response.numberOfDataNodes = clusterState.nodes().dataNodes().size();

		for (String index : clusterState.metaData().concreteIndicesIgnoreMissing(request.indices())) {
			IndexRoutingTable indexRoutingTable = clusterState.routingTable().index(index);
			IndexMetaData indexMetaData = clusterState.metaData().index(index);
			if (indexRoutingTable == null) {
				continue;
			}
			ClusterIndexHealth indexHealth = new ClusterIndexHealth(index, indexMetaData.numberOfShards(),
					indexMetaData.numberOfReplicas(), validation.indexFailures(indexMetaData.index()));

			for (IndexShardRoutingTable shardRoutingTable : indexRoutingTable) {
				ClusterShardHealth shardHealth = new ClusterShardHealth(shardRoutingTable.shardId().id());
				for (ShardRouting shardRouting : shardRoutingTable) {
					if (shardRouting.active()) {
						shardHealth.activeShards++;
						if (shardRouting.relocating()) {

							shardHealth.relocatingShards++;
						}
						if (shardRouting.primary()) {
							shardHealth.primaryActive = true;
						}
					} else if (shardRouting.initializing()) {
						shardHealth.initializingShards++;
					} else if (shardRouting.unassigned()) {
						shardHealth.unassignedShards++;
					}
				}
				if (shardHealth.primaryActive) {
					if (shardHealth.activeShards == shardRoutingTable.size()) {
						shardHealth.status = ClusterHealthStatus.GREEN;
					} else {
						shardHealth.status = ClusterHealthStatus.YELLOW;
					}
				} else {
					shardHealth.status = ClusterHealthStatus.RED;
				}
				indexHealth.shards.put(shardHealth.id(), shardHealth);
			}

			for (ClusterShardHealth shardHealth : indexHealth) {
				if (shardHealth.primaryActive()) {
					indexHealth.activePrimaryShards++;
				}
				indexHealth.activeShards += shardHealth.activeShards;
				indexHealth.relocatingShards += shardHealth.relocatingShards;
				indexHealth.initializingShards += shardHealth.initializingShards;
				indexHealth.unassignedShards += shardHealth.unassignedShards;
			}

			indexHealth.status = ClusterHealthStatus.GREEN;
			if (!indexHealth.validationFailures().isEmpty()) {
				indexHealth.status = ClusterHealthStatus.RED;
			} else if (indexHealth.shards().isEmpty()) {
				indexHealth.status = ClusterHealthStatus.RED;
			} else {
				for (ClusterShardHealth shardHealth : indexHealth) {
					if (shardHealth.status() == ClusterHealthStatus.RED) {
						indexHealth.status = ClusterHealthStatus.RED;
						break;
					}
					if (shardHealth.status() == ClusterHealthStatus.YELLOW) {
						indexHealth.status = ClusterHealthStatus.YELLOW;
					}
				}
			}

			response.indices.put(indexHealth.index(), indexHealth);
		}

		for (ClusterIndexHealth indexHealth : response) {
			response.activePrimaryShards += indexHealth.activePrimaryShards;
			response.activeShards += indexHealth.activeShards;
			response.relocatingShards += indexHealth.relocatingShards;
			response.initializingShards += indexHealth.initializingShards;
			response.unassignedShards += indexHealth.unassignedShards;
		}

		response.status = ClusterHealthStatus.GREEN;
		if (!response.validationFailures().isEmpty()) {
			response.status = ClusterHealthStatus.RED;
		} else if (clusterState.blocks().hasGlobalBlock(RestStatus.SERVICE_UNAVAILABLE)) {
			response.status = ClusterHealthStatus.RED;
		} else {
			for (ClusterIndexHealth indexHealth : response) {
				if (indexHealth.status() == ClusterHealthStatus.RED) {
					response.status = ClusterHealthStatus.RED;
					break;
				}
				if (indexHealth.status() == ClusterHealthStatus.YELLOW) {
					response.status = ClusterHealthStatus.YELLOW;
				}
			}
		}

		return response;
	}
}
