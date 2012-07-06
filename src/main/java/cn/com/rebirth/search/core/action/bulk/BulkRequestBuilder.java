/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BulkRequestBuilder.java 2012-3-29 15:01:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.bulk;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.delete.DeleteRequestBuilder;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.index.IndexRequestBuilder;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Client;


/**
 * The Class BulkRequestBuilder.
 *
 * @author l.xue.nong
 */
public class BulkRequestBuilder extends BaseRequestBuilder<BulkRequest, BulkResponse> {

	
	/**
	 * Instantiates a new bulk request builder.
	 *
	 * @param client the client
	 */
	public BulkRequestBuilder(Client client) {
		super(client, new BulkRequest());
	}

	
	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the bulk request builder
	 */
	public BulkRequestBuilder add(IndexRequest request) {
		super.request.add(request);
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the bulk request builder
	 */
	public BulkRequestBuilder add(IndexRequestBuilder request) {
		super.request.add(request.request());
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the bulk request builder
	 */
	public BulkRequestBuilder add(DeleteRequest request) {
		super.request.add(request);
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the bulk request builder
	 */
	public BulkRequestBuilder add(DeleteRequestBuilder request) {
		super.request.add(request.request());
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param data the data
	 * @param from the from
	 * @param length the length
	 * @param contentUnsafe the content unsafe
	 * @return the bulk request builder
	 * @throws Exception the exception
	 */
	public BulkRequestBuilder add(byte[] data, int from, int length, boolean contentUnsafe) throws Exception {
		request.add(data, from, length, contentUnsafe, null, null);
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param data the data
	 * @param from the from
	 * @param length the length
	 * @param contentUnsafe the content unsafe
	 * @param defaultIndex the default index
	 * @param defaultType the default type
	 * @return the bulk request builder
	 * @throws Exception the exception
	 */
	public BulkRequestBuilder add(byte[] data, int from, int length, boolean contentUnsafe,
			@Nullable String defaultIndex, @Nullable String defaultType) throws Exception {
		request.add(data, from, length, contentUnsafe, defaultIndex, defaultType);
		return this;
	}

	
	/**
	 * Sets the replication type.
	 *
	 * @param replicationType the replication type
	 * @return the bulk request builder
	 */
	public BulkRequestBuilder setReplicationType(ReplicationType replicationType) {
		request.replicationType(replicationType);
		return this;
	}

	
	/**
	 * Sets the consistency level.
	 *
	 * @param consistencyLevel the consistency level
	 * @return the bulk request builder
	 */
	public BulkRequestBuilder setConsistencyLevel(WriteConsistencyLevel consistencyLevel) {
		request.consistencyLevel(consistencyLevel);
		return this;
	}

	
	/**
	 * Sets the refresh.
	 *
	 * @param refresh the refresh
	 * @return the bulk request builder
	 */
	public BulkRequestBuilder setRefresh(boolean refresh) {
		request.refresh(refresh);
		return this;
	}

	
	/**
	 * Number of actions.
	 *
	 * @return the int
	 */
	public int numberOfActions() {
		return request.numberOfActions();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<BulkResponse> listener) {
		client.bulk(request, listener);
	}
}
