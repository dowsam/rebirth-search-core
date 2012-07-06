/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardSearchFailure.java 2012-3-29 15:01:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search;

import java.io.IOException;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.search.SearchException;
import cn.com.rebirth.search.core.search.SearchShardTarget;


/**
 * The Class ShardSearchFailure.
 *
 * @author l.xue.nong
 */
public class ShardSearchFailure implements ShardOperationFailedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2726939437618729061L;

	
	/** The Constant EMPTY_ARRAY. */
	public static final ShardSearchFailure[] EMPTY_ARRAY = new ShardSearchFailure[0];

	
	/** The shard target. */
	private SearchShardTarget shardTarget;

	
	/** The reason. */
	private String reason;

	
	/** The status. */
	private RestStatus status;

	
	/**
	 * Instantiates a new shard search failure.
	 */
	private ShardSearchFailure() {

	}

	
	/**
	 * Instantiates a new shard search failure.
	 *
	 * @param t the t
	 */
	public ShardSearchFailure(Throwable t) {
		Throwable actual = ExceptionsHelper.unwrapCause(t);
		if (actual != null && actual instanceof SearchException) {
			this.shardTarget = ((SearchException) actual).shard();
		}
		status = RestStatus.INTERNAL_SERVER_ERROR;
		this.reason = ExceptionsHelper.detailedMessage(t);
	}

	
	/**
	 * Instantiates a new shard search failure.
	 *
	 * @param reason the reason
	 * @param shardTarget the shard target
	 */
	public ShardSearchFailure(String reason, SearchShardTarget shardTarget) {
		this.shardTarget = shardTarget;
		this.reason = reason;
		this.status = RestStatus.INTERNAL_SERVER_ERROR;
	}

	
	/**
	 * Shard.
	 *
	 * @return the search shard target
	 */
	@Nullable
	public SearchShardTarget shard() {
		return this.shardTarget;
	}

	
	/**
	 * Status.
	 *
	 * @return the rest status
	 */
	public RestStatus status() {
		return this.status;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ShardOperationFailedException#index()
	 */
	@Override
	public String index() {
		if (shardTarget != null) {
			return shardTarget.index();
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ShardOperationFailedException#shardId()
	 */
	@Override
	public int shardId() {
		if (shardTarget != null) {
			return shardTarget.shardId();
		}
		return -1;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ShardOperationFailedException#reason()
	 */
	public String reason() {
		return this.reason;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "shard [" + (shardTarget == null ? "_na" : shardTarget) + "], reason [" + reason + "]";
	}

	
	/**
	 * Read shard search failure.
	 *
	 * @param in the in
	 * @return the shard search failure
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ShardSearchFailure readShardSearchFailure(StreamInput in) throws IOException {
		ShardSearchFailure shardSearchFailure = new ShardSearchFailure();
		shardSearchFailure.readFrom(in);
		return shardSearchFailure;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		if (in.readBoolean()) {
			shardTarget = SearchShardTarget.readSearchShardTarget(in);
		}
		reason = in.readUTF();
		status = RestStatus.readFrom(in);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		if (shardTarget == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			shardTarget.writeTo(out);
		}
		out.writeUTF(reason);
		RestStatus.writeTo(out, status);
	}
}
