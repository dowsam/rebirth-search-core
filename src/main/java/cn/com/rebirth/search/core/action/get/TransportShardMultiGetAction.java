/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportShardMultiGetAction.java 2012-7-6 14:28:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.get;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.get.GetResult;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportShardMultiGetAction.
 *
 * @author l.xue.nong
 */
public class TransportShardMultiGetAction extends
		TransportShardSingleOperationAction<MultiGetShardRequest, MultiGetShardResponse> {

	/** The indices service. */
	private final IndicesService indicesService;

	/** The realtime. */
	private final boolean realtime;

	/**
	 * Instantiates a new transport shard multi get action.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 * @param threadPool the thread pool
	 */
	@Inject
	public TransportShardMultiGetAction(Settings settings, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService, ThreadPool threadPool) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;

		this.realtime = settings.getAsBoolean("action.get.realtime", true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.GET;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return MultiGetAction.NAME + "/shard";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction#newRequest()
	 */
	@Override
	protected MultiGetShardRequest newRequest() {
		return new MultiGetShardRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction#newResponse()
	 */
	@Override
	protected MultiGetShardResponse newResponse() {
		return new MultiGetShardResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, MultiGetShardRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.READ);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, MultiGetShardRequest request) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.READ, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest)
	 */
	@Override
	protected ShardIterator shards(ClusterState state, MultiGetShardRequest request) {
		return clusterService.operationRouting().getShards(clusterService.state(), request.index(), request.shardId(),
				request.preference());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction#resolveRequest(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest)
	 */
	@Override
	protected void resolveRequest(ClusterState state, MultiGetShardRequest request) {
		if (request.realtime == null) {
			request.realtime = this.realtime;
		}

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.TransportShardSingleOperationAction#shardOperation(cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest, int)
	 */
	@Override
	protected MultiGetShardResponse shardOperation(MultiGetShardRequest request, int shardId) throws RebirthException {
		IndexService indexService = indicesService.indexServiceSafe(request.index());
		IndexShard indexShard = indexService.shardSafe(shardId);

		if (request.refresh() && !request.realtime()) {
			indexShard.refresh(new Engine.Refresh(false));
		}

		MultiGetShardResponse response = new MultiGetShardResponse();
		for (int i = 0; i < request.locations.size(); i++) {
			String type = request.types.get(i);
			String id = request.ids.get(i);
			String[] fields = request.fields.get(i);

			try {
				GetResult getResult = indexShard.getService().get(type, id, fields, request.realtime());
				response.add(request.locations.get(i), new GetResponse(getResult));
			} catch (Exception e) {
				logger.debug("[" + request.index() + "][" + shardId + "] failed to execute multi_get for [" + type
						+ "]/[" + id + "]", e);
				response.add(request.locations.get(i), new MultiGetResponse.Failure(request.index(), type, id,
						ExceptionsHelper.detailedMessage(e)));
			}
		}

		return response;
	}

}