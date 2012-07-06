/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardStatus.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.status;

import static cn.com.rebirth.search.core.cluster.routing.ImmutableShardRouting.readShardRoutingEntry;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.flush.FlushStats;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.refresh.RefreshStats;
import cn.com.rebirth.search.core.index.shard.IndexShardState;

/**
 * The Class ShardStatus.
 *
 * @author l.xue.nong
 */
public class ShardStatus extends BroadcastShardOperationResponse {

	/** The shard routing. */
	private ShardRouting shardRouting;

	/** The state. */
	IndexShardState state;

	/** The store size. */
	ByteSizeValue storeSize;

	/** The translog id. */
	long translogId = -1;

	/** The translog operations. */
	long translogOperations = -1;

	/** The docs. */
	DocsStatus docs;

	/** The merge stats. */
	MergeStats mergeStats;

	/** The refresh stats. */
	RefreshStats refreshStats;

	/** The flush stats. */
	FlushStats flushStats;

	/** The peer recovery status. */
	PeerRecoveryStatus peerRecoveryStatus;

	/** The gateway recovery status. */
	GatewayRecoveryStatus gatewayRecoveryStatus;

	/** The gateway snapshot status. */
	GatewaySnapshotStatus gatewaySnapshotStatus;

	/**
	 * Instantiates a new shard status.
	 */
	ShardStatus() {
	}

	/**
	 * Instantiates a new shard status.
	 *
	 * @param shardRouting the shard routing
	 */
	ShardStatus(ShardRouting shardRouting) {
		super(shardRouting.index(), shardRouting.id());
		this.shardRouting = shardRouting;
	}

	/**
	 * Shard routing.
	 *
	 * @return the shard routing
	 */
	public ShardRouting shardRouting() {
		return this.shardRouting;
	}

	/**
	 * Gets the shard routing.
	 *
	 * @return the shard routing
	 */
	public ShardRouting getShardRouting() {
		return shardRouting();
	}

	/**
	 * State.
	 *
	 * @return the index shard state
	 */
	public IndexShardState state() {
		return state;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public IndexShardState getState() {
		return state();
	}

	/**
	 * Store size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue storeSize() {
		return storeSize;
	}

	/**
	 * Gets the store size.
	 *
	 * @return the store size
	 */
	public ByteSizeValue getStoreSize() {
		return storeSize();
	}

	/**
	 * Translog id.
	 *
	 * @return the long
	 */
	public long translogId() {
		return translogId;
	}

	/**
	 * Gets the translog id.
	 *
	 * @return the translog id
	 */
	public long getTranslogId() {
		return translogId();
	}

	/**
	 * Translog operations.
	 *
	 * @return the long
	 */
	public long translogOperations() {
		return translogOperations;
	}

	/**
	 * Gets the translog operations.
	 *
	 * @return the translog operations
	 */
	public long getTranslogOperations() {
		return translogOperations();
	}

	/**
	 * Docs.
	 *
	 * @return the docs status
	 */
	public DocsStatus docs() {
		return docs;
	}

	/**
	 * Gets the docs.
	 *
	 * @return the docs
	 */
	public DocsStatus getDocs() {
		return docs();
	}

	/**
	 * Merge stats.
	 *
	 * @return the merge stats
	 */
	public MergeStats mergeStats() {
		return this.mergeStats;
	}

	/**
	 * Gets the merge stats.
	 *
	 * @return the merge stats
	 */
	public MergeStats getMergeStats() {
		return this.mergeStats;
	}

	/**
	 * Refresh stats.
	 *
	 * @return the refresh stats
	 */
	public RefreshStats refreshStats() {
		return this.refreshStats;
	}

	/**
	 * Gets the refresh stats.
	 *
	 * @return the refresh stats
	 */
	public RefreshStats getRefreshStats() {
		return refreshStats();
	}

	/**
	 * Flush stats.
	 *
	 * @return the flush stats
	 */
	public FlushStats flushStats() {
		return this.flushStats;
	}

	/**
	 * Gets the flush stats.
	 *
	 * @return the flush stats
	 */
	public FlushStats getFlushStats() {
		return this.flushStats;
	}

	/**
	 * Peer recovery status.
	 *
	 * @return the peer recovery status
	 */
	public PeerRecoveryStatus peerRecoveryStatus() {
		return peerRecoveryStatus;
	}

	/**
	 * Gets the peer recovery status.
	 *
	 * @return the peer recovery status
	 */
	public PeerRecoveryStatus getPeerRecoveryStatus() {
		return peerRecoveryStatus();
	}

	/**
	 * Gateway recovery status.
	 *
	 * @return the gateway recovery status
	 */
	public GatewayRecoveryStatus gatewayRecoveryStatus() {
		return gatewayRecoveryStatus;
	}

	/**
	 * Gets the gateway recovery status.
	 *
	 * @return the gateway recovery status
	 */
	public GatewayRecoveryStatus getGatewayRecoveryStatus() {
		return gatewayRecoveryStatus();
	}

	/**
	 * Gateway snapshot status.
	 *
	 * @return the gateway snapshot status
	 */
	public GatewaySnapshotStatus gatewaySnapshotStatus() {
		return gatewaySnapshotStatus;
	}

	/**
	 * Gets the gateway snapshot status.
	 *
	 * @return the gateway snapshot status
	 */
	public GatewaySnapshotStatus getGatewaySnapshotStatus() {
		return gatewaySnapshotStatus();
	}

	/**
	 * Read index shard status.
	 *
	 * @param in the in
	 * @return the shard status
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ShardStatus readIndexShardStatus(StreamInput in) throws IOException {
		ShardStatus shardStatus = new ShardStatus();
		shardStatus.readFrom(in);
		return shardStatus;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		shardRouting.writeTo(out);
		out.writeByte(state.id());
		if (storeSize == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			storeSize.writeTo(out);
		}
		out.writeLong(translogId);
		out.writeLong(translogOperations);
		if (docs == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeLong(docs.numDocs());
			out.writeLong(docs.maxDoc());
			out.writeLong(docs.deletedDocs());
		}
		if (peerRecoveryStatus == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeByte(peerRecoveryStatus.stage.value());
			out.writeVLong(peerRecoveryStatus.startTime);
			out.writeVLong(peerRecoveryStatus.time);
			out.writeVLong(peerRecoveryStatus.indexSize);
			out.writeVLong(peerRecoveryStatus.reusedIndexSize);
			out.writeVLong(peerRecoveryStatus.recoveredIndexSize);
			out.writeVLong(peerRecoveryStatus.recoveredTranslogOperations);
		}

		if (gatewayRecoveryStatus == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeByte(gatewayRecoveryStatus.stage.value());
			out.writeVLong(gatewayRecoveryStatus.startTime);
			out.writeVLong(gatewayRecoveryStatus.time);
			out.writeVLong(gatewayRecoveryStatus.indexSize);
			out.writeVLong(gatewayRecoveryStatus.reusedIndexSize);
			out.writeVLong(gatewayRecoveryStatus.recoveredIndexSize);
			out.writeVLong(gatewayRecoveryStatus.recoveredTranslogOperations);
		}

		if (gatewaySnapshotStatus == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeByte(gatewaySnapshotStatus.stage.value());
			out.writeVLong(gatewaySnapshotStatus.startTime);
			out.writeVLong(gatewaySnapshotStatus.time);
			out.writeVLong(gatewaySnapshotStatus.indexSize);
			out.writeVInt(gatewaySnapshotStatus.expectedNumberOfOperations());
		}

		if (mergeStats == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			mergeStats.writeTo(out);
		}
		if (refreshStats == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			refreshStats.writeTo(out);
		}
		if (flushStats == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			flushStats.writeTo(out);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		shardRouting = readShardRoutingEntry(in);
		state = IndexShardState.fromId(in.readByte());
		if (in.readBoolean()) {
			storeSize = ByteSizeValue.readBytesSizeValue(in);
		}
		translogId = in.readLong();
		translogOperations = in.readLong();
		if (in.readBoolean()) {
			docs = new DocsStatus();
			docs.numDocs = in.readLong();
			docs.maxDoc = in.readLong();
			docs.deletedDocs = in.readLong();
		}
		if (in.readBoolean()) {
			peerRecoveryStatus = new PeerRecoveryStatus(PeerRecoveryStatus.Stage.fromValue(in.readByte()),
					in.readVLong(), in.readVLong(), in.readVLong(), in.readVLong(), in.readVLong(), in.readVLong());
		}

		if (in.readBoolean()) {
			gatewayRecoveryStatus = new GatewayRecoveryStatus(GatewayRecoveryStatus.Stage.fromValue(in.readByte()),
					in.readVLong(), in.readVLong(), in.readVLong(), in.readVLong(), in.readVLong(), in.readVLong());
		}

		if (in.readBoolean()) {
			gatewaySnapshotStatus = new GatewaySnapshotStatus(GatewaySnapshotStatus.Stage.fromValue(in.readByte()),
					in.readVLong(), in.readVLong(), in.readVLong(), in.readVInt());
		}

		if (in.readBoolean()) {
			mergeStats = MergeStats.readMergeStats(in);
		}
		if (in.readBoolean()) {
			refreshStats = RefreshStats.readRefreshStats(in);
		}
		if (in.readBoolean()) {
			flushStats = FlushStats.readFlushStats(in);
		}
	}
}
