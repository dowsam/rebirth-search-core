/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeleteRequest.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.delete;

import java.io.IOException;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.Required;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest;
import cn.com.rebirth.search.core.index.VersionType;

/**
 * The Class DeleteRequest.
 *
 * @author l.xue.nong
 */
public class DeleteRequest extends ShardReplicationOperationRequest {

	/** The type. */
	private String type;

	/** The id. */
	private String id;

	/** The routing. */
	@Nullable
	private String routing;

	/** The refresh. */
	private boolean refresh;

	/** The version. */
	private long version;

	/** The version type. */
	private VersionType versionType = VersionType.INTERNAL;

	/**
	 * Instantiates a new delete request.
	 *
	 * @param index the index
	 */
	public DeleteRequest(String index) {
		this.index = index;
	}

	/**
	 * Instantiates a new delete request.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 */
	public DeleteRequest(String index, String type, String id) {
		this.index = index;
		this.type = type;
		this.id = id;
	}

	/**
	 * Instantiates a new delete request.
	 */
	public DeleteRequest() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		if (type == null) {
			validationException = ValidateActions.addValidationError("type is missing", validationException);
		}
		if (id == null) {
			validationException = ValidateActions.addValidationError("id is missing", validationException);
		}
		return validationException;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#index(java.lang.String)
	 */
	@Override
	public DeleteRequest index(String index) {
		super.index(index);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public DeleteRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#operationThreaded(boolean)
	 */
	@Override
	public DeleteRequest operationThreaded(boolean threadedOperation) {
		super.operationThreaded(threadedOperation);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#replicationType(cn.com.rebirth.search.core.action.support.replication.ReplicationType)
	 */
	@Override
	public DeleteRequest replicationType(ReplicationType replicationType) {
		super.replicationType(replicationType);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#consistencyLevel(cn.com.rebirth.search.core.action.WriteConsistencyLevel)
	 */
	@Override
	public DeleteRequest consistencyLevel(WriteConsistencyLevel consistencyLevel) {
		super.consistencyLevel(consistencyLevel);
		return this;
	}

	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return type;
	}

	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the delete request
	 */
	@Required
	public DeleteRequest type(String type) {
		this.type = type;
		return this;
	}

	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return id;
	}

	/**
	 * Id.
	 *
	 * @param id the id
	 * @return the delete request
	 */
	@Required
	public DeleteRequest id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete request
	 */
	public DeleteRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Parent.
	 *
	 * @param parent the parent
	 * @return the delete request
	 */
	public DeleteRequest parent(String parent) {
		if (routing == null) {
			routing = parent;
		}
		return this;
	}

	/**
	 * Routing.
	 *
	 * @param routing the routing
	 * @return the delete request
	 */
	public DeleteRequest routing(String routing) {
		if (routing != null && routing.length() == 0) {
			this.routing = null;
		} else {
			this.routing = routing;
		}
		return this;
	}

	/**
	 * Routing.
	 *
	 * @return the string
	 */
	public String routing() {
		return this.routing;
	}

	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @return the delete request
	 */
	public DeleteRequest refresh(boolean refresh) {
		this.refresh = refresh;
		return this;
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
	 * @return the delete request
	 */
	public DeleteRequest version(long version) {
		this.version = version;
		return this;
	}

	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return this.version;
	}

	/**
	 * Version type.
	 *
	 * @param versionType the version type
	 * @return the delete request
	 */
	public DeleteRequest versionType(VersionType versionType) {
		this.versionType = versionType;
		return this;
	}

	/**
	 * Version type.
	 *
	 * @return the version type
	 */
	public VersionType versionType() {
		return this.versionType;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		type = in.readUTF();
		id = in.readUTF();
		if (in.readBoolean()) {
			routing = in.readUTF();
		}
		refresh = in.readBoolean();
		version = in.readLong();
		versionType = VersionType.fromValue(in.readByte());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeUTF(type);
		out.writeUTF(id);
		if (routing == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(routing);
		}
		out.writeBoolean(refresh);
		out.writeLong(version);
		out.writeByte(versionType.getValue());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "delete {[" + index + "][" + type + "][" + id + "]}";
	}
}