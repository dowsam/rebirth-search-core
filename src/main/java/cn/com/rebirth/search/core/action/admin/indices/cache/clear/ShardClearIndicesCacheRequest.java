/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardClearIndicesCacheRequest.java 2012-3-29 15:02:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.cache.clear;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest;


/**
 * The Class ShardClearIndicesCacheRequest.
 *
 * @author l.xue.nong
 */
class ShardClearIndicesCacheRequest extends BroadcastShardOperationRequest {

	
	/** The filter cache. */
	private boolean filterCache = false;

	
	/** The field data cache. */
	private boolean fieldDataCache = false;

	
	/** The id cache. */
	private boolean idCache = false;

	
	/** The bloom cache. */
	private boolean bloomCache = false;

	
	/** The fields. */
	private String[] fields = null;

	
	/**
	 * Instantiates a new shard clear indices cache request.
	 */
	ShardClearIndicesCacheRequest() {
	}

	
	/**
	 * Instantiates a new shard clear indices cache request.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param request the request
	 */
	public ShardClearIndicesCacheRequest(String index, int shardId, ClearIndicesCacheRequest request) {
		super(index, shardId);
		filterCache = request.filterCache();
		fieldDataCache = request.fieldDataCache();
		idCache = request.idCache();
		bloomCache = request.bloomCache();
		fields = request.fields();
	}

	
	/**
	 * Filter cache.
	 *
	 * @return true, if successful
	 */
	public boolean filterCache() {
		return filterCache;
	}

	
	/**
	 * Field data cache.
	 *
	 * @return true, if successful
	 */
	public boolean fieldDataCache() {
		return this.fieldDataCache;
	}

	
	/**
	 * Id cache.
	 *
	 * @return true, if successful
	 */
	public boolean idCache() {
		return this.idCache;
	}

	
	/**
	 * Bloom cache.
	 *
	 * @return true, if successful
	 */
	public boolean bloomCache() {
		return this.bloomCache;
	}

	
	/**
	 * Fields.
	 *
	 * @return the string[]
	 */
	public String[] fields() {
		return this.fields;
	}

	
	/**
	 * Wait for operations.
	 *
	 * @param waitForOperations the wait for operations
	 * @return the shard clear indices cache request
	 */
	public ShardClearIndicesCacheRequest waitForOperations(boolean waitForOperations) {
		this.filterCache = waitForOperations;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		filterCache = in.readBoolean();
		fieldDataCache = in.readBoolean();
		idCache = in.readBoolean();
		bloomCache = in.readBoolean();
		int size = in.readVInt();
		if (size > 0) {
			fields = new String[size];
			for (int i = 0; i < size; i++) {
				fields[i] = in.readUTF();
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(filterCache);
		out.writeBoolean(fieldDataCache);
		out.writeBoolean(idCache);
		out.writeBoolean(bloomCache);
		if (fields == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(fields.length);
			for (String field : fields) {
				out.writeUTF(field);
			}
		}
	}
}