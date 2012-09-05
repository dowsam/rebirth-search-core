/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportClearIndicesCacheAction.java 2012-7-6 14:29:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.cache.clear;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.DefaultShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportClearIndicesCacheAction.
 *
 * @author l.xue.nong
 */
public class TransportClearIndicesCacheAction
		extends
		TransportBroadcastOperationAction<ClearIndicesCacheRequest, ClearIndicesCacheResponse, ShardClearIndicesCacheRequest, ShardClearIndicesCacheResponse> {

	/** The indices service. */
	private final IndicesService indicesService;

	/**
	 * Instantiates a new transport clear indices cache action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 */
	@Inject
	public TransportClearIndicesCacheAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.MANAGEMENT;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return ClearIndicesCacheAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newRequest()
	 */
	@Override
	protected ClearIndicesCacheRequest newRequest() {
		return new ClearIndicesCacheRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#ignoreNonActiveExceptions()
	 */
	@Override
	protected boolean ignoreNonActiveExceptions() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newResponse(cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray, cn.com.rebirth.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClearIndicesCacheResponse newResponse(ClearIndicesCacheRequest request,
			AtomicReferenceArray shardsResponses, ClusterState clusterState) {
		int successfulShards = 0;
		int failedShards = 0;
		List<ShardOperationFailedException> shardFailures = null;
		for (int i = 0; i < shardsResponses.length(); i++) {
			Object shardResponse = shardsResponses.get(i);
			if (shardResponse == null) {

			} else if (shardResponse instanceof BroadcastShardOperationFailedException) {
				failedShards++;
				if (shardFailures == null) {
					shardFailures = newArrayList();
				}
				shardFailures.add(new DefaultShardOperationFailedException(
						(BroadcastShardOperationFailedException) shardResponse));
			} else {
				successfulShards++;
			}
		}
		return new ClearIndicesCacheResponse(shardsResponses.length(), successfulShards, failedShards, shardFailures);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest()
	 */
	@Override
	protected ShardClearIndicesCacheRequest newShardRequest() {
		return new ShardClearIndicesCacheRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardRequest(cn.com.rebirth.search.core.cluster.routing.ShardRouting, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ShardClearIndicesCacheRequest newShardRequest(ShardRouting shard, ClearIndicesCacheRequest request) {
		return new ShardClearIndicesCacheRequest(shard.index(), shard.id(), request);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#newShardResponse()
	 */
	@Override
	protected ShardClearIndicesCacheResponse newShardResponse() {
		return new ShardClearIndicesCacheResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#shardOperation(cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest)
	 */
	@Override
	protected ShardClearIndicesCacheResponse shardOperation(ShardClearIndicesCacheRequest request)
			throws RebirthException {
		IndexService service = indicesService.indexService(request.index());
		if (service != null) {

			service.cache().queryParserCache().clear();
			boolean clearedAtLeastOne = false;
			if (request.filterCache()) {
				clearedAtLeastOne = true;
				service.cache().filter().clear();
			}
			if (request.fieldDataCache()) {
				clearedAtLeastOne = true;
				if (request.fields() == null || request.fields().length == 0) {
					service.cache().fieldData().clear();
				} else {
					for (String field : request.fields()) {
						service.cache().fieldData().clear(field);
					}
				}
			}
			if (request.idCache()) {
				clearedAtLeastOne = true;
				service.cache().idCache().clear();
			}
			if (request.bloomCache()) {
				clearedAtLeastOne = true;
				service.cache().bloomCache().clear();
			}
			if (!clearedAtLeastOne) {
				if (request.fields() != null && request.fields().length > 0) {

					for (String field : request.fields()) {
						service.cache().fieldData().clear(field);
					}
				} else {
					service.cache().clear();
				}
			}
			service.cache().invalidateCache();
		}
		return new ShardClearIndicesCacheResponse(request.index(), request.shardId());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected GroupShardsIterator shards(ClusterState clusterState, ClearIndicesCacheRequest request,
			String[] concreteIndices) {
		return clusterState.routingTable().allActiveShardsGrouped(concreteIndices, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, ClearIndicesCacheRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.TransportBroadcastOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest, java.lang.String[])
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, ClearIndicesCacheRequest request,
			String[] concreteIndices) {
		return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
	}

}