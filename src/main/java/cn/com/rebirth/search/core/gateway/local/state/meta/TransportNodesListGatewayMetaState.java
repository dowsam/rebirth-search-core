/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportNodesListGatewayMetaState.java 2012-7-6 14:30:30 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.local.state.meta;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.FailedNodeException;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse;
import cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Lists;

/**
 * The Class TransportNodesListGatewayMetaState.
 *
 * @author l.xue.nong
 */
public class TransportNodesListGatewayMetaState
		extends
		TransportNodesOperationAction<TransportNodesListGatewayMetaState.Request, TransportNodesListGatewayMetaState.NodesLocalGatewayMetaState, TransportNodesListGatewayMetaState.NodeRequest, TransportNodesListGatewayMetaState.NodeLocalGatewayMetaState> {

	/** The meta state. */
	private LocalGatewayMetaState metaState;

	/**
	 * Instantiates a new transport nodes list gateway meta state.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 */
	@Inject
	public TransportNodesListGatewayMetaState(Settings settings, ClusterName clusterName, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService) {
		super(settings, clusterName, threadPool, clusterService, transportService);
	}

	/**
	 * Inits the.
	 *
	 * @param metaState the meta state
	 * @return the transport nodes list gateway meta state
	 */
	TransportNodesListGatewayMetaState init(LocalGatewayMetaState metaState) {
		this.metaState = metaState;
		return this;
	}

	/**
	 * List.
	 *
	 * @param nodesIds the nodes ids
	 * @param timeout the timeout
	 * @return the action future
	 */
	public ActionFuture<NodesLocalGatewayMetaState> list(Set<String> nodesIds, @Nullable TimeValue timeout) {
		return execute(new Request(nodesIds).timeout(timeout));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return "/gateway/local/meta-state";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#transportCompress()
	 */
	@Override
	protected boolean transportCompress() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newRequest()
	 */
	@Override
	protected Request newRequest() {
		return new Request();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest()
	 */
	@Override
	protected NodeRequest newNodeRequest() {
		return new NodeRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest(java.lang.String, cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest)
	 */
	@Override
	protected NodeRequest newNodeRequest(String nodeId, Request request) {
		return new NodeRequest(nodeId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeResponse()
	 */
	@Override
	protected NodeLocalGatewayMetaState newNodeResponse() {
		return new NodeLocalGatewayMetaState();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newResponse(cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray)
	 */
	@Override
	protected NodesLocalGatewayMetaState newResponse(Request request, AtomicReferenceArray responses) {
		final List<NodeLocalGatewayMetaState> nodesList = Lists.newArrayList();
		final List<FailedNodeException> failures = Lists.newArrayList();
		for (int i = 0; i < responses.length(); i++) {
			Object resp = responses.get(i);
			if (resp instanceof NodeLocalGatewayMetaState) {
				nodesList.add((NodeLocalGatewayMetaState) resp);
			} else if (resp instanceof FailedNodeException) {
				failures.add((FailedNodeException) resp);
			}
		}
		return new NodesLocalGatewayMetaState(clusterName, nodesList.toArray(new NodeLocalGatewayMetaState[nodesList
				.size()]), failures.toArray(new FailedNodeException[failures.size()]));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#nodeOperation(cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest)
	 */
	@Override
	protected NodeLocalGatewayMetaState nodeOperation(NodeRequest request) throws RebirthException {
		return new NodeLocalGatewayMetaState(clusterService.localNode(), metaState.currentMetaData());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#accumulateExceptions()
	 */
	@Override
	protected boolean accumulateExceptions() {
		return true;
	}

	/**
	 * The Class Request.
	 *
	 * @author l.xue.nong
	 */
	static class Request extends NodesOperationRequest {

		/**
		 * Instantiates a new request.
		 */
		public Request() {
		}

		/**
		 * Instantiates a new request.
		 *
		 * @param nodesIds the nodes ids
		 */
		public Request(Set<String> nodesIds) {
			super(nodesIds.toArray(new String[nodesIds.size()]));
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest#timeout(cn.com.rebirth.commons.unit.TimeValue)
		 */
		@Override
		public Request timeout(TimeValue timeout) {
			super.timeout(timeout);
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
		}
	}

	/**
	 * The Class NodesLocalGatewayMetaState.
	 *
	 * @author l.xue.nong
	 */
	public static class NodesLocalGatewayMetaState extends NodesOperationResponse<NodeLocalGatewayMetaState> {

		/** The failures. */
		private FailedNodeException[] failures;

		/**
		 * Instantiates a new nodes local gateway meta state.
		 */
		NodesLocalGatewayMetaState() {
		}

		/**
		 * Instantiates a new nodes local gateway meta state.
		 *
		 * @param clusterName the cluster name
		 * @param nodes the nodes
		 * @param failures the failures
		 */
		public NodesLocalGatewayMetaState(ClusterName clusterName, NodeLocalGatewayMetaState[] nodes,
				FailedNodeException[] failures) {
			super(clusterName, nodes);
			this.failures = failures;
		}

		/**
		 * Failures.
		 *
		 * @return the failed node exception[]
		 */
		public FailedNodeException[] failures() {
			return failures;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			nodes = new NodeLocalGatewayMetaState[in.readVInt()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = new NodeLocalGatewayMetaState();
				nodes[i].readFrom(in);
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			out.writeVInt(nodes.length);
			for (NodeLocalGatewayMetaState response : nodes) {
				response.writeTo(out);
			}
		}
	}

	/**
	 * The Class NodeRequest.
	 *
	 * @author l.xue.nong
	 */
	static class NodeRequest extends NodeOperationRequest {

		/**
		 * Instantiates a new node request.
		 */
		NodeRequest() {
		}

		/**
		 * Instantiates a new node request.
		 *
		 * @param nodeId the node id
		 */
		NodeRequest(String nodeId) {
			super(nodeId);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
		}
	}

	/**
	 * The Class NodeLocalGatewayMetaState.
	 *
	 * @author l.xue.nong
	 */
	public static class NodeLocalGatewayMetaState extends NodeOperationResponse {

		/** The meta data. */
		private MetaData metaData;

		/**
		 * Instantiates a new node local gateway meta state.
		 */
		NodeLocalGatewayMetaState() {
		}

		/**
		 * Instantiates a new node local gateway meta state.
		 *
		 * @param node the node
		 * @param metaData the meta data
		 */
		public NodeLocalGatewayMetaState(DiscoveryNode node, MetaData metaData) {
			super(node);
			this.metaData = metaData;
		}

		/**
		 * Meta data.
		 *
		 * @return the meta data
		 */
		public MetaData metaData() {
			return metaData;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			if (in.readBoolean()) {
				metaData = MetaData.Builder.readFrom(in);
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			if (metaData == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				MetaData.Builder.writeTo(metaData, out);
			}
		}
	}
}
