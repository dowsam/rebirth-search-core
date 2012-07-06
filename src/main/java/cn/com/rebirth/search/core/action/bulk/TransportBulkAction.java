/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportBulkAction.java 2012-3-29 15:02:33 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.bulk;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexRequest;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexResponse;
import cn.com.rebirth.search.core.action.admin.indices.create.TransportCreateIndexAction;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.metadata.MappingMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.indices.IndexAlreadyExistsException;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * The Class TransportBulkAction.
 *
 * @author l.xue.nong
 */
public class TransportBulkAction extends TransportAction<BulkRequest, BulkResponse> {

	
	/** The auto create index. */
	private final boolean autoCreateIndex;

	
	/** The allow id generation. */
	private final boolean allowIdGeneration;

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The shard bulk action. */
	private final TransportShardBulkAction shardBulkAction;

	
	/** The create index action. */
	private final TransportCreateIndexAction createIndexAction;

	
	/**
	 * Instantiates a new transport bulk action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param shardBulkAction the shard bulk action
	 * @param createIndexAction the create index action
	 */
	@Inject
	public TransportBulkAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterService clusterService, TransportShardBulkAction shardBulkAction,
			TransportCreateIndexAction createIndexAction) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.shardBulkAction = shardBulkAction;
		this.createIndexAction = createIndexAction;

		this.autoCreateIndex = settings.getAsBoolean("action.auto_create_index", true);
		this.allowIdGeneration = componentSettings.getAsBoolean("action.allow_id_generation", true);

		transportService.registerHandler(BulkAction.NAME, new TransportHandler());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.TransportAction#doExecute(cn.com.summall.search.core.action.ActionRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(final BulkRequest bulkRequest, final ActionListener<BulkResponse> listener) {
		final long startTime = System.currentTimeMillis();
		Set<String> indices = Sets.newHashSet();
		for (ActionRequest request : bulkRequest.requests) {
			if (request instanceof IndexRequest) {
				IndexRequest indexRequest = (IndexRequest) request;
				if (!indices.contains(indexRequest.index())) {
					indices.add(indexRequest.index());
				}
			} else if (request instanceof DeleteRequest) {
				DeleteRequest deleteRequest = (DeleteRequest) request;
				if (!indices.contains(deleteRequest.index())) {
					indices.add(deleteRequest.index());
				}
			}
		}

		if (autoCreateIndex) {
			final AtomicInteger counter = new AtomicInteger(indices.size());
			final AtomicBoolean failed = new AtomicBoolean();
			for (String index : indices) {
				if (!clusterService.state().metaData().hasConcreteIndex(index)) {
					createIndexAction.execute(new CreateIndexRequest(index).cause("auto(bulk api)"),
							new ActionListener<CreateIndexResponse>() {
								@Override
								public void onResponse(CreateIndexResponse result) {
									if (counter.decrementAndGet() == 0) {
										executeBulk(bulkRequest, startTime, listener);
									}
								}

								@Override
								public void onFailure(Throwable e) {
									if (ExceptionsHelper.unwrapCause(e) instanceof IndexAlreadyExistsException) {
										
										if (counter.decrementAndGet() == 0) {
											executeBulk(bulkRequest, startTime, listener);
										}
									} else if (failed.compareAndSet(false, true)) {
										listener.onFailure(e);
									}
								}
							});
				} else {
					if (counter.decrementAndGet() == 0) {
						executeBulk(bulkRequest, startTime, listener);
					}
				}
			}
		} else {
			executeBulk(bulkRequest, startTime, listener);
		}
	}

	
	/**
	 * Execute bulk.
	 *
	 * @param bulkRequest the bulk request
	 * @param startTime the start time
	 * @param listener the listener
	 */
	private void executeBulk(final BulkRequest bulkRequest, final long startTime,
			final ActionListener<BulkResponse> listener) {
		ClusterState clusterState = clusterService.state();
		
		clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.WRITE);

		MetaData metaData = clusterState.metaData();
		for (ActionRequest request : bulkRequest.requests) {
			if (request instanceof IndexRequest) {
				IndexRequest indexRequest = (IndexRequest) request;
				String aliasOrIndex = indexRequest.index();
				indexRequest.index(clusterState.metaData().concreteIndex(indexRequest.index()));

				MappingMetaData mappingMd = null;
				if (metaData.hasIndex(indexRequest.index())) {
					mappingMd = metaData.index(indexRequest.index()).mapping(indexRequest.type());
				}
				indexRequest.process(metaData, aliasOrIndex, mappingMd, allowIdGeneration);
			} else if (request instanceof DeleteRequest) {
				DeleteRequest deleteRequest = (DeleteRequest) request;
				deleteRequest.routing(clusterState.metaData().resolveIndexRouting(deleteRequest.routing(),
						deleteRequest.index()));
				deleteRequest.index(clusterState.metaData().concreteIndex(deleteRequest.index()));
			}
		}
		final BulkItemResponse[] responses = new BulkItemResponse[bulkRequest.requests.size()];

		
		Map<ShardId, List<BulkItemRequest>> requestsByShard = Maps.newHashMap();
		for (int i = 0; i < bulkRequest.requests.size(); i++) {
			ActionRequest request = bulkRequest.requests.get(i);
			if (request instanceof IndexRequest) {
				IndexRequest indexRequest = (IndexRequest) request;
				ShardId shardId = clusterService
						.operationRouting()
						.indexShards(clusterState, indexRequest.index(), indexRequest.type(), indexRequest.id(),
								indexRequest.routing()).shardId();
				List<BulkItemRequest> list = requestsByShard.get(shardId);
				if (list == null) {
					list = Lists.newArrayList();
					requestsByShard.put(shardId, list);
				}
				list.add(new BulkItemRequest(i, request));
			} else if (request instanceof DeleteRequest) {
				DeleteRequest deleteRequest = (DeleteRequest) request;
				MappingMetaData mappingMd = clusterState.metaData().index(deleteRequest.index())
						.mapping(deleteRequest.type());
				if (mappingMd != null && mappingMd.routing().required() && deleteRequest.routing() == null) {
					
					GroupShardsIterator groupShards = clusterService.operationRouting().broadcastDeleteShards(
							clusterState, deleteRequest.index());
					for (ShardIterator shardIt : groupShards) {
						List<BulkItemRequest> list = requestsByShard.get(shardIt.shardId());
						if (list == null) {
							list = Lists.newArrayList();
							requestsByShard.put(shardIt.shardId(), list);
						}
						list.add(new BulkItemRequest(i, request));
					}
				} else {
					ShardId shardId = clusterService
							.operationRouting()
							.deleteShards(clusterState, deleteRequest.index(), deleteRequest.type(),
									deleteRequest.id(), deleteRequest.routing()).shardId();
					List<BulkItemRequest> list = requestsByShard.get(shardId);
					if (list == null) {
						list = Lists.newArrayList();
						requestsByShard.put(shardId, list);
					}
					list.add(new BulkItemRequest(i, request));
				}
			}
		}

		if (requestsByShard.isEmpty()) {
			listener.onResponse(new BulkResponse(responses, System.currentTimeMillis() - startTime));
			return;
		}

		final AtomicInteger counter = new AtomicInteger(requestsByShard.size());
		for (Map.Entry<ShardId, List<BulkItemRequest>> entry : requestsByShard.entrySet()) {
			final ShardId shardId = entry.getKey();
			final List<BulkItemRequest> requests = entry.getValue();
			BulkShardRequest bulkShardRequest = new BulkShardRequest(shardId.index().name(), shardId.id(),
					bulkRequest.refresh(), requests.toArray(new BulkItemRequest[requests.size()]));
			bulkShardRequest.replicationType(bulkRequest.replicationType());
			bulkShardRequest.consistencyLevel(bulkRequest.consistencyLevel());
			shardBulkAction.execute(bulkShardRequest, new ActionListener<BulkShardResponse>() {
				@Override
				public void onResponse(BulkShardResponse bulkShardResponse) {
					synchronized (responses) {
						for (BulkItemResponse bulkItemResponse : bulkShardResponse.responses()) {
							responses[bulkItemResponse.itemId()] = bulkItemResponse;
						}
					}
					if (counter.decrementAndGet() == 0) {
						finishHim();
					}
				}

				@Override
				public void onFailure(Throwable e) {
					
					String message = ExceptionsHelper.detailedMessage(e);
					synchronized (responses) {
						for (BulkItemRequest request : requests) {
							if (request.request() instanceof IndexRequest) {
								IndexRequest indexRequest = (IndexRequest) request.request();
								responses[request.id()] = new BulkItemResponse(request.id(), indexRequest.opType()
										.toString().toLowerCase(), new BulkItemResponse.Failure(indexRequest.index(),
										indexRequest.type(), indexRequest.id(), message));
							} else if (request.request() instanceof DeleteRequest) {
								DeleteRequest deleteRequest = (DeleteRequest) request.request();
								responses[request.id()] = new BulkItemResponse(request.id(), "delete",
										new BulkItemResponse.Failure(deleteRequest.index(), deleteRequest.type(),
												deleteRequest.id(), message));
							}
						}
					}
					if (counter.decrementAndGet() == 0) {
						finishHim();
					}
				}

				private void finishHim() {
					listener.onResponse(new BulkResponse(responses, System.currentTimeMillis() - startTime));
				}
			});
		}
	}

	
	/**
	 * The Class TransportHandler.
	 *
	 * @author l.xue.nong
	 */
	class TransportHandler extends BaseTransportRequestHandler<BulkRequest> {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public BulkRequest newInstance() {
			return new BulkRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final BulkRequest request, final TransportChannel channel) throws Exception {
			
			request.listenerThreaded(false);
			execute(request, new ActionListener<BulkResponse>() {
				@Override
				public void onResponse(BulkResponse result) {
					try {
						channel.sendResponse(result);
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(e);
					} catch (Exception e1) {
						logger.warn("Failed to send error response for action [" + BulkAction.NAME + "] and request ["
								+ request + "]", e1);
					}
				}
			});
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}
	}
}
