/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesAliasesRequestBuilder.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.alias;

import java.util.Map;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;
import cn.com.rebirth.search.core.cluster.metadata.AliasAction;
import cn.com.rebirth.search.core.index.query.FilterBuilder;

/**
 * The Class IndicesAliasesRequestBuilder.
 *
 * @author l.xue.nong
 */
public class IndicesAliasesRequestBuilder extends
		BaseIndicesRequestBuilder<IndicesAliasesRequest, IndicesAliasesResponse> {

	/**
	 * Instantiates a new indices aliases request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public IndicesAliasesRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new IndicesAliasesRequest());
	}

	/**
	 * Adds the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @return the indices aliases request builder
	 */
	public IndicesAliasesRequestBuilder addAlias(String index, String alias) {
		request.addAlias(index, alias);
		return this;
	}

	/**
	 * Adds the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @param filter the filter
	 * @return the indices aliases request builder
	 */
	public IndicesAliasesRequestBuilder addAlias(String index, String alias, String filter) {
		request.addAlias(index, alias, filter);
		return this;
	}

	/**
	 * Adds the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @param filter the filter
	 * @return the indices aliases request builder
	 */
	public IndicesAliasesRequestBuilder addAlias(String index, String alias, Map<String, Object> filter) {
		request.addAlias(index, alias, filter);
		return this;
	}

	/**
	 * Adds the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @param filterBuilder the filter builder
	 * @return the indices aliases request builder
	 */
	public IndicesAliasesRequestBuilder addAlias(String index, String alias, FilterBuilder filterBuilder) {
		request.addAlias(index, alias, filterBuilder);
		return this;
	}

	/**
	 * Adds the alias action.
	 *
	 * @param aliasAction the alias action
	 * @return the indices aliases request builder
	 */
	public IndicesAliasesRequestBuilder addAliasAction(AliasAction aliasAction) {
		request.addAliasAction(aliasAction);
		return this;
	}

	/**
	 * Removes the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @return the indices aliases request builder
	 */
	public IndicesAliasesRequestBuilder removeAlias(String index, String alias) {
		request.removeAlias(index, alias);
		return this;
	}

	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the indices aliases request builder
	 */
	public IndicesAliasesRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the indices aliases request builder
	 */
	public IndicesAliasesRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<IndicesAliasesResponse> listener) {
		client.aliases(request, listener);
	}
}
