/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportIndexAction.java 2012-7-6 14:30:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.index;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.RoutingMissingException;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.create.TransportCreateIndexAction;
import cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.action.index.MappingUpdatedAction;
import cn.com.rebirth.search.core.cluster.action.shard.ShardStateAction;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.MappingMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.SourceToParse;
import cn.com.rebirth.search.core.index.percolator.PercolatorExecutor;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndexAlreadyExistsException;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportIndexAction.
 *
 * @author l.xue.nong
 */
public class TransportIndexAction extends
		TransportShardReplicationOperationAction<IndexRequest, IndexRequest, IndexResponse> {

	/** The auto create index. */
	private final boolean autoCreateIndex;

	/** The allow id generation. */
	private final boolean allowIdGeneration;

	/** The create index action. */
	private final TransportCreateIndexAction createIndexAction;

	/** The mapping updated action. */
	private final MappingUpdatedAction mappingUpdatedAction;

	/** The wait for mapping change. */
	private final boolean waitForMappingChange;

	/**
	 * Instantiates a new transport index action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param threadPool the thread pool
	 * @param shardStateAction the shard state action
	 * @param createIndexAction the create index action
	 * @param mappingUpdatedAction the mapping updated action
	 */
	@Inject
	public TransportIndexAction(Settings settings, TransportService transportService, ClusterService clusterService,
			IndicesService indicesService, ThreadPool threadPool, ShardStateAction shardStateAction,
			TransportCreateIndexAction createIndexAction, MappingUpdatedAction mappingUpdatedAction) {
		super(settings, transportService, clusterService, indicesService, threadPool, shardStateAction);
		this.createIndexAction = createIndexAction;
		this.mappingUpdatedAction = mappingUpdatedAction;
		this.autoCreateIndex = settings.getAsBoolean("action.auto_create_index", true);
		this.allowIdGeneration = settings.getAsBoolean("action.allow_id_generation", true);
		this.waitForMappingChange = settings.getAsBoolean("action.wait_on_mapping_change", false);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#doExecute(cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(final IndexRequest request, final ActionListener<IndexResponse> listener) {

		if (autoCreateIndex && !clusterService.state().metaData().hasConcreteIndex(request.index())) {
			request.beforeLocalFork();
			createIndexAction.execute(new CreateIndexRequest(request.index()).cause("auto(index api)")
					.masterNodeTimeout(request.timeout()), new ActionListener<CreateIndexResponse>() {
				@Override
				public void onResponse(CreateIndexResponse result) {
					innerExecute(request, listener);
				}

				@Override
				public void onFailure(Throwable e) {
					if (ExceptionsHelper.unwrapCause(e) instanceof IndexAlreadyExistsException) {

						try {
							innerExecute(request, listener);
						} catch (Exception e1) {
							listener.onFailure(e1);
						}
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
	protected boolean resolveRequest(ClusterState state, IndexRequest request,
			ActionListener<IndexResponse> indexResponseActionListener) {
		MetaData metaData = clusterService.state().metaData();
		String aliasOrIndex = request.index();
		request.index(metaData.concreteIndex(request.index()));
		MappingMetaData mappingMd = null;
		if (metaData.hasIndex(request.index())) {
			mappingMd = metaData.index(request.index()).mapping(request.type());
		}
		request.process(metaData, aliasOrIndex, mappingMd, allowIdGeneration);
		return true;
	}

	/**
	 * Inner execute.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	private void innerExecute(final IndexRequest request, final ActionListener<IndexResponse> listener) {
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
	protected IndexRequest newRequestInstance() {
		return new IndexRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newReplicaRequestInstance()
	 */
	@Override
	protected IndexRequest newReplicaRequestInstance() {
		return new IndexRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newResponseInstance()
	 */
	@Override
	protected IndexResponse newResponseInstance() {
		return new IndexResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return IndexAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.INDEX;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, IndexRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.WRITE);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, IndexRequest request) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ShardIterator shards(ClusterState clusterState, IndexRequest request) {
		return clusterService.operationRouting().indexShards(clusterService.state(), request.index(), request.type(),
				request.id(), request.routing());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnPrimary(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.PrimaryOperationRequest)
	 */
	@Override
	protected PrimaryResponse<IndexResponse, IndexRequest> shardOperationOnPrimary(ClusterState clusterState,
			PrimaryOperationRequest shardRequest) {
		final IndexRequest request = shardRequest.request;

		MappingMetaData mappingMd = clusterState.metaData().index(request.index()).mapping(request.type());
		if (mappingMd != null && mappingMd.routing().required()) {
			if (request.routing() == null) {
				throw new RoutingMissingException(request.index(), request.type(), request.id());
			}
		}

		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);
		SourceToParse sourceToParse = SourceToParse
				.source(request.underlyingSource(), request.underlyingSourceOffset(), request.underlyingSourceLength())
				.type(request.type()).id(request.id()).routing(request.routing()).parent(request.parent())
				.timestamp(request.timestamp()).ttl(request.ttl());
		long version;
		Engine.IndexingOperation op;
		if (request.opType() == IndexRequest.OpType.INDEX) {
			Engine.Index index = indexShard.prepareIndex(sourceToParse).version(request.version())
					.versionType(request.versionType()).origin(Engine.Operation.Origin.PRIMARY);
			indexShard.index(index);
			version = index.version();
			op = index;
		} else {
			Engine.Create create = indexShard.prepareCreate(sourceToParse).version(request.version())
					.versionType(request.versionType()).origin(Engine.Operation.Origin.PRIMARY);
			indexShard.create(create);
			version = create.version();
			op = create;
		}
		if (request.refresh()) {
			try {
				indexShard.refresh(new Engine.Refresh(false));
			} catch (Exception e) {

			}
		}
		if (op.parsedDoc().mappersAdded()) {
			updateMappingOnMaster(request);
		}

		request.version(version);

		IndexResponse response = new IndexResponse(request.index(), request.type(), request.id(), version);
		return new PrimaryResponse<IndexResponse, IndexRequest>(shardRequest.request, response, op);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#postPrimaryOperation(cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest, cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.PrimaryResponse)
	 */
	@Override
	protected void postPrimaryOperation(IndexRequest request, PrimaryResponse<IndexResponse, IndexRequest> response) {
		Engine.IndexingOperation op = (Engine.IndexingOperation) response.payload();
		if (!Strings.hasLength(request.percolate())) {
			return;
		}
		IndexService indexService = indicesService.indexServiceSafe(request.index());
		try {
			PercolatorExecutor.Response percolate = indexService.percolateService().percolate(
					new PercolatorExecutor.DocAndSourceQueryRequest(op.parsedDoc(), request.percolate()));
			response.response().matches(percolate.matches());
		} catch (Exception e) {
			logger.warn("failed to percolate [{}]", e, request);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnReplica(cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.ReplicaOperationRequest)
	 */
	@Override
	protected void shardOperationOnReplica(ReplicaOperationRequest shardRequest) {
		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);
		IndexRequest request = shardRequest.request;
		SourceToParse sourceToParse = SourceToParse
				.source(request.underlyingSource(), request.underlyingSourceOffset(), request.underlyingSourceLength())
				.type(request.type()).id(request.id()).routing(request.routing()).parent(request.parent())
				.timestamp(request.timestamp()).ttl(request.ttl());
		if (request.opType() == IndexRequest.OpType.INDEX) {
			Engine.Index index = indexShard.prepareIndex(sourceToParse).version(request.version())
					.origin(Engine.Operation.Origin.REPLICA);
			indexShard.index(index);
		} else {
			Engine.Create create = indexShard.prepareCreate(sourceToParse).version(request.version())
					.origin(Engine.Operation.Origin.REPLICA);
			indexShard.create(create);
		}
		if (request.refresh()) {
			try {
				indexShard.refresh(new Engine.Refresh(false));
			} catch (Exception e) {

			}
		}
	}

	/**
	 * Update mapping on master.
	 *
	 * @param request the request
	 */
	private void updateMappingOnMaster(final IndexRequest request) {
		final CountDownLatch latch = new CountDownLatch(1);
		try {
			MapperService mapperService = indicesService.indexServiceSafe(request.index()).mapperService();
			final DocumentMapper documentMapper = mapperService.documentMapper(request.type());
			if (documentMapper == null) {
				return;
			}
			documentMapper.refreshSource();

			mappingUpdatedAction.execute(new MappingUpdatedAction.MappingUpdatedRequest(request.index(),
					request.type(), documentMapper.mappingSource()),
					new ActionListener<MappingUpdatedAction.MappingUpdatedResponse>() {
						@Override
						public void onResponse(MappingUpdatedAction.MappingUpdatedResponse mappingUpdatedResponse) {

							latch.countDown();
						}

						@Override
						public void onFailure(Throwable e) {
							latch.countDown();
							try {
								logger.warn("Failed to update master on updated mapping for index [" + request.index()
										+ "], type [" + request.type() + "] and source ["
										+ documentMapper.mappingSource().string() + "]", e);
							} catch (IOException e1) {

							}
						}
					});
		} catch (Exception e) {
			latch.countDown();
			logger.warn("Failed to update master on updated mapping for index [" + request.index() + "], type ["
					+ request.type() + "]", e);
		}

		if (waitForMappingChange) {
			try {
				latch.await(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {

			}
		}
	}
}
