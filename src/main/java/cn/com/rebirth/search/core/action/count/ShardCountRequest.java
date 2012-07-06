/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardCountRequest.java 2012-3-29 15:02:13 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.count;

import java.io.IOException;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest;


/**
 * The Class ShardCountRequest.
 *
 * @author l.xue.nong
 */
class ShardCountRequest extends BroadcastShardOperationRequest {

	
	/** The min score. */
	private float minScore;

	
	/** The query source. */
	private byte[] querySource;

	
	/** The query source offset. */
	private int querySourceOffset;

	
	/** The query source length. */
	private int querySourceLength;

	
	/** The types. */
	private String[] types = Strings.EMPTY_ARRAY;

	
	/** The filtering aliases. */
	@Nullable
	private String[] filteringAliases;

	
	/**
	 * Instantiates a new shard count request.
	 */
	ShardCountRequest() {

	}

	
	/**
	 * Instantiates a new shard count request.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param filteringAliases the filtering aliases
	 * @param request the request
	 */
	public ShardCountRequest(String index, int shardId, @Nullable String[] filteringAliases, CountRequest request) {
		super(index, shardId);
		this.minScore = request.minScore();
		this.querySource = request.querySource();
		this.querySourceOffset = request.querySourceOffset();
		this.querySourceLength = request.querySourceLength();
		this.types = request.types();
		this.filteringAliases = filteringAliases;
	}

	
	/**
	 * Min score.
	 *
	 * @return the float
	 */
	public float minScore() {
		return minScore;
	}

	
	/**
	 * Query source.
	 *
	 * @return the byte[]
	 */
	public byte[] querySource() {
		return querySource;
	}

	
	/**
	 * Query source offset.
	 *
	 * @return the int
	 */
	public int querySourceOffset() {
		return querySourceOffset;
	}

	
	/**
	 * Query source length.
	 *
	 * @return the int
	 */
	public int querySourceLength() {
		return querySourceLength;
	}

	
	/**
	 * Types.
	 *
	 * @return the string[]
	 */
	public String[] types() {
		return this.types;
	}

	
	/**
	 * Filtering aliases.
	 *
	 * @return the string[]
	 */
	public String[] filteringAliases() {
		return filteringAliases;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		minScore = in.readFloat();

		BytesHolder bytes = in.readBytesReference();
		querySource = bytes.bytes();
		querySourceOffset = bytes.offset();
		querySourceLength = bytes.length();

		int typesSize = in.readVInt();
		if (typesSize > 0) {
			types = new String[typesSize];
			for (int i = 0; i < typesSize; i++) {
				types[i] = in.readUTF();
			}
		}
		int aliasesSize = in.readVInt();
		if (aliasesSize > 0) {
			filteringAliases = new String[aliasesSize];
			for (int i = 0; i < aliasesSize; i++) {
				filteringAliases[i] = in.readUTF();
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeFloat(minScore);

		out.writeBytesHolder(querySource, querySourceOffset, querySourceLength);

		out.writeVInt(types.length);
		for (String type : types) {
			out.writeUTF(type);
		}
		if (filteringAliases != null) {
			out.writeVInt(filteringAliases.length);
			for (String alias : filteringAliases) {
				out.writeUTF(alias);
			}
		} else {
			out.writeVInt(0);
		}
	}
}
