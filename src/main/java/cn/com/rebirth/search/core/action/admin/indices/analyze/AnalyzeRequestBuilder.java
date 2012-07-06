/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AnalyzeRequestBuilder.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.analyze;

import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class AnalyzeRequestBuilder.
 *
 * @author l.xue.nong
 */
public class AnalyzeRequestBuilder extends BaseIndicesRequestBuilder<AnalyzeRequest, AnalyzeResponse> {

	/**
	 * Instantiates a new analyze request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public AnalyzeRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new AnalyzeRequest());
	}

	/**
	 * Instantiates a new analyze request builder.
	 *
	 * @param indicesClient the indices client
	 * @param index the index
	 * @param text the text
	 */
	public AnalyzeRequestBuilder(IndicesAdminClient indicesClient, String index, String text) {
		super(indicesClient, new AnalyzeRequest(index, text));
	}

	/**
	 * Sets the index.
	 *
	 * @param index the index
	 * @return the analyze request builder
	 */
	public AnalyzeRequestBuilder setIndex(String index) {
		request.index(index);
		return this;
	}

	/**
	 * Sets the analyzer.
	 *
	 * @param analyzer the analyzer
	 * @return the analyze request builder
	 */
	public AnalyzeRequestBuilder setAnalyzer(String analyzer) {
		request.analyzer(analyzer);
		return this;
	}

	/**
	 * Sets the field.
	 *
	 * @param field the field
	 * @return the analyze request builder
	 */
	public AnalyzeRequestBuilder setField(String field) {
		request.field(field);
		return this;
	}

	/**
	 * Sets the tokenizer.
	 *
	 * @param tokenizer the tokenizer
	 * @return the analyze request builder
	 */
	public AnalyzeRequestBuilder setTokenizer(String tokenizer) {
		request.tokenizer(tokenizer);
		return this;
	}

	/**
	 * Sets the token filters.
	 *
	 * @param tokenFilters the token filters
	 * @return the analyze request builder
	 */
	public AnalyzeRequestBuilder setTokenFilters(String... tokenFilters) {
		request.tokenFilters(tokenFilters);
		return this;
	}

	/**
	 * Sets the prefer local.
	 *
	 * @param preferLocal the prefer local
	 * @return the analyze request builder
	 */
	public AnalyzeRequestBuilder setPreferLocal(boolean preferLocal) {
		request.preferLocal(preferLocal);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<AnalyzeResponse> listener) {
		client.analyze(request, listener);
	}
}
