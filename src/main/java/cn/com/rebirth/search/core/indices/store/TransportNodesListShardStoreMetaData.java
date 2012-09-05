/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportNodesListShardStoreMetaData.java 2012-7-6 14:28:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.store;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
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
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.index.store.Store;
import cn.com.rebirth.search.core.index.store.StoreFileMetaData;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class TransportNodesListShardStoreMetaData.
 *
 * @author l.xue.nong
 */
public class TransportNodesListShardStoreMetaData
		extends
		TransportNodesOperationAction<TransportNodesListShardStoreMetaData.Request, TransportNodesListShardStoreMetaData.NodesStoreFilesMetaData, TransportNodesListShardStoreMetaData.NodeRequest, TransportNodesListShardStoreMetaData.NodeStoreFilesMetaData> {

	/** The indices service. */
	private final IndicesService indicesService;

	/** The node env. */
	private final NodeEnvironment nodeEnv;

	/**
	 * Instantiates a new transport nodes list shard store meta data.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 * @param nodeEnv the node env
	 */
	@Inject
	public TransportNodesListShardStoreMetaData(Settings settings, ClusterName clusterName, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService, IndicesService indicesService,
			NodeEnvironment nodeEnv) {
		super(settings, clusterName, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
		this.nodeEnv = nodeEnv;
	}

	/**
	 * List.
	 *
	 * @param shardId the shard id
	 * @param onlyUnallocated the only unallocated
	 * @param nodesIds the nodes ids
	 * @param timeout the timeout
	 * @return the action future
	 */
	public ActionFuture<NodesStoreFilesMetaData> list(ShardId shardId, boolean onlyUnallocated, Set<String> nodesIds,
			@Nullable TimeValue timeout) {
		return execute(new Request(shardId, onlyUnallocated, nodesIds).timeout(timeout));
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
		return "/cluster/nodes/indices/shard/store";
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
		return new NodeRequest(nodeId, request.shardId, request.unallocated);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeResponse()
	 */
	@Override
	protected NodeStoreFilesMetaData newNodeResponse() {
		return new NodeStoreFilesMetaData();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newResponse(cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray)
	 */
	@Override
	protected NodesStoreFilesMetaData newResponse(Request request, AtomicReferenceArray responses) {
		final List<NodeStoreFilesMetaData> nodeStoreFilesMetaDatas = Lists.newArrayList();
		final List<FailedNodeException> failures = Lists.newArrayList();
		for (int i = 0; i < responses.length(); i++) {
			Object resp = responses.get(i);
			if (resp instanceof NodeStoreFilesMetaData) {
				nodeStoreFilesMetaDatas.add((NodeStoreFilesMetaData) resp);
			} else if (resp instanceof FailedNodeException) {
				failures.add((FailedNodeException) resp);
			}
		}
		return new NodesStoreFilesMetaData(clusterName,
				nodeStoreFilesMetaDatas.toArray(new NodeStoreFilesMetaData[nodeStoreFilesMetaDatas.size()]),
				failures.toArray(new FailedNodeException[failures.size()]));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#nodeOperation(cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest)
	 */
	@Override
	protected NodeStoreFilesMetaData nodeOperation(NodeRequest request) throws RebirthException {
		if (request.unallocated) {
			IndexService indexService = indicesService.indexService(request.shardId.index().name());
			if (indexService == null) {
				return new NodeStoreFilesMetaData(clusterService.state().nodes().localNode(), null);
			}
			if (!indexService.hasShard(request.shardId.id())) {
				return new NodeStoreFilesMetaData(clusterService.state().nodes().localNode(), null);
			}
		}
		IndexMetaData metaData = clusterService.state().metaData().index(request.shardId.index().name());
		if (metaData == null) {
			return new NodeStoreFilesMetaData(clusterService.state().nodes().localNode(), null);
		}
		try {
			return new NodeStoreFilesMetaData(clusterService.state().nodes().localNode(),
					listStoreMetaData(request.shardId));
		} catch (IOException e) {
			throw new RebirthException("Failed to list store metadata for shard [" + request.shardId + "]", e);
		}
	}

	/**
	 * List store meta data.
	 *
	 * @param shardId the shard id
	 * @return the store files meta data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private StoreFilesMetaData listStoreMetaData(ShardId shardId) throws IOException {
		IndexService indexService = indicesService.indexService(shardId.index().name());
		if (indexService != null) {
			InternalIndexShard indexShard = (InternalIndexShard) indexService.shard(shardId.id());
			if (indexShard != null) {
				return new StoreFilesMetaData(true, shardId, indexShard.store().list());
			}
		}

		IndexMetaData metaData = clusterService.state().metaData().index(shardId.index().name());
		if (metaData == null) {
			return new StoreFilesMetaData(false, shardId, ImmutableMap.<String, StoreFileMetaData> of());
		}
		String storeType = metaData.settings().get("index.store.type", "fs");
		if (!storeType.contains("fs")) {
			return new StoreFilesMetaData(false, shardId, ImmutableMap.<String, StoreFileMetaData> of());
		}
		File[] shardLocations = nodeEnv.shardLocations(shardId);
		File[] shardIndexLocations = new File[shardLocations.length];
		for (int i = 0; i < shardLocations.length; i++) {
			shardIndexLocations[i] = new File(shardLocations[i], "index");
		}
		boolean exists = false;
		for (File shardIndexLocation : shardIndexLocations) {
			if (shardIndexLocation.exists()) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			return new StoreFilesMetaData(false, shardId, ImmutableMap.<String, StoreFileMetaData> of());
		}

		Map<String, String> checksums = Store.readChecksums(shardIndexLocations);
		if (checksums == null) {
			checksums = ImmutableMap.of();
		}

		Map<String, StoreFileMetaData> files = Maps.newHashMap();
		for (File shardIndexLocation : shardIndexLocations) {
			File[] listedFiles = shardIndexLocation.listFiles();
			if (listedFiles == null) {
				continue;
			}
			for (File file : listedFiles) {

				if (file.getName().endsWith(".cks")) {
					continue;
				}
				if (Store.isChecksum(file.getName())) {
					continue;
				}
				files.put(file.getName(), new StoreFileMetaData(file.getName(), file.length(), file.lastModified(),
						checksums.get(file.getName())));
			}
		}

		return new StoreFilesMetaData(false, shardId, files);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#accumulateExceptions()
	 */
	@Override
	protected boolean accumulateExceptions() {
		return true;
	}

	/**
	 * The Class StoreFilesMetaData.
	 *
	 * @author l.xue.nong
	 */
	public static class StoreFilesMetaData implements Iterable<StoreFileMetaData>, Streamable {

		/** The allocated. */
		private boolean allocated;

		/** The shard id. */
		private ShardId shardId;

		/** The files. */
		private Map<String, StoreFileMetaData> files;

		/**
		 * Instantiates a new store files meta data.
		 */
		StoreFilesMetaData() {
		}

		/**
		 * Instantiates a new store files meta data.
		 *
		 * @param allocated the allocated
		 * @param shardId the shard id
		 * @param files the files
		 */
		public StoreFilesMetaData(boolean allocated, ShardId shardId, Map<String, StoreFileMetaData> files) {
			this.allocated = allocated;
			this.shardId = shardId;
			this.files = files;
		}

		/**
		 * Allocated.
		 *
		 * @return true, if successful
		 */
		public boolean allocated() {
			return allocated;
		}

		/**
		 * Shard id.
		 *
		 * @return the shard id
		 */
		public ShardId shardId() {
			return this.shardId;
		}

		/**
		 * Total size in bytes.
		 *
		 * @return the long
		 */
		public long totalSizeInBytes() {
			long totalSizeInBytes = 0;
			for (StoreFileMetaData file : this) {
				totalSizeInBytes += file.length();
			}
			return totalSizeInBytes;
		}

		/* (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<StoreFileMetaData> iterator() {
			return files.values().iterator();
		}

		/**
		 * File exists.
		 *
		 * @param name the name
		 * @return true, if successful
		 */
		public boolean fileExists(String name) {
			return files.containsKey(name);
		}

		/**
		 * File.
		 *
		 * @param name the name
		 * @return the store file meta data
		 */
		public StoreFileMetaData file(String name) {
			return files.get(name);
		}

		/**
		 * Read store files meta data.
		 *
		 * @param in the in
		 * @return the store files meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static StoreFilesMetaData readStoreFilesMetaData(StreamInput in) throws IOException {
			StoreFilesMetaData md = new StoreFilesMetaData();
			md.readFrom(in);
			return md;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			allocated = in.readBoolean();
			shardId = ShardId.readShardId(in);
			int size = in.readVInt();
			files = Maps.newHashMapWithExpectedSize(size);
			for (int i = 0; i < size; i++) {
				StoreFileMetaData md = StoreFileMetaData.readStoreFileMetaData(in);
				files.put(md.name(), md);
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeBoolean(allocated);
			shardId.writeTo(out);
			out.writeVInt(files.size());
			for (StoreFileMetaData md : files.values()) {
				md.writeTo(out);
			}
		}
	}

	/**
	 * The Class Request.
	 *
	 * @author l.xue.nong
	 */
	static class Request extends NodesOperationRequest {

		/** The shard id. */
		private ShardId shardId;

		/** The unallocated. */
		private boolean unallocated;

		/**
		 * Instantiates a new request.
		 */
		public Request() {
		}

		/**
		 * Instantiates a new request.
		 *
		 * @param shardId the shard id
		 * @param unallocated the unallocated
		 * @param nodesIds the nodes ids
		 */
		public Request(ShardId shardId, boolean unallocated, Set<String> nodesIds) {
			super(nodesIds.toArray(new String[nodesIds.size()]));
			this.shardId = shardId;
			this.unallocated = unallocated;
		}

		/**
		 * Instantiates a new request.
		 *
		 * @param shardId the shard id
		 * @param unallocated the unallocated
		 * @param nodesIds the nodes ids
		 */
		public Request(ShardId shardId, boolean unallocated, String... nodesIds) {
			super(nodesIds);
			this.shardId = shardId;
			this.unallocated = unallocated;
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
			shardId = ShardId.readShardId(in);
			unallocated = in.readBoolean();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			shardId.writeTo(out);
			out.writeBoolean(unallocated);
		}
	}

	/**
	 * The Class NodesStoreFilesMetaData.
	 *
	 * @author l.xue.nong
	 */
	public static class NodesStoreFilesMetaData extends NodesOperationResponse<NodeStoreFilesMetaData> {

		/** The failures. */
		private FailedNodeException[] failures;

		/**
		 * Instantiates a new nodes store files meta data.
		 */
		NodesStoreFilesMetaData() {
		}

		/**
		 * Instantiates a new nodes store files meta data.
		 *
		 * @param clusterName the cluster name
		 * @param nodes the nodes
		 * @param failures the failures
		 */
		public NodesStoreFilesMetaData(ClusterName clusterName, NodeStoreFilesMetaData[] nodes,
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
			nodes = new NodeStoreFilesMetaData[in.readVInt()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = NodeStoreFilesMetaData.readListShardStoreNodeOperationResponse(in);
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			out.writeVInt(nodes.length);
			for (NodeStoreFilesMetaData response : nodes) {
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
		private ShardId shardId;

		/** The unallocated. */
		private boolean unallocated;

		/**
		 * Instantiates a new node request.
		 */
		NodeRequest() {
		}

		/**
		 * Instantiates a new node request.
		 *
		 * @param nodeId the node id
		 * @param shardId the shard id
		 * @param unallocated the unallocated
		 */
		NodeRequest(String nodeId, ShardId shardId, boolean unallocated) {
			super(nodeId);
			this.shardId = shardId;
			this.unallocated = unallocated;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			shardId = ShardId.readShardId(in);
			unallocated = in.readBoolean();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			shardId.writeTo(out);
			out.writeBoolean(unallocated);
		}
	}

	/**
	 * The Class NodeStoreFilesMetaData.
	 *
	 * @author l.xue.nong
	 */
	public static class NodeStoreFilesMetaData extends NodeOperationResponse {

		/** The store files meta data. */
		private StoreFilesMetaData storeFilesMetaData;

		/**
		 * Instantiates a new node store files meta data.
		 */
		NodeStoreFilesMetaData() {
		}

		/**
		 * Instantiates a new node store files meta data.
		 *
		 * @param node the node
		 * @param storeFilesMetaData the store files meta data
		 */
		public NodeStoreFilesMetaData(DiscoveryNode node, StoreFilesMetaData storeFilesMetaData) {
			super(node);
			this.storeFilesMetaData = storeFilesMetaData;
		}

		/**
		 * Store files meta data.
		 *
		 * @return the store files meta data
		 */
		public StoreFilesMetaData storeFilesMetaData() {
			return storeFilesMetaData;
		}

		/**
		 * Read list shard store node operation response.
		 *
		 * @param in the in
		 * @return the node store files meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static NodeStoreFilesMetaData readListShardStoreNodeOperationResponse(StreamInput in) throws IOException {
			NodeStoreFilesMetaData resp = new NodeStoreFilesMetaData();
			resp.readFrom(in);
			return resp;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			if (in.readBoolean()) {
				storeFilesMetaData = StoreFilesMetaData.readStoreFilesMetaData(in);
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			if (storeFilesMetaData == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				storeFilesMetaData.writeTo(out);
			}
		}
	}
}
