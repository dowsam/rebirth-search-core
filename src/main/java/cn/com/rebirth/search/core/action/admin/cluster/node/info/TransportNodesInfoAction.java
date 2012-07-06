/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportNodesInfoAction.java 2012-7-6 14:30:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest;
import cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.node.service.NodeService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportNodesInfoAction.
 *
 * @author l.xue.nong
 */
public class TransportNodesInfoAction
		extends
		TransportNodesOperationAction<NodesInfoRequest, NodesInfoResponse, TransportNodesInfoAction.NodeInfoRequest, NodeInfo> {

	/** The node service. */
	private final NodeService nodeService;

	/**
	 * Instantiates a new transport nodes info action.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param nodeService the node service
	 */
	@Inject
	public TransportNodesInfoAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
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
		return NodesInfoAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newResponse(cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected NodesInfoResponse newResponse(NodesInfoRequest nodesInfoRequest, AtomicReferenceArray responses) {
		final List<NodeInfo> nodesInfos = new ArrayList<NodeInfo>();
		for (int i = 0; i < responses.length(); i++) {
			Object resp = responses.get(i);
			if (resp instanceof NodeInfo) {
				nodesInfos.add((NodeInfo) resp);
			}
		}
		return new NodesInfoResponse(clusterName, nodesInfos.toArray(new NodeInfo[nodesInfos.size()]));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newRequest()
	 */
	@Override
	protected NodesInfoRequest newRequest() {
		return new NodesInfoRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest()
	 */
	@Override
	protected NodeInfoRequest newNodeRequest() {
		return new NodeInfoRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest(java.lang.String, cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest)
	 */
	@Override
	protected NodeInfoRequest newNodeRequest(String nodeId, NodesInfoRequest request) {
		return new NodeInfoRequest(nodeId, request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeResponse()
	 */
	@Override
	protected NodeInfo newNodeResponse() {
		return new NodeInfo();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#nodeOperation(cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest)
	 */
	@Override
	protected NodeInfo nodeOperation(NodeInfoRequest nodeRequest) throws RebirthException {
		NodesInfoRequest request = nodeRequest.request;
		return nodeService.info(request.settings(), request.os(), request.process(), request.jvm(),
				request.threadPool(), request.network(), request.transport(), request.http());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#accumulateExceptions()
	 */
	@Override
	protected boolean accumulateExceptions() {
		return false;
	}

	/**
	 * The Class NodeInfoRequest.
	 *
	 * @author l.xue.nong
	 */
	static class NodeInfoRequest extends NodeOperationRequest {

		/** The request. */
		NodesInfoRequest request;

		/**
		 * Instantiates a new node info request.
		 */
		NodeInfoRequest() {
		}

		/**
		 * Instantiates a new node info request.
		 *
		 * @param nodeId the node id
		 * @param request the request
		 */
		NodeInfoRequest(String nodeId, NodesInfoRequest request) {
			super(nodeId);
			this.request = request;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			request = new NodesInfoRequest();
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
