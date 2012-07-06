/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportDeleteAction.java 2012-7-6 14:29:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.delete;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.create.TransportCreateIndexAction;
import cn.com.rebirth.search.core.action.delete.index.IndexDeleteRequest;
import cn.com.rebirth.search.core.action.delete.index.IndexDeleteResponse;
import cn.com.rebirth.search.core.action.delete.index.ShardDeleteResponse;
import cn.com.rebirth.search.core.action.delete.index.TransportIndexDeleteAction;
import cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.action.shard.ShardStateAction;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.MappingMetaData;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndexAlreadyExistsException;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportDeleteAction.
 *
 * @author l.xue.nong
 */
public class TransportDeleteAction extends
		TransportShardReplicationOperationAction<DeleteRequest, DeleteRequest, DeleteResponse> {

	/** The auto create index. */
	private final boolean autoCreateIndex;

	/** The create index action. */
	private final TransportCreateIndexAction createIndexAction;

	/** The index delete action. */
	private final TransportIndexDeleteAction indexDeleteAction;

	/**
	 * Instantiates a new transport delete action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param threadPool the thread pool
	 * @param shardStateAction the shard state action
	 * @param createIndexAction the create index action
	 * @param indexDeleteAction the index delete action
	 */
	@Inject
	public TransportDeleteAction(Settings settings, TransportService transportService, ClusterService clusterService,
			IndicesService indicesService, ThreadPool threadPool, ShardStateAction shardStateAction,
			TransportCreateIndexAction createIndexAction, TransportIndexDeleteAction indexDeleteAction) {
		super(settings, transportService, clusterService, indicesService, threadPool, shardStateAction);
		this.createIndexAction = createIndexAction;
		this.indexDeleteAction = indexDeleteAction;
		this.autoCreateIndex = settings.getAsBoolean("action.auto_create_index", true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.INDEX;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#doExecute(cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(final DeleteRequest request, final ActionListener<DeleteResponse> listener) {
		if (autoCreateIndex && !clusterService.state().metaData().hasConcreteIndex(request.index())) {
			request.beforeLocalFork();
			createIndexAction.execute(new CreateIndexRequest(request.index()).cause("auto(delete api)")
					.masterNodeTimeout(request.timeout()), new ActionListener<CreateIndexResponse>() {
				@Override
				public void onResponse(CreateIndexResponse result) {
					innerExecute(request, listener);
				}

				@Override
				public void onFailure(Throwable e) {
					if (ExceptionsHelper.unwrapCause(e) instanceof IndexAlreadyExistsException) {

						innerExecute(request, listener);
					} else {
						listener.onFailure(e);
					}
				}
			});
		} else {
			innerExecute(request, listener);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#resolveRequest(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected boolean resolveRequest(final ClusterState state, final DeleteRequest request,
			final ActionListener<DeleteResponse> listener) {
		request.routing(state.metaData().resolveIndexRouting(request.routing(), request.index()));
		request.index(state.metaData().concreteIndex(request.index()));
		if (state.metaData().hasIndex(request.index())) {

			MappingMetaData mappingMd = state.metaData().index(request.index()).mapping(request.type());
			if (mappingMd != null && mappingMd.routing().required()) {
				if (request.routing() == null) {
					indexDeleteAction.execute(new IndexDeleteRequest(request),
							new ActionListener<IndexDeleteResponse>() {
								@Override
								public void onResponse(IndexDeleteResponse indexDeleteResponse) {

									long version = 0;
									boolean found = false;
									for (ShardDeleteResponse deleteResponse : indexDeleteResponse.responses()) {
										if (!deleteResponse.notFound()) {
											found = true;
											version = deleteResponse.version();
											break;
										}
									}
									listener.onResponse(new DeleteResponse(request.index(), request.type(), request
											.id(), version, !found));
								}

								@Override
								public void onFailure(Throwable e) {
									listener.onFailure(e);
								}
							});
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Inner execute.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	private void innerExecute(final DeleteRequest request, final ActionListener<DeleteResponse> listener) {
		super.doExecute(request, listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkWriteConsistency()
	 */
	@Override
	protected boolean checkWriteConsistency() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newRequestInstance()
	 */
	@Override
	protected DeleteRequest newRequestInstance() {
		return new DeleteRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newReplicaRequestInstance()
	 */
	@Override
	protected DeleteRequest newReplicaRequestInstance() {
		return new DeleteRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newResponseInstance()
	 */
	@Override
	protected DeleteResponse newResponseInstance() {
		return new DeleteResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return DeleteAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, DeleteRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.WRITE);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, DeleteRequest request) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnPrimary(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.PrimaryOperationRequest)
	 */
	@Override
	protected PrimaryResponse<DeleteResponse, DeleteRequest> shardOperationOnPrimary(ClusterState clusterState,
			PrimaryOperationRequest shardRequest) {
		DeleteRequest request = shardRequest.request;
		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);
		Engine.Delete delete = indexShard.prepareDelete(request.type(), request.id(), request.version())
				.versionType(request.versionType()).origin(Engine.Operation.Origin.PRIMARY);
		indexShard.delete(delete);

		request.version(delete.version());

		if (request.refresh()) {
			try {
				indexShard.refresh(new Engine.Refresh(false));
			} catch (Exception e) {

			}
		}

		DeleteResponse response = new DeleteResponse(request.index(), request.type(), request.id(), delete.version(),
				delete.notFound());
		return new PrimaryResponse<DeleteResponse, DeleteRequest>(shardRequest.request, response, null);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnReplica(cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.ReplicaOperationRequest)
	 */
	@Override
	protected void shardOperationOnReplica(ReplicaOperationRequest shardRequest) {
		DeleteRequest request = shardRequest.request;
		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);
		Engine.Delete delete = indexShard.prepareDelete(request.type(), request.id(), request.version()).origin(
				Engine.Operation.Origin.REPLICA);

		indexShard.delete(delete);

		if (request.refresh()) {
			try {
				indexShard.refresh(new Engine.Refresh(false));
			} catch (Exception e) {

			}
		}

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ShardIterator shards(ClusterState clusterState, DeleteRequest request) {
		return clusterService.operationRouting().deleteShards(clusterService.state(), request.index(), request.type(),
				request.id(), request.routing());
	}
}
