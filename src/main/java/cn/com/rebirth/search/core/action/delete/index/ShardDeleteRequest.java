/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardDeleteRequest.java 2012-3-29 15:00:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.delete.index;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest;


/**
 * The Class ShardDeleteRequest.
 *
 * @author l.xue.nong
 */
public class ShardDeleteRequest extends ShardReplicationOperationRequest {

	
	/** The shard id. */
	private int shardId;

	
	/** The type. */
	private String type;

	
	/** The id. */
	private String id;

	
	/** The refresh. */
	private boolean refresh = false;

	
	/** The version. */
	private long version;

	
	/**
	 * Instantiates a new shard delete request.
	 *
	 * @param request the request
	 * @param shardId the shard id
	 */
	ShardDeleteRequest(IndexDeleteRequest request, int shardId) {
		this.index = request.index();
		this.shardId = shardId;
		this.type = request.type();
		this.id = request.id();
		replicationType(request.replicationType());
		consistencyLevel(request.consistencyLevel());
		timeout = request.timeout();
		this.refresh = request.refresh();
		this.version = request.version();
	}

	
	/**
	 * Instantiates a new shard delete request.
	 */
	ShardDeleteRequest() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		if (type == null) {
			ValidateActions.addValidationError("type is missing", validationException);
		}
		if (id == null) {
			ValidateActions.addValidationError("id is missing", validationException);
		}
		return validationException;
	}

	
	/**
	 * Shard id.
	 *
	 * @return the int
	 */
	public int shardId() {
		return this.shardId;
	}

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return this.type;
	}

	
	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return this.id;
	}

	
	/**
	 * Refresh.
	 *
	 * @return true, if successful
	 */
	public boolean refresh() {
		return this.refresh;
	}

	
	/**
	 * Version.
	 *
	 * @param version the version
	 */
	public void version(long version) {
		this.version = version;
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
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		shardId = in.readVInt();
		type = in.readUTF();
		id = in.readUTF();
		refresh = in.readBoolean();
		version = in.readLong();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(shardId);
		out.writeUTF(type);
		out.writeUTF(id);
		out.writeBoolean(refresh);
		out.writeLong(version);
	}
}