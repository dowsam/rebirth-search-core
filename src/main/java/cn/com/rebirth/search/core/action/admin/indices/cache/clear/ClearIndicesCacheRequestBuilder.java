/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClearIndicesCacheRequestBuilder.java 2012-3-29 15:01:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.cache.clear;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class ClearIndicesCacheRequestBuilder.
 *
 * @author l.xue.nong
 */
public class ClearIndicesCacheRequestBuilder extends
		BaseIndicesRequestBuilder<ClearIndicesCacheRequest, ClearIndicesCacheResponse> {

	
	/**
	 * Instantiates a new clear indices cache request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public ClearIndicesCacheRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new ClearIndicesCacheRequest());
	}

	
	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the clear indices cache request builder
	 */
	public ClearIndicesCacheRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	
	/**
	 * Sets the filter cache.
	 *
	 * @param filterCache the filter cache
	 * @return the clear indices cache request builder
	 */
	public ClearIndicesCacheRequestBuilder setFilterCache(boolean filterCache) {
		request.filterCache(filterCache);
		return this;
	}

	
	/**
	 * Sets the field data cache.
	 *
	 * @param fieldDataCache the field data cache
	 * @return the clear indices cache request builder
	 */
	public ClearIndicesCacheRequestBuilder setFieldDataCache(boolean fieldDataCache) {
		request.fieldDataCache(fieldDataCache);
		return this;
	}

	
	/**
	 * Sets the fields.
	 *
	 * @param fields the fields
	 * @return the clear indices cache request builder
	 */
	public ClearIndicesCacheRequestBuilder setFields(String... fields) {
		request.fields(fields);
		return this;
	}

	
	/**
	 * Sets the id cache.
	 *
	 * @param idCache the id cache
	 * @return the clear indices cache request builder
	 */
	public ClearIndicesCacheRequestBuilder setIdCache(boolean idCache) {
		request.idCache(idCache);
		return this;
	}

	
	/**
	 * Sets the bloom cache.
	 *
	 * @param bloomCache the bloom cache
	 * @return the clear indices cache request builder
	 */
	public ClearIndicesCacheRequestBuilder setBloomCache(boolean bloomCache) {
		request.bloomCache(bloomCache);
		return this;
	}

	
	/**
	 * Sets the listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the clear indices cache request builder
	 */
	public ClearIndicesCacheRequestBuilder setListenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	
	/**
	 * Sets the operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the clear indices cache request builder
	 */
	public ClearIndicesCacheRequestBuilder setOperationThreading(BroadcastOperationThreading operationThreading) {
		request.operationThreading(operationThreading);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<ClearIndicesCacheResponse> listener) {
		client.clearCache(request, listener);
	}
}
