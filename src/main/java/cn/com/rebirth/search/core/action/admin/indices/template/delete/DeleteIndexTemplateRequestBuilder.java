/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteIndexTemplateRequestBuilder.java 2012-3-29 15:01:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.template.delete;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.support.BaseIndicesRequestBuilder;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class DeleteIndexTemplateRequestBuilder.
 *
 * @author l.xue.nong
 */
public class DeleteIndexTemplateRequestBuilder extends
		BaseIndicesRequestBuilder<DeleteIndexTemplateRequest, DeleteIndexTemplateResponse> {

	
	/**
	 * Instantiates a new delete index template request builder.
	 *
	 * @param indicesClient the indices client
	 */
	public DeleteIndexTemplateRequestBuilder(IndicesAdminClient indicesClient) {
		super(indicesClient, new DeleteIndexTemplateRequest());
	}

	
	/**
	 * Instantiates a new delete index template request builder.
	 *
	 * @param indicesClient the indices client
	 * @param name the name
	 */
	public DeleteIndexTemplateRequestBuilder(IndicesAdminClient indicesClient, String name) {
		super(indicesClient, new DeleteIndexTemplateRequest(name));
	}

	
	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index template request builder
	 */
	public DeleteIndexTemplateRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	
	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index template request builder
	 */
	public DeleteIndexTemplateRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index template request builder
	 */
	public DeleteIndexTemplateRequestBuilder setMasterNodeTimeout(TimeValue timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/**
	 * Sets the master node timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index template request builder
	 */
	public DeleteIndexTemplateRequestBuilder setMasterNodeTimeout(String timeout) {
		request.masterNodeTimeout(timeout);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.support.BaseIndicesRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<DeleteIndexTemplateResponse> listener) {
		client.deleteTemplate(request, listener);
	}
}
