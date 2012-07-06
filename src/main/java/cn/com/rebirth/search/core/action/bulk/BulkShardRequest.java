/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BulkShardRequest.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.bulk;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest;

/**
 * The Class BulkShardRequest.
 *
 * @author l.xue.nong
 */
public class BulkShardRequest extends ShardReplicationOperationRequest {

	/** The shard id. */
	private int shardId;

	/** The items. */
	private BulkItemRequest[] items;

	/** The refresh. */
	private boolean refresh;

	/**
	 * Instantiates a new bulk shard request.
	 */
	BulkShardRequest() {
	}

	/**
	 * Instantiates a new bulk shard request.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param refresh the refresh
	 * @param items the items
	 */
	BulkShardRequest(String index, int shardId, boolean refresh, BulkItemRequest[] items) {
		this.index = index;
		this.shardId = shardId;
		this.items = items;
		this.refresh = refresh;
	}

	/**
	 * Refresh.
	 *
	 * @return true, if successful
	 */
	boolean refresh() {
		return this.refresh;
	}

	/**
	 * Shard id.
	 *
	 * @return the int
	 */
	int shardId() {
		return shardId;
	}

	/**
	 * Items.
	 *
	 * @return the bulk item request[]
	 */
	BulkItemRequest[] items() {
		return items;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#beforeLocalFork()
	 */
	@Override
	public void beforeLocalFork() {
		for (BulkItemRequest item : items) {
			((ShardReplicationOperationRequest) item.request()).beforeLocalFork();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(shardId);
		out.writeVInt(items.length);
		for (BulkItemRequest item : items) {
			if (item != null) {
				out.writeBoolean(true);
				item.writeTo(out);
			} else {
				out.writeBoolean(false);
			}
		}
		out.writeBoolean(refresh);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		shardId = in.readVInt();
		items = new BulkItemRequest[in.readVInt()];
		for (int i = 0; i < items.length; i++) {
			if (in.readBoolean()) {
				items[i] = BulkItemRequest.readBulkItem(in);
			}
		}
		refresh = in.readBoolean();
	}
}
