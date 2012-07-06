/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteRequestBuilder.java 2012-3-29 15:01:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.delete;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.index.VersionType;


/**
 * The Class DeleteRequestBuilder.
 *
 * @author l.xue.nong
 */
public class DeleteRequestBuilder extends BaseRequestBuilder<DeleteRequest, DeleteResponse> {

	
	/**
	 * Instantiates a new delete request builder.
	 *
	 * @param client the client
	 */
	public DeleteRequestBuilder(Client client) {
		super(client, new DeleteRequest());
	}

	
	/**
	 * Instantiates a new delete request builder.
	 *
	 * @param client the client
	 * @param index the index
	 */
	public DeleteRequestBuilder(Client client, @Nullable String index) {
		super(client, new DeleteRequest(index));
	}

	
	/**
	 * Sets the index.
	 *
	 * @param index the index
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setIndex(String index) {
		request.index(index);
		return this;
	}

	
	/**
	 * Sets the type.
	 *
	 * @param type the type
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setType(String type) {
		request.type(type);
		return this;
	}

	
	/**
	 * Sets the id.
	 *
	 * @param id the id
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setId(String id) {
		request.id(id);
		return this;
	}

	
	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setRouting(String routing) {
		request.routing(routing);
		return this;
	}

	
	/**
	 * Sets the refresh.
	 *
	 * @param refresh the refresh
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setRefresh(boolean refresh) {
		request.refresh(refresh);
		return this;
	}

	
	/**
	 * Sets the version.
	 *
	 * @param version the version
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setVersion(long version) {
		request.version(version);
		return this;
	}

	
	/**
	 * Sets the version type.
	 *
	 * @param versionType the version type
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setVersionType(VersionType versionType) {
		request.versionType(versionType);
		return this;
	}

	
	/**
	 * Sets the listener threaded.
	 *
	 * @param threadedListener the threaded listener
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setListenerThreaded(boolean threadedListener) {
		request.listenerThreaded(threadedListener);
		return this;
	}

	
	/**
	 * Sets the operation threaded.
	 *
	 * @param threadedOperation the threaded operation
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setOperationThreaded(boolean threadedOperation) {
		request.operationThreaded(threadedOperation);
		return this;
	}

	
	/**
	 * Sets the replication type.
	 *
	 * @param replicationType the replication type
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setReplicationType(ReplicationType replicationType) {
		request.replicationType(replicationType);
		return this;
	}

	
	/**
	 * Sets the consistency level.
	 *
	 * @param consistencyLevel the consistency level
	 * @return the delete request builder
	 */
	public DeleteRequestBuilder setConsistencyLevel(WriteConsistencyLevel consistencyLevel) {
		request.consistencyLevel(consistencyLevel);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<DeleteResponse> listener) {
		client.delete(request, listener);
	}
}
