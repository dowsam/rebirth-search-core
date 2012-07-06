/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core UpdateRequestBuilder.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.update;

import java.util.Map;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class UpdateRequestBuilder.
 *
 * @author l.xue.nong
 */
public class UpdateRequestBuilder extends BaseRequestBuilder<UpdateRequest, UpdateResponse> {

	/**
	 * Instantiates a new update request builder.
	 *
	 * @param client the client
	 */
	public UpdateRequestBuilder(Client client) {
		super(client, new UpdateRequest());
	}

	/**
	 * Instantiates a new update request builder.
	 *
	 * @param client the client
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 */
	public UpdateRequestBuilder(Client client, String index, String type, String id) {
		super(client, new UpdateRequest(index, type, id));
	}

	/**
	 * Sets the index.
	 *
	 * @param index the index
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setIndex(String index) {
		request.index(index);
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setType(String type) {
		request.type(type);
		return this;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setId(String id) {
		request.id(id);
		return this;
	}

	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setRouting(String routing) {
		request.routing(routing);
		return this;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the parent
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setParent(String parent) {
		request.parent(parent);
		return this;
	}

	/**
	 * Sets the script.
	 *
	 * @param script the script
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setScript(String script) {
		request.script(script);
		return this;
	}

	/**
	 * Sets the script lang.
	 *
	 * @param scriptLang the script lang
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setScriptLang(String scriptLang) {
		request.scriptLang(scriptLang);
		return this;
	}

	/**
	 * Sets the script params.
	 *
	 * @param scriptParams the script params
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setScriptParams(Map<String, Object> scriptParams) {
		request.scriptParams(scriptParams);
		return this;
	}

	/**
	 * Adds the script param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the update request builder
	 */
	public UpdateRequestBuilder addScriptParam(String name, Object value) {
		request.addScriptParam(name, value);
		return this;
	}

	/**
	 * Sets the retry on conflict.
	 *
	 * @param retryOnConflict the retry on conflict
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setRetryOnConflict(int retryOnConflict) {
		request.retryOnConflict(retryOnConflict);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the refresh.
	 *
	 * @param refresh the refresh
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setRefresh(boolean refresh) {
		request.refresh(refresh);
		return this;
	}

	/**
	 * Sets the replication type.
	 *
	 * @param replicationType the replication type
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setReplicationType(ReplicationType replicationType) {
		request.replicationType(replicationType);
		return this;
	}

	/**
	 * Sets the consistency level.
	 *
	 * @param consistencyLevel the consistency level
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setConsistencyLevel(WriteConsistencyLevel consistencyLevel) {
		request.consistencyLevel(consistencyLevel);
		return this;
	}

	/**
	 * Sets the percolate.
	 *
	 * @param percolate the percolate
	 * @return the update request builder
	 */
	public UpdateRequestBuilder setPercolate(String percolate) {
		request.percolate(percolate);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<UpdateResponse> listener) {
		client.update(request, listener);
	}
}
