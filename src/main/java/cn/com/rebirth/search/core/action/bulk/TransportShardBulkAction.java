/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportShardBulkAction.java 2012-7-6 14:29:36 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.bulk;

import java.io.IOException;
import java.util.Set;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.RoutingMissingException;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.delete.DeleteResponse;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.index.IndexResponse;
import cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.action.index.MappingUpdatedAction;
import cn.com.rebirth.search.core.cluster.action.shard.ShardStateAction;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.MappingMetaData;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.SourceToParse;
import cn.com.rebirth.search.core.index.percolator.PercolatorExecutor;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Sets;

/**
 * The Class TransportShardBulkAction.
 *
 * @author l.xue.nong
 */
public class TransportShardBulkAction extends
		TransportShardReplicationOperationAction<BulkShardRequest, BulkShardRequest, BulkShardResponse> {

	/** The mapping updated action. */
	private final MappingUpdatedAction mappingUpdatedAction;

	/**
	 * Instantiates a new transport shard bulk action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param threadPool the thread pool
	 * @param shardStateAction the shard state action
	 * @param mappingUpdatedAction the mapping updated action
	 */
	@Inject
	public TransportShardBulkAction(Settings settings, TransportService transportService,
			ClusterService clusterService, IndicesService indicesService, ThreadPool threadPool,
			ShardStateAction shardStateAction, MappingUpdatedAction mappingUpdatedAction) {
		super(settings, transportService, clusterService, indicesService, threadPool, shardStateAction);
		this.mappingUpdatedAction = mappingUpdatedAction;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.BULK;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkWriteConsistency()
	 */
	@Override
	protected boolean checkWriteConsistency() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#transportOptions()
	 */
	@Override
	protected TransportRequestOptions transportOptions() {

		return TransportRequestOptions.options().withCompress(true).withLowType();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newRequestInstance()
	 */
	@Override
	protected BulkShardRequest newRequestInstance() {
		return new BulkShardRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newReplicaRequestInstance()
	 */
	@Override
	protected BulkShardRequest newReplicaRequestInstance() {
		return new BulkShardRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#newResponseInstance()
	 */
	@Override
	protected BulkShardResponse newResponseInstance() {
		return new BulkShardResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return BulkAction.NAME + "/shard";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, BulkShardRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.WRITE);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, BulkShardRequest request) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest)
	 */
	@Override
	protected ShardIterator shards(ClusterState clusterState, BulkShardRequest request) {
		return clusterState.routingTable().index(request.index()).shard(request.shardId()).shardsIt();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnPrimary(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.PrimaryOperationRequest)
	 */
	@Override
	protected PrimaryResponse<BulkShardResponse, BulkShardRequest> shardOperationOnPrimary(ClusterState clusterState,
			PrimaryOperationRequest shardRequest) {
		final BulkShardRequest request = shardRequest.request;
		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);

		Engine.IndexingOperation[] ops = null;

		Set<Tuple<String, String>> mappingsToUpdate = null;

		BulkItemResponse[] responses = new BulkItemResponse[request.items().length];
		for (int i = 0; i < request.items().length; i++) {
			BulkItemRequest item = request.items()[i];
			if (item.request() instanceof IndexRequest) {
				IndexRequest indexRequest = (IndexRequest) item.request();
				try {

					MappingMetaData mappingMd = clusterState.metaData().index(request.index())
							.mapping(indexRequest.type());
					if (mappingMd != null && mappingMd.routing().required()) {
						if (indexRequest.routing() == null) {
							throw new RoutingMissingException(indexRequest.index(), indexRequest.type(),
									indexRequest.id());
						}
					}

					SourceToParse sourceToParse = SourceToParse
							.source(indexRequest.underlyingSource(), indexRequest.underlyingSourceOffset(),
									indexRequest.underlyingSourceLength()).type(indexRequest.type())
							.id(indexRequest.id()).routing(indexRequest.routing()).parent(indexRequest.parent())
							.timestamp(indexRequest.timestamp()).ttl(indexRequest.ttl());

					long version;
					Engine.IndexingOperation op;
					if (indexRequest.opType() == IndexRequest.OpType.INDEX) {
						Engine.Index index = indexShard.prepareIndex(sourceToParse).version(indexRequest.version())
								.versionType(indexRequest.versionType()).origin(Engine.Operation.Origin.PRIMARY);
						indexShard.index(index);
						version = index.version();
						op = index;
					} else {
						Engine.Create create = indexShard.prepareCreate(sourceToParse).version(indexRequest.version())
								.versionType(indexRequest.versionType()).origin(Engine.Operation.Origin.PRIMARY);
						indexShard.create(create);
						version = create.version();
						op = create;
					}

					indexRequest.version(version);

					if (op.parsedDoc().mappersAdded()) {
						if (mappingsToUpdate == null) {
							mappingsToUpdate = Sets.newHashSet();
						}
						mappingsToUpdate.add(Tuple.create(indexRequest.index(), indexRequest.type()));
					}

					if (Strings.hasLength(indexRequest.percolate())) {
						if (ops == null) {
							ops = new Engine.IndexingOperation[request.items().length];
						}
						ops[i] = op;
					}

					responses[i] = new BulkItemResponse(item.id(), indexRequest.opType().toString().toLowerCase(),
							new IndexResponse(indexRequest.index(), indexRequest.type(), indexRequest.id(), version));
				} catch (Exception e) {

					if (retryPrimaryException(e)) {
						throw (RebirthException) e;
					}
					responses[i] = new BulkItemResponse(item.id(), indexRequest.opType().toString().toLowerCase(),
							new BulkItemResponse.Failure(indexRequest.index(), indexRequest.type(), indexRequest.id(),
									ExceptionsHelper.detailedMessage(e)));

					request.items()[i] = null;
				}
			} else if (item.request() instanceof DeleteRequest) {
				DeleteRequest deleteRequest = (DeleteRequest) item.request();
				try {
					Engine.Delete delete = indexShard
							.prepareDelete(deleteRequest.type(), deleteRequest.id(), deleteRequest.version())
							.versionType(deleteRequest.versionType()).origin(Engine.Operation.Origin.PRIMARY);
					indexShard.delete(delete);

					deleteRequest.version(delete.version());

					responses[i] = new BulkItemResponse(item.id(), "delete", new DeleteResponse(deleteRequest.index(),
							deleteRequest.type(), deleteRequest.id(), delete.version(), delete.notFound()));
				} catch (Exception e) {

					if (retryPrimaryException(e)) {
						throw (RebirthException) e;
					}
					responses[i] = new BulkItemResponse(item.id(), "delete", new BulkItemResponse.Failure(
							deleteRequest.index(), deleteRequest.type(), deleteRequest.id(),
							ExceptionsHelper.detailedMessage(e)));

					request.items()[i] = null;
				}
			}
		}

		if (mappingsToUpdate != null) {
			for (Tuple<String, String> mappingToUpdate : mappingsToUpdate) {
				updateMappingOnMaster(mappingToUpdate.v1(), mappingToUpdate.v2());
			}
		}

		if (request.refresh()) {
			try {
				indexShard.refresh(new Engine.Refresh(false));
			} catch (Exception e) {

			}
		}
		BulkShardResponse response = new BulkShardResponse(new ShardId(request.index(), request.shardId()), responses);
		return new PrimaryResponse<BulkShardResponse, BulkShardRequest>(shardRequest.request, response, ops);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#postPrimaryOperation(cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest, cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.PrimaryResponse)
	 */
	@Override
	protected void postPrimaryOperation(BulkShardRequest request,
			PrimaryResponse<BulkShardResponse, BulkShardRequest> response) {
		IndexService indexService = indicesService.indexServiceSafe(request.index());
		Engine.IndexingOperation[] ops = (Engine.IndexingOperation[]) response.payload();
		if (ops == null) {
			return;
		}
		for (int i = 0; i < ops.length; i++) {
			BulkItemRequest itemRequest = request.items()[i];
			BulkItemResponse itemResponse = response.response().responses()[i];
			if (itemResponse.failed()) {

				continue;
			}
			Engine.IndexingOperation op = ops[i];
			if (op == null) {
				continue;
			}
			if (itemRequest.request() instanceof IndexRequest) {
				IndexRequest indexRequest = (IndexRequest) itemRequest.request();
				if (!Strings.hasLength(indexRequest.percolate())) {
					continue;
				}
				try {
					PercolatorExecutor.Response percolate = indexService.percolateService().percolate(
							new PercolatorExecutor.DocAndSourceQueryRequest(op.parsedDoc(), indexRequest.percolate()));
					((IndexResponse) itemResponse.response()).matches(percolate.matches());
				} catch (Exception e) {
					logger.warn("failed to percolate [{}]", e, itemRequest.request());
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction#shardOperationOnReplica(cn.com.rebirth.search.core.action.support.replication.TransportShardReplicationOperationAction.ReplicaOperationRequest)
	 */
	@Override
	protected void shardOperationOnReplica(ReplicaOperationRequest shardRequest) {
		IndexShard indexShard = indicesService.indexServiceSafe(shardRequest.request.index()).shardSafe(
				shardRequest.shardId);
		final BulkShardRequest request = shardRequest.request;
		for (int i = 0; i < request.items().length; i++) {
			BulkItemRequest item = request.items()[i];
			if (item == null) {
				continue;
			}
			if (item.request() instanceof IndexRequest) {
				IndexRequest indexRequest = (IndexRequest) item.request();
				try {
					SourceToParse sourceToParse = SourceToParse
							.source(indexRequest.underlyingSource(), indexRequest.underlyingSourceOffset(),
									indexRequest.underlyingSourceLength()).type(indexRequest.type())
							.id(indexRequest.id()).routing(indexRequest.routing()).parent(indexRequest.parent())
							.timestamp(indexRequest.timestamp()).ttl(indexRequest.ttl());

					if (indexRequest.opType() == IndexRequest.OpType.INDEX) {
						Engine.Index index = indexShard.prepareIndex(sourceToParse).version(indexRequest.version())
								.origin(Engine.Operation.Origin.REPLICA);
						indexShard.index(index);
					} else {
						Engine.Create create = indexShard.prepareCreate(sourceToParse).version(indexRequest.version())
								.origin(Engine.Operation.Origin.REPLICA);
						indexShard.create(create);
					}
				} catch (Exception e) {

				}
			} else if (item.request() instanceof DeleteRequest) {
				DeleteRequest deleteRequest = (DeleteRequest) item.request();
				try {
					Engine.Delete delete = indexShard.prepareDelete(deleteRequest.type(), deleteRequest.id(),
							deleteRequest.version()).origin(Engine.Operation.Origin.REPLICA);
					indexShard.delete(delete);
				} catch (Exception e) {

				}
			}
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
	 * @param index the index
	 * @param type the type
	 */
	private void updateMappingOnMaster(final String index, final String type) {
		try {
			MapperService mapperService = indicesService.indexServiceSafe(index).mapperService();
			final DocumentMapper documentMapper = mapperService.documentMapper(type);
			if (documentMapper == null) {
				return;
			}
			documentMapper.refreshSource();

			mappingUpdatedAction.execute(
					new MappingUpdatedAction.MappingUpdatedRequest(index, type, documentMapper.mappingSource()),
					new ActionListener<MappingUpdatedAction.MappingUpdatedResponse>() {
						@Override
						public void onResponse(MappingUpdatedAction.MappingUpdatedResponse mappingUpdatedResponse) {

						}

						@Override
						public void onFailure(Throwable e) {
							try {
								logger.warn("failed to update master on updated mapping for index [" + index
										+ "], type [" + type + "] and source ["
										+ documentMapper.mappingSource().string() + "]", e);
							} catch (IOException e1) {

							}
						}
					});
		} catch (Exception e) {
			logger.warn("failed to update master on updated mapping for index [" + index + "], type [" + type + "]", e);
		}
	}
}
