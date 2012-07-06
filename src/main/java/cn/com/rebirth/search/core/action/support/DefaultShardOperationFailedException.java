/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DefaultShardOperationFailedException.java 2012-7-6 14:29:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support;

import java.io.IOException;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.index.shard.IndexShardException;

/**
 * The Class DefaultShardOperationFailedException.
 *
 * @author l.xue.nong
 */
public class DefaultShardOperationFailedException implements ShardOperationFailedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1998881884554928751L;

	/** The index. */
	private String index;

	/** The shard id. */
	private int shardId;

	/** The reason. */
	private String reason;

	/**
	 * Instantiates a new default shard operation failed exception.
	 */
	private DefaultShardOperationFailedException() {

	}

	/**
	 * Instantiates a new default shard operation failed exception.
	 *
	 * @param e the e
	 */
	public DefaultShardOperationFailedException(IndexShardException e) {
		this.index = e.shardId().index().name();
		this.shardId = e.shardId().id();
		this.reason = ExceptionsHelper.detailedMessage(e);
	}

	/**
	 * Instantiates a new default shard operation failed exception.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param t the t
	 */
	public DefaultShardOperationFailedException(String index, int shardId, Throwable t) {
		this.index = index;
		this.shardId = shardId;
		this.reason = ExceptionsHelper.detailedMessage(t);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ShardOperationFailedException#index()
	 */
	@Override
	public String index() {
		return this.index;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ShardOperationFailedException#shardId()
	 */
	@Override
	public int shardId() {
		return this.shardId;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ShardOperationFailedException#reason()
	 */
	@Override
	public String reason() {
		return this.reason;
	}

	/**
	 * Read shard operation failed.
	 *
	 * @param in the in
	 * @return the default shard operation failed exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static DefaultShardOperationFailedException readShardOperationFailed(StreamInput in) throws IOException {
		DefaultShardOperationFailedException exp = new DefaultShardOperationFailedException();
		exp.readFrom(in);
		return exp;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		if (in.readBoolean()) {
			index = in.readUTF();
		}
		shardId = in.readVInt();
		reason = in.readUTF();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		if (index == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(index);
		}
		out.writeVInt(shardId);
		out.writeUTF(reason);
	}
}
