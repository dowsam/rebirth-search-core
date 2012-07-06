/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesStatsRequestBuilder.java 2012-3-29 15:01:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.stats;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class IndicesStatsRequestBuilder.
 *
 * @author l.xue.nong
 */
public class IndicesStatsRequestBuilder extends BaseIndicesRequestBuilder<IndicesStatsRequest, IndicesStats> {

	
	/**
	 * Instantiates a new indices stats request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public IndicesStatsRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new IndicesStatsRequest());
	}

	
	/**
	 * All.
	 *
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder all() {
		request.all();
		return this;
	}

	
	/**
	 * Clear.
	 *
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder clear() {
		request.clear();
		return this;
	}

	
	/**
	 * Sets the indices.
	 *
	 * @param indices the indices
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	
	/**
	 * Sets the types.
	 *
	 * @param types the types
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setTypes(String... types) {
		request.types(types);
		return this;
	}

	
	/**
	 * Sets the groups.
	 *
	 * @param groups the groups
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setGroups(String... groups) {
		request.groups(groups);
		return this;
	}

	
	/**
	 * Sets the docs.
	 *
	 * @param docs the docs
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setDocs(boolean docs) {
		request.docs(docs);
		return this;
	}

	
	/**
	 * Sets the store.
	 *
	 * @param store the store
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setStore(boolean store) {
		request.store(store);
		return this;
	}

	
	/**
	 * Sets the indexing.
	 *
	 * @param indexing the indexing
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setIndexing(boolean indexing) {
		request.indexing(indexing);
		return this;
	}

	
	/**
	 * Sets the get.
	 *
	 * @param get the get
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setGet(boolean get) {
		request.get(get);
		return this;
	}

	
	/**
	 * Sets the search.
	 *
	 * @param search the search
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setSearch(boolean search) {
		request.search(search);
		return this;
	}

	
	/**
	 * Sets the merge.
	 *
	 * @param merge the merge
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setMerge(boolean merge) {
		request.merge(merge);
		return this;
	}

	
	/**
	 * Sets the refresh.
	 *
	 * @param refresh the refresh
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setRefresh(boolean refresh) {
		request.refresh(refresh);
		return this;
	}

	
	/**
	 * Sets the flush.
	 *
	 * @param flush the flush
	 * @return the indices stats request builder
	 */
	public IndicesStatsRequestBuilder setFlush(boolean flush) {
		request.flush(flush);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<IndicesStats> listener) {
		client.stats(request, listener);
	}
}
