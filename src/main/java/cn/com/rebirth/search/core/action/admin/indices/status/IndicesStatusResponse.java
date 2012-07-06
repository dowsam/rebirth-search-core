/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesStatusResponse.java 2012-3-29 15:02:35 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.status;

import static cn.com.rebirth.search.core.action.admin.indices.status.ShardStatus.readIndexShardStatus;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.settings.SettingsFilter;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.index.flush.FlushStats;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.refresh.RefreshStats;

import com.google.common.collect.Sets;


/**
 * The Class IndicesStatusResponse.
 *
 * @author l.xue.nong
 */
public class IndicesStatusResponse extends BroadcastOperationResponse implements ToXContent {

	
	/** The shards. */
	protected ShardStatus[] shards;

	
	/** The indices status. */
	private Map<String, IndexStatus> indicesStatus;

	
	/**
	 * Instantiates a new indices status response.
	 */
	IndicesStatusResponse() {
	}

	
	/**
	 * Instantiates a new indices status response.
	 *
	 * @param shards the shards
	 * @param clusterState the cluster state
	 * @param totalShards the total shards
	 * @param successfulShards the successful shards
	 * @param failedShards the failed shards
	 * @param shardFailures the shard failures
	 */
	IndicesStatusResponse(ShardStatus[] shards, ClusterState clusterState, int totalShards, int successfulShards,
			int failedShards, List<ShardOperationFailedException> shardFailures) {
		super(totalShards, successfulShards, failedShards, shardFailures);
		this.shards = shards;
	}

	
	/**
	 * Shards.
	 *
	 * @return the shard status[]
	 */
	public ShardStatus[] shards() {
		return this.shards;
	}

	
	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public ShardStatus[] getShards() {
		return this.shards;
	}

	
	/**
	 * Gets the at.
	 *
	 * @param position the position
	 * @return the at
	 */
	public ShardStatus getAt(int position) {
		return shards[position];
	}

	
	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the index status
	 */
	public IndexStatus index(String index) {
		return indices().get(index);
	}

	
	/**
	 * Gets the indices.
	 *
	 * @return the indices
	 */
	public Map<String, IndexStatus> getIndices() {
		return indices();
	}

	
	/**
	 * Indices.
	 *
	 * @return the map
	 */
	public Map<String, IndexStatus> indices() {
		if (indicesStatus != null) {
			return indicesStatus;
		}
		Map<String, IndexStatus> indicesStatus = newHashMap();

		Set<String> indices = Sets.newHashSet();
		for (ShardStatus shard : shards) {
			indices.add(shard.index());
		}

		for (String index : indices) {
			List<ShardStatus> shards = newArrayList();
			for (ShardStatus shard : this.shards) {
				if (shard.shardRouting().index().equals(index)) {
					shards.add(shard);
				}
			}
			indicesStatus.put(index, new IndexStatus(index, shards.toArray(new ShardStatus[shards.size()])));
		}
		this.indicesStatus = indicesStatus;
		return indicesStatus;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationResponse#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(shards().length);
		for (ShardStatus status : shards()) {
			status.writeTo(out);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationResponse#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		shards = new ShardStatus[in.readVInt()];
		for (int i = 0; i < shards.length; i++) {
			shards[i] = readIndexShardStatus(in);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		return toXContent(builder, params, null);
	}

	
	/**
	 * To x content.
	 *
	 * @param builder the builder
	 * @param params the params
	 * @param settingsFilter the settings filter
	 * @return the x content builder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public XContentBuilder toXContent(XContentBuilder builder, Params params, @Nullable SettingsFilter settingsFilter)
			throws IOException {
		builder.startObject(Fields.INDICES);
		for (IndexStatus indexStatus : indices().values()) {
			builder.startObject(indexStatus.index(), XContentBuilder.FieldCaseConversion.NONE);

			builder.startObject(Fields.INDEX);
			if (indexStatus.storeSize() != null) {
				builder.field(Fields.PRIMARY_SIZE, indexStatus.primaryStoreSize().toString());
				builder.field(Fields.PRIMARY_SIZE_IN_BYTES, indexStatus.primaryStoreSize().bytes());
				builder.field(Fields.SIZE, indexStatus.storeSize().toString());
				builder.field(Fields.SIZE_IN_BYTES, indexStatus.storeSize().bytes());
			}
			builder.endObject();
			if (indexStatus.translogOperations() != -1) {
				builder.startObject(Fields.TRANSLOG);
				builder.field(Fields.OPERATIONS, indexStatus.translogOperations());
				builder.endObject();
			}

			if (indexStatus.docs() != null) {
				builder.startObject(Fields.DOCS);
				builder.field(Fields.NUM_DOCS, indexStatus.docs().numDocs());
				builder.field(Fields.MAX_DOC, indexStatus.docs().maxDoc());
				builder.field(Fields.DELETED_DOCS, indexStatus.docs().deletedDocs());
				builder.endObject();
			}

			MergeStats mergeStats = indexStatus.mergeStats();
			if (mergeStats != null) {
				mergeStats.toXContent(builder, params);
			}
			RefreshStats refreshStats = indexStatus.refreshStats();
			if (refreshStats != null) {
				refreshStats.toXContent(builder, params);
			}
			FlushStats flushStats = indexStatus.flushStats();
			if (flushStats != null) {
				flushStats.toXContent(builder, params);
			}

			builder.startObject(Fields.SHARDS);
			for (IndexShardStatus indexShardStatus : indexStatus) {
				builder.startArray(Integer.toString(indexShardStatus.shardId().id()));
				for (ShardStatus shardStatus : indexShardStatus) {
					builder.startObject();

					builder.startObject(Fields.ROUTING).field(Fields.STATE, shardStatus.shardRouting().state())
							.field(Fields.PRIMARY, shardStatus.shardRouting().primary())
							.field(Fields.NODE, shardStatus.shardRouting().currentNodeId())
							.field(Fields.RELOCATING_NODE, shardStatus.shardRouting().relocatingNodeId())
							.field(Fields.SHARD, shardStatus.shardRouting().shardId().id())
							.field(Fields.INDEX, shardStatus.shardRouting().shardId().index().name()).endObject();

					builder.field(Fields.STATE, shardStatus.state());
					if (shardStatus.storeSize() != null) {
						builder.startObject(Fields.INDEX);
						builder.field(Fields.SIZE, shardStatus.storeSize().toString());
						builder.field(Fields.SIZE_IN_BYTES, shardStatus.storeSize().bytes());
						builder.endObject();
					}
					if (shardStatus.translogId() != -1) {
						builder.startObject(Fields.TRANSLOG);
						builder.field(Fields.ID, shardStatus.translogId());
						builder.field(Fields.OPERATIONS, shardStatus.translogOperations());
						builder.endObject();
					}

					if (shardStatus.docs() != null) {
						builder.startObject(Fields.DOCS);
						builder.field(Fields.NUM_DOCS, shardStatus.docs().numDocs());
						builder.field(Fields.MAX_DOC, shardStatus.docs().maxDoc());
						builder.field(Fields.DELETED_DOCS, shardStatus.docs().deletedDocs());
						builder.endObject();
					}

					mergeStats = shardStatus.mergeStats();
					if (mergeStats != null) {
						mergeStats.toXContent(builder, params);
					}

					refreshStats = shardStatus.refreshStats();
					if (refreshStats != null) {
						refreshStats.toXContent(builder, params);
					}
					flushStats = shardStatus.flushStats();
					if (flushStats != null) {
						flushStats.toXContent(builder, params);
					}

					if (shardStatus.peerRecoveryStatus() != null) {
						PeerRecoveryStatus peerRecoveryStatus = shardStatus.peerRecoveryStatus();
						builder.startObject(Fields.PEER_RECOVERY);
						builder.field(Fields.STAGE, peerRecoveryStatus.stage());
						builder.field(Fields.START_TIME_IN_MILLIS, peerRecoveryStatus.startTime());
						builder.field(Fields.TIME, peerRecoveryStatus.time());
						builder.field(Fields.TIME_IN_MILLIS, peerRecoveryStatus.time().millis());

						builder.startObject(Fields.INDEX);
						builder.field(Fields.PROGRESS, peerRecoveryStatus.indexRecoveryProgress());
						builder.field(Fields.SIZE, peerRecoveryStatus.indexSize());
						builder.field(Fields.SIZE_IN_BYTES, peerRecoveryStatus.indexSize().bytes());
						builder.field(Fields.REUSED_SIZE, peerRecoveryStatus.reusedIndexSize());
						builder.field(Fields.REUSED_SIZE_IN_BYTES, peerRecoveryStatus.reusedIndexSize().bytes());
						builder.field(Fields.EXPECTED_RECOVERED_SIZE, peerRecoveryStatus.expectedRecoveredIndexSize());
						builder.field(Fields.EXPECTED_RECOVERED_SIZE_IN_BYTES, peerRecoveryStatus
								.expectedRecoveredIndexSize().bytes());
						builder.field(Fields.RECOVERED_SIZE, peerRecoveryStatus.recoveredIndexSize());
						builder.field(Fields.RECOVERED_SIZE_IN_BYTES, peerRecoveryStatus.recoveredIndexSize().bytes());
						builder.endObject();

						builder.startObject(Fields.TRANSLOG);
						builder.field(Fields.RECOVERED, peerRecoveryStatus.recoveredTranslogOperations());
						builder.endObject();

						builder.endObject();
					}

					if (shardStatus.gatewayRecoveryStatus() != null) {
						GatewayRecoveryStatus gatewayRecoveryStatus = shardStatus.gatewayRecoveryStatus();
						builder.startObject(Fields.GATEWAY_RECOVERY);
						builder.field(Fields.STAGE, gatewayRecoveryStatus.stage());
						builder.field(Fields.START_TIME_IN_MILLIS, gatewayRecoveryStatus.startTime());
						builder.field(Fields.TIME, gatewayRecoveryStatus.time());
						builder.field(Fields.TIME_IN_MILLIS, gatewayRecoveryStatus.time().millis());

						builder.startObject(Fields.INDEX);
						builder.field(Fields.PROGRESS, gatewayRecoveryStatus.indexRecoveryProgress());
						builder.field(Fields.SIZE, gatewayRecoveryStatus.indexSize());
						builder.field(Fields.SIZE, gatewayRecoveryStatus.indexSize().bytes());
						builder.field(Fields.REUSED_SIZE, gatewayRecoveryStatus.reusedIndexSize());
						builder.field(Fields.REUSED_SIZE_IN_BYTES, gatewayRecoveryStatus.reusedIndexSize().bytes());
						builder.field(Fields.EXPECTED_RECOVERED_SIZE,
								gatewayRecoveryStatus.expectedRecoveredIndexSize());
						builder.field(Fields.EXPECTED_RECOVERED_SIZE_IN_BYTES, gatewayRecoveryStatus
								.expectedRecoveredIndexSize().bytes());
						builder.field(Fields.RECOVERED_SIZE, gatewayRecoveryStatus.recoveredIndexSize());
						builder.field(Fields.RECOVERED_SIZE_IN_BYTES, gatewayRecoveryStatus.recoveredIndexSize()
								.bytes());
						builder.endObject();

						builder.startObject(Fields.TRANSLOG);
						builder.field(Fields.RECOVERED, gatewayRecoveryStatus.recoveredTranslogOperations());
						builder.endObject();

						builder.endObject();
					}

					if (shardStatus.gatewaySnapshotStatus() != null) {
						GatewaySnapshotStatus gatewaySnapshotStatus = shardStatus.gatewaySnapshotStatus();
						builder.startObject(Fields.GATEWAY_SNAPSHOT);
						builder.field(Fields.STAGE, gatewaySnapshotStatus.stage());
						builder.field(Fields.START_TIME_IN_MILLIS, gatewaySnapshotStatus.startTime());
						builder.field(Fields.TIME, gatewaySnapshotStatus.time());
						builder.field(Fields.TIME_IN_MILLIS, gatewaySnapshotStatus.time().millis());

						builder.startObject(Fields.INDEX);
						builder.field(Fields.SIZE, gatewaySnapshotStatus.indexSize());
						builder.field(Fields.SIZE_IN_BYTES, gatewaySnapshotStatus.indexSize().bytes());
						builder.endObject();

						builder.startObject(Fields.TRANSLOG);
						builder.field(Fields.EXPECTED_OPERATIONS, gatewaySnapshotStatus.expectedNumberOfOperations());
						builder.endObject();

						builder.endObject();
					}

					builder.endObject();
				}
				builder.endArray();
			}
			builder.endObject();

			builder.endObject();
		}
		builder.endObject();
		return builder;
	}

	
	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		
		/** The Constant INDICES. */
		static final XContentBuilderString INDICES = new XContentBuilderString("indices");

		
		/** The Constant INDEX. */
		static final XContentBuilderString INDEX = new XContentBuilderString("index");

		
		/** The Constant PRIMARY_SIZE. */
		static final XContentBuilderString PRIMARY_SIZE = new XContentBuilderString("primary_size");

		
		/** The Constant PRIMARY_SIZE_IN_BYTES. */
		static final XContentBuilderString PRIMARY_SIZE_IN_BYTES = new XContentBuilderString("primary_size_in_bytes");

		
		/** The Constant SIZE. */
		static final XContentBuilderString SIZE = new XContentBuilderString("size");

		
		/** The Constant SIZE_IN_BYTES. */
		static final XContentBuilderString SIZE_IN_BYTES = new XContentBuilderString("size_in_bytes");

		
		/** The Constant TRANSLOG. */
		static final XContentBuilderString TRANSLOG = new XContentBuilderString("translog");

		
		/** The Constant OPERATIONS. */
		static final XContentBuilderString OPERATIONS = new XContentBuilderString("operations");

		
		/** The Constant DOCS. */
		static final XContentBuilderString DOCS = new XContentBuilderString("docs");

		
		/** The Constant NUM_DOCS. */
		static final XContentBuilderString NUM_DOCS = new XContentBuilderString("num_docs");

		
		/** The Constant MAX_DOC. */
		static final XContentBuilderString MAX_DOC = new XContentBuilderString("max_doc");

		
		/** The Constant DELETED_DOCS. */
		static final XContentBuilderString DELETED_DOCS = new XContentBuilderString("deleted_docs");

		
		/** The Constant SHARDS. */
		static final XContentBuilderString SHARDS = new XContentBuilderString("shards");

		
		/** The Constant ROUTING. */
		static final XContentBuilderString ROUTING = new XContentBuilderString("routing");

		
		/** The Constant STATE. */
		static final XContentBuilderString STATE = new XContentBuilderString("state");

		
		/** The Constant PRIMARY. */
		static final XContentBuilderString PRIMARY = new XContentBuilderString("primary");

		
		/** The Constant NODE. */
		static final XContentBuilderString NODE = new XContentBuilderString("node");

		
		/** The Constant RELOCATING_NODE. */
		static final XContentBuilderString RELOCATING_NODE = new XContentBuilderString("relocating_node");

		
		/** The Constant SHARD. */
		static final XContentBuilderString SHARD = new XContentBuilderString("shard");

		
		/** The Constant ID. */
		static final XContentBuilderString ID = new XContentBuilderString("id");

		
		/** The Constant PEER_RECOVERY. */
		static final XContentBuilderString PEER_RECOVERY = new XContentBuilderString("peer_recovery");

		
		/** The Constant STAGE. */
		static final XContentBuilderString STAGE = new XContentBuilderString("stage");

		
		/** The Constant START_TIME_IN_MILLIS. */
		static final XContentBuilderString START_TIME_IN_MILLIS = new XContentBuilderString("start_time_in_millis");

		
		/** The Constant TIME. */
		static final XContentBuilderString TIME = new XContentBuilderString("time");

		
		/** The Constant TIME_IN_MILLIS. */
		static final XContentBuilderString TIME_IN_MILLIS = new XContentBuilderString("time_in_millis");

		
		/** The Constant PROGRESS. */
		static final XContentBuilderString PROGRESS = new XContentBuilderString("progress");

		
		/** The Constant REUSED_SIZE. */
		static final XContentBuilderString REUSED_SIZE = new XContentBuilderString("reused_size");

		
		/** The Constant REUSED_SIZE_IN_BYTES. */
		static final XContentBuilderString REUSED_SIZE_IN_BYTES = new XContentBuilderString("reused_size_in_bytes");

		
		/** The Constant EXPECTED_RECOVERED_SIZE. */
		static final XContentBuilderString EXPECTED_RECOVERED_SIZE = new XContentBuilderString(
				"expected_recovered_size");

		
		/** The Constant EXPECTED_RECOVERED_SIZE_IN_BYTES. */
		static final XContentBuilderString EXPECTED_RECOVERED_SIZE_IN_BYTES = new XContentBuilderString(
				"expected_recovered_size_in_bytes");

		
		/** The Constant RECOVERED_SIZE. */
		static final XContentBuilderString RECOVERED_SIZE = new XContentBuilderString("recovered_size");

		
		/** The Constant RECOVERED_SIZE_IN_BYTES. */
		static final XContentBuilderString RECOVERED_SIZE_IN_BYTES = new XContentBuilderString(
				"recovered_size_in_bytes");

		
		/** The Constant RECOVERED. */
		static final XContentBuilderString RECOVERED = new XContentBuilderString("recovered");

		
		/** The Constant GATEWAY_RECOVERY. */
		static final XContentBuilderString GATEWAY_RECOVERY = new XContentBuilderString("gateway_recovery");

		
		/** The Constant GATEWAY_SNAPSHOT. */
		static final XContentBuilderString GATEWAY_SNAPSHOT = new XContentBuilderString("gateway_snapshot");

		
		/** The Constant EXPECTED_OPERATIONS. */
		static final XContentBuilderString EXPECTED_OPERATIONS = new XContentBuilderString("expected_operations");
	}
}
