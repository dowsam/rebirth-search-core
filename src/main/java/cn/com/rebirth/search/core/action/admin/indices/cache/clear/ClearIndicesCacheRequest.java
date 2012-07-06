/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClearIndicesCacheRequest.java 2012-3-29 15:01:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.cache.clear;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;


/**
 * The Class ClearIndicesCacheRequest.
 *
 * @author l.xue.nong
 */
public class ClearIndicesCacheRequest extends BroadcastOperationRequest {

	
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
	 * Instantiates a new clear indices cache request.
	 */
	ClearIndicesCacheRequest() {
	}

	
	/**
	 * Instantiates a new clear indices cache request.
	 *
	 * @param indices the indices
	 */
	public ClearIndicesCacheRequest(String... indices) {
		super(indices);
		
		operationThreading(BroadcastOperationThreading.THREAD_PER_SHARD);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public ClearIndicesCacheRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#operationThreading(cn.com.summall.search.core.action.support.broadcast.BroadcastOperationThreading)
	 */
	@Override
	public ClearIndicesCacheRequest operationThreading(BroadcastOperationThreading operationThreading) {
		super.operationThreading(operationThreading);
		return this;
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
	 * Filter cache.
	 *
	 * @param filterCache the filter cache
	 * @return the clear indices cache request
	 */
	public ClearIndicesCacheRequest filterCache(boolean filterCache) {
		this.filterCache = filterCache;
		return this;
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
	 * Field data cache.
	 *
	 * @param fieldDataCache the field data cache
	 * @return the clear indices cache request
	 */
	public ClearIndicesCacheRequest fieldDataCache(boolean fieldDataCache) {
		this.fieldDataCache = fieldDataCache;
		return this;
	}

	
	/**
	 * Fields.
	 *
	 * @param fields the fields
	 * @return the clear indices cache request
	 */
	public ClearIndicesCacheRequest fields(String... fields) {
		this.fields = fields;
		return this;
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
	 * Id cache.
	 *
	 * @return true, if successful
	 */
	public boolean idCache() {
		return this.idCache;
	}

	
	/**
	 * Id cache.
	 *
	 * @param idCache the id cache
	 * @return the clear indices cache request
	 */
	public ClearIndicesCacheRequest idCache(boolean idCache) {
		this.idCache = idCache;
		return this;
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
	 * Bloom cache.
	 *
	 * @param bloomCache the bloom cache
	 * @return the clear indices cache request
	 */
	public ClearIndicesCacheRequest bloomCache(boolean bloomCache) {
		this.bloomCache = bloomCache;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
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
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
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