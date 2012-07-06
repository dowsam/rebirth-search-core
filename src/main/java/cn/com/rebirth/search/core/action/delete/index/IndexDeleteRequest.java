/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexDeleteRequest.java 2012-3-29 15:01:01 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.delete.index;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.support.replication.IndexReplicationOperationRequest;


/**
 * The Class IndexDeleteRequest.
 *
 * @author l.xue.nong
 */
public class IndexDeleteRequest extends IndexReplicationOperationRequest {

	
	/** The type. */
	private String type;

	
	/** The id. */
	private String id;

	
	/** The refresh. */
	private boolean refresh = false;

	
	/** The version. */
	private long version;

	
	/**
	 * Instantiates a new index delete request.
	 */
	IndexDeleteRequest() {
	}

	
	/**
	 * Instantiates a new index delete request.
	 *
	 * @param request the request
	 */
	public IndexDeleteRequest(DeleteRequest request) {
		this.timeout = request.timeout();
		this.consistencyLevel = request.consistencyLevel();
		this.replicationType = request.replicationType();
		this.index = request.index();
		this.type = request.type();
		this.id = request.id();
		this.refresh = request.refresh();
		this.version = request.version();
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
	 * @return the long
	 */
	public long version() {
		return this.version;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		type = in.readUTF();
		id = in.readUTF();
		refresh = in.readBoolean();
		version = in.readLong();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeUTF(type);
		out.writeUTF(id);
		out.writeBoolean(refresh);
		out.writeLong(version);
	}
}
