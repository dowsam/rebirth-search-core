/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportNodesStatsAction.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.stats;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest;
import cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.node.service.NodeService;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Lists;

/**
 * The Class TransportNodesStatsAction.
 *
 * @author l.xue.nong
 */
public class TransportNodesStatsAction
		extends
		TransportNodesOperationAction<NodesStatsRequest, NodesStatsResponse, TransportNodesStatsAction.NodeStatsRequest, NodeStats> {

	/** The node service. */
	private final NodeService nodeService;

	/**
	 * Instantiates a new transport nodes stats action.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param nodeService the node service
	 */
	@Inject
	public TransportNodesStatsAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService, NodeService nodeService) {
		super(settings, clusterName, threadPool, clusterService, transportService);
		this.nodeService = nodeService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.MANAGEMENT;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return NodesStatsAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newResponse(cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected NodesStatsResponse newResponse(NodesStatsRequest nodesInfoRequest, AtomicReferenceArray responses) {
		final List<NodeStats> nodeStats = Lists.newArrayList();
		for (int i = 0; i < responses.length(); i++) {
			Object resp = responses.get(i);
			if (resp instanceof NodeStats) {
				nodeStats.add((NodeStats) resp);
			}
		}
		return new NodesStatsResponse(clusterName, nodeStats.toArray(new NodeStats[nodeStats.size()]));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newRequest()
	 */
	@Override
	protected NodesStatsRequest newRequest() {
		return new NodesStatsRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest()
	 */
	@Override
	protected NodeStatsRequest newNodeRequest() {
		return new NodeStatsRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest(java.lang.String, cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest)
	 */
	@Override
	protected NodeStatsRequest newNodeRequest(String nodeId, NodesStatsRequest request) {
		return new NodeStatsRequest(nodeId, request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeResponse()
	 */
	@Override
	protected NodeStats newNodeResponse() {
		return new NodeStats();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#nodeOperation(cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest)
	 */
	@Override
	protected NodeStats nodeOperation(NodeStatsRequest nodeStatsRequest) throws RebirthException {
		NodesStatsRequest request = nodeStatsRequest.request;
		return nodeService.stats(request.indices(), request.os(), request.process(), request.jvm(),
				request.threadPool(), request.network(), request.fs(), request.transport(), request.http());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#accumulateExceptions()
	 */
	@Override
	protected boolean accumulateExceptions() {
		return false;
	}

	/**
	 * The Class NodeStatsRequest.
	 *
	 * @author l.xue.nong
	 */
	static class NodeStatsRequest extends NodeOperationRequest {

		/** The request. */
		NodesStatsRequest request;

		/**
		 * Instantiates a new node stats request.
		 */
		NodeStatsRequest() {
		}

		/**
		 * Instantiates a new node stats request.
		 *
		 * @param nodeId the node id
		 * @param request the request
		 */
		NodeStatsRequest(String nodeId, NodesStatsRequest request) {
			super(nodeId);
			this.request = request;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			request = new NodesStatsRequest();
			request.readFrom(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			request.writeTo(out);
		}
	}
}