/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportNodesListGatewayStartedShards.java 2012-3-29 15:02:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.local.state.shards;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.FailedNodeException;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse;
import cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Lists;

/**
 * The Class TransportNodesListGatewayStartedShards.
 *
 * @author l.xue.nong
 */
public class TransportNodesListGatewayStartedShards
		extends
		TransportNodesOperationAction<TransportNodesListGatewayStartedShards.Request, TransportNodesListGatewayStartedShards.NodesLocalGatewayStartedShards, TransportNodesListGatewayStartedShards.NodeRequest, TransportNodesListGatewayStartedShards.NodeLocalGatewayStartedShards> {

	/** The shards state. */
	private LocalGatewayShardsState shardsState;

	/**
	 * Instantiates a new transport nodes list gateway started shards.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 */
	@Inject
	public TransportNodesListGatewayStartedShards(Settings settings, ClusterName clusterName, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService) {
		super(settings, clusterName, threadPool, clusterService, transportService);
	}

	/**
	 * Inits the gateway.
	 *
	 * @param shardsState the shards state
	 * @return the transport nodes list gateway started shards
	 */
	TransportNodesListGatewayStartedShards initGateway(LocalGatewayShardsState shardsState) {
		this.shardsState = shardsState;
		return this;
	}

	/**
	 * List.
	 *
	 * @param shardId the shard id
	 * @param nodesIds the nodes ids
	 * @param timeout the timeout
	 * @return the action future
	 */
	public ActionFuture<NodesLocalGatewayStartedShards> list(ShardId shardId, Set<String> nodesIds,
			@Nullable TimeValue timeout) {
		return execute(new Request(shardId, nodesIds).timeout(timeout));
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return "/gateway/local/started-shards";
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#transportCompress()
	 */
	@Override
	protected boolean transportCompress() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#newRequest()
	 */
	@Override
	protected Request newRequest() {
		return new Request();
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest()
	 */
	@Override
	protected NodeRequest newNodeRequest() {
		return new NodeRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest(java.lang.String, cn.com.summall.search.core.action.support.nodes.NodesOperationRequest)
	 */
	@Override
	protected NodeRequest newNodeRequest(String nodeId, Request request) {
		return new NodeRequest(request.shardId(), nodeId);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#newNodeResponse()
	 */
	@Override
	protected NodeLocalGatewayStartedShards newNodeResponse() {
		return new NodeLocalGatewayStartedShards();
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#newResponse(cn.com.summall.search.core.action.support.nodes.NodesOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray)
	 */
	@Override
	protected NodesLocalGatewayStartedShards newResponse(Request request, AtomicReferenceArray responses) {
		final List<NodeLocalGatewayStartedShards> nodesList = Lists.newArrayList();
		final List<FailedNodeException> failures = Lists.newArrayList();
		for (int i = 0; i < responses.length(); i++) {
			Object resp = responses.get(i);
			if (resp instanceof NodeLocalGatewayStartedShards) {
				nodesList.add((NodeLocalGatewayStartedShards) resp);
			} else if (resp instanceof FailedNodeException) {
				failures.add((FailedNodeException) resp);
			}
		}
		return new NodesLocalGatewayStartedShards(clusterName,
				nodesList.toArray(new NodeLocalGatewayStartedShards[nodesList.size()]),
				failures.toArray(new FailedNodeException[failures.size()]));
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#nodeOperation(cn.com.summall.search.core.action.support.nodes.NodeOperationRequest)
	 */
	@Override
	protected NodeLocalGatewayStartedShards nodeOperation(NodeRequest request) throws RestartException {
		Map<ShardId, ShardStateInfo> shardsStateInfo = shardsState.currentStartedShards();
		if (shardsStateInfo != null) {
			for (Map.Entry<ShardId, ShardStateInfo> entry : shardsStateInfo.entrySet()) {
				if (entry.getKey().equals(request.shardId)) {
					return new NodeLocalGatewayStartedShards(clusterService.localNode(), entry.getValue().version);
				}
			}
		}
		return new NodeLocalGatewayStartedShards(clusterService.localNode(), -1);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.nodes.TransportNodesOperationAction#accumulateExceptions()
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

		/** The shard id. */
		private ShardId shardId;

		/**
		 * Instantiates a new request.
		 */
		public Request() {
		}

		/**
		 * Instantiates a new request.
		 *
		 * @param shardId the shard id
		 * @param nodesIds the nodes ids
		 */
		public Request(ShardId shardId, Set<String> nodesIds) {
			super(nodesIds.toArray(new String[nodesIds.size()]));
			this.shardId = shardId;
		}

		/**
		 * Shard id.
		 *
		 * @return the shard id
		 */
		public ShardId shardId() {
			return this.shardId;
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.nodes.NodesOperationRequest#timeout(cn.com.summall.search.commons.unit.TimeValue)
		 */
		@Override
		public Request timeout(TimeValue timeout) {
			super.timeout(timeout);
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.nodes.NodesOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			shardId = ShardId.readShardId(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.nodes.NodesOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			shardId.writeTo(out);
		}
	}

	/**
	 * The Class NodesLocalGatewayStartedShards.
	 *
	 * @author l.xue.nong
	 */
	public static class NodesLocalGatewayStartedShards extends NodesOperationResponse<NodeLocalGatewayStartedShards> {

		/** The failures. */
		private FailedNodeException[] failures;

		/**
		 * Instantiates a new nodes local gateway started shards.
		 */
		NodesLocalGatewayStartedShards() {
		}

		/**
		 * Instantiates a new nodes local gateway started shards.
		 *
		 * @param clusterName the cluster name
		 * @param nodes the nodes
		 * @param failures the failures
		 */
		public NodesLocalGatewayStartedShards(ClusterName clusterName, NodeLocalGatewayStartedShards[] nodes,
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
		 * @see cn.com.summall.search.core.action.support.nodes.NodesOperationResponse#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			nodes = new NodeLocalGatewayStartedShards[in.readVInt()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = new NodeLocalGatewayStartedShards();
				nodes[i].readFrom(in);
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.nodes.NodesOperationResponse#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			out.writeVInt(nodes.length);
			for (NodeLocalGatewayStartedShards response : nodes) {
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

		/** The shard id. */
		ShardId shardId;

		/**
		 * Instantiates a new node request.
		 */
		NodeRequest() {
		}

		/**
		 * Instantiates a new node request.
		 *
		 * @param shardId the shard id
		 * @param nodeId the node id
		 */
		NodeRequest(ShardId shardId, String nodeId) {
			super(nodeId);
			this.shardId = shardId;
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.nodes.NodeOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			shardId = ShardId.readShardId(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.nodes.NodeOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			shardId.writeTo(out);
		}
	}

	/**
	 * The Class NodeLocalGatewayStartedShards.
	 *
	 * @author l.xue.nong
	 */
	public static class NodeLocalGatewayStartedShards extends NodeOperationResponse {

		/** The version. */
		private long version = -1;

		/**
		 * Instantiates a new node local gateway started shards.
		 */
		NodeLocalGatewayStartedShards() {
		}

		/**
		 * Instantiates a new node local gateway started shards.
		 *
		 * @param node the node
		 * @param version the version
		 */
		public NodeLocalGatewayStartedShards(DiscoveryNode node, long version) {
			super(node);
			this.version = version;
		}

		/**
		 * Checks for version.
		 *
		 * @return true, if successful
		 */
		public boolean hasVersion() {
			return version != -1;
		}

		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return this.version;
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.nodes.NodeOperationResponse#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			version = in.readLong();
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.nodes.NodeOperationResponse#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			out.writeLong(version);
		}
	}
}
