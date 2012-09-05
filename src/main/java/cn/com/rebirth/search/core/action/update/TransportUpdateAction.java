/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportUpdateAction.java 2012-7-6 14:30:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.update;

import java.util.HashMap;
import java.util.Map;

import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentHelper;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.delete.DeleteResponse;
import cn.com.rebirth.search.core.action.delete.TransportDeleteAction;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.index.IndexResponse;
import cn.com.rebirth.search.core.action.index.TransportIndexAction;
import cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.routing.PlainShardIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.engine.DocumentMissingException;
import cn.com.rebirth.search.core.index.engine.DocumentSourceMissingException;
import cn.com.rebirth.search.core.index.engine.VersionConflictEngineException;
import cn.com.rebirth.search.core.index.get.GetResult;
import cn.com.rebirth.search.core.index.mapper.internal.ParentFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.RoutingFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TTLFieldMapper;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.IllegalIndexShardStateException;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.script.ExecutableScript;
import cn.com.rebirth.search.core.script.ScriptService;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.ImmutableList;

/**
 * The Class TransportUpdateAction.
 *
 * @author l.xue.nong
 */
public class TransportUpdateAction extends TransportInstanceSingleOperationAction<UpdateRequest, UpdateResponse> {

	/** The indices service. */
	private final IndicesService indicesService;

	/** The delete action. */
	private final TransportDeleteAction deleteAction;

	/** The index action. */
	private final TransportIndexAction indexAction;

	/** The script service. */
	private final ScriptService scriptService;

	/**
	 * Instantiates a new transport update action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 * @param indexAction the index action
	 * @param deleteAction the delete action
	 * @param scriptService the script service
	 */
	@Inject
	public TransportUpdateAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService, TransportIndexAction indexAction,
			TransportDeleteAction deleteAction, ScriptService scriptService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
		this.indexAction = indexAction;
		this.deleteAction = deleteAction;
		this.scriptService = scriptService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return UpdateAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.INDEX;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction#newRequest()
	 */
	@Override
	protected UpdateRequest newRequest() {
		return new UpdateRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction#newResponse()
	 */
	@Override
	protected UpdateResponse newResponse() {
		return new UpdateResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.instance.InstanceShardOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, UpdateRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.WRITE);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.instance.InstanceShardOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, UpdateRequest request) {
		return state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, request.index());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction#retryOnFailure(java.lang.Throwable)
	 */
	@Override
	protected boolean retryOnFailure(Throwable e) {
		e = ExceptionsHelper.unwrapCause(e);
		if (e instanceof IllegalIndexShardStateException) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.instance.InstanceShardOperationRequest)
	 */
	@Override
	protected ShardIterator shards(ClusterState clusterState, UpdateRequest request) throws RebirthException {
		if (request.shardId() != -1) {
			return clusterState.routingTable().index(request.index()).shard(request.shardId()).primaryShardIt();
		}
		ShardIterator shardIterator = clusterService.operationRouting().indexShards(clusterService.state(),
				request.index(), request.type(), request.id(), request.routing());
		ShardRouting shard;
		while ((shard = shardIterator.nextOrNull()) != null) {
			if (shard.primary()) {
				return new PlainShardIterator(shardIterator.shardId(), ImmutableList.of(shard));
			}
		}
		return new PlainShardIterator(shardIterator.shardId(), ImmutableList.<ShardRouting> of());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.TransportInstanceSingleOperationAction#shardOperation(cn.com.rebirth.search.core.action.support.single.instance.InstanceShardOperationRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void shardOperation(final UpdateRequest request, final ActionListener<UpdateResponse> listener)
			throws RebirthException {
		shardOperation(request, listener, 0);
	}

	/**
	 * Shard operation.
	 *
	 * @param request the request
	 * @param listener the listener
	 * @param retryCount the retry count
	 * @throws RebirthException the rebirth exception
	 */
	protected void shardOperation(final UpdateRequest request, final ActionListener<UpdateResponse> listener,
			final int retryCount) throws RebirthException {
		IndexService indexService = indicesService.indexServiceSafe(request.index());
		IndexShard indexShard = indexService.shardSafe(request.shardId());

		long getDate = System.currentTimeMillis();
		GetResult getResult = indexShard.getService().get(
				request.type(),
				request.id(),
				new String[] { SourceFieldMapper.NAME, RoutingFieldMapper.NAME, ParentFieldMapper.NAME,
						TTLFieldMapper.NAME }, true);

		if (!getResult.exists()) {
			listener.onFailure(new DocumentMissingException(new ShardId(request.index(), request.shardId()), request
					.type(), request.id()));
			return;
		}

		if (getResult.internalSourceRef() == null) {

			listener.onFailure(new DocumentSourceMissingException(new ShardId(request.index(), request.shardId()),
					request.type(), request.id()));
			return;
		}

		Tuple<XContentType, Map<String, Object>> sourceAndContent = XContentHelper.convertToMap(getResult
				.internalSourceRef().bytes(), getResult.internalSourceRef().offset(), getResult.internalSourceRef()
				.length(), true);
		Map<String, Object> source = sourceAndContent.v2();
		Map<String, Object> ctx = new HashMap<String, Object>(2);
		ctx.put("_source", source);

		try {
			ExecutableScript script = scriptService
					.executable(request.scriptLang, request.script, request.scriptParams);
			script.setNextVar("ctx", ctx);
			script.run();

			ctx = (Map<String, Object>) script.unwrap(ctx);
		} catch (Exception e) {
			throw new RebirthIllegalArgumentException("failed to execute script", e);
		}

		String operation = (String) ctx.get("op");
		String timestamp = (String) ctx.get("_timestamp");
		Long ttl = null;
		Object fetchedTTL = ctx.get("_ttl");
		if (fetchedTTL != null) {
			if (fetchedTTL instanceof Number) {
				ttl = ((Number) fetchedTTL).longValue();
			} else {
				ttl = TimeValue.parseTimeValue((String) fetchedTTL, null).millis();
			}
		}
		source = (Map<String, Object>) ctx.get("_source");

		String routing = getResult.fields().containsKey(RoutingFieldMapper.NAME) ? getResult
				.field(RoutingFieldMapper.NAME).value().toString() : null;
		String parent = getResult.fields().containsKey(ParentFieldMapper.NAME) ? getResult
				.field(ParentFieldMapper.NAME).value().toString() : null;

		if (ttl == null) {
			ttl = getResult.fields().containsKey(TTLFieldMapper.NAME) ? (Long) getResult.field(TTLFieldMapper.NAME)
					.value() : null;
			if (ttl != null) {
				ttl = ttl - (System.currentTimeMillis() - getDate);
			}
		}

		if (operation == null || "index".equals(operation)) {
			IndexRequest indexRequest = Requests.indexRequest(request.index()).type(request.type()).id(request.id())
					.routing(routing).parent(parent).source(source, sourceAndContent.v1()).version(getResult.version())
					.replicationType(request.replicationType()).consistencyLevel(request.consistencyLevel())
					.timestamp(timestamp).ttl(ttl).percolate(request.percolate()).refresh(request.refresh());
			indexRequest.operationThreaded(false);
			indexAction.execute(indexRequest, new ActionListener<IndexResponse>() {
				@Override
				public void onResponse(IndexResponse response) {
					UpdateResponse update = new UpdateResponse(response.index(), response.type(), response.id(),
							response.version());
					update.matches(response.matches());
					listener.onResponse(update);
				}

				@Override
				public void onFailure(Throwable e) {
					e = ExceptionsHelper.unwrapCause(e);
					if (e instanceof VersionConflictEngineException) {
						if (retryCount < request.retryOnConflict()) {
							threadPool.executor(executor()).execute(new Runnable() {
								@Override
								public void run() {
									shardOperation(request, listener, retryCount + 1);
								}
							});
							return;
						}
					}
					listener.onFailure(e);
				}
			});
		} else if ("delete".equals(operation)) {
			DeleteRequest deleteRequest = Requests.deleteRequest(request.index()).type(request.type()).id(request.id())
					.routing(routing).parent(parent).version(getResult.version())
					.replicationType(request.replicationType()).consistencyLevel(request.consistencyLevel());
			deleteRequest.operationThreaded(false);
			deleteAction.execute(deleteRequest, new ActionListener<DeleteResponse>() {
				@Override
				public void onResponse(DeleteResponse response) {
					UpdateResponse update = new UpdateResponse(response.index(), response.type(), response.id(),
							response.version());
					listener.onResponse(update);
				}

				@Override
				public void onFailure(Throwable e) {
					e = ExceptionsHelper.unwrapCause(e);
					if (e instanceof VersionConflictEngineException) {
						if (retryCount < request.retryOnConflict()) {
							threadPool.executor(executor()).execute(new Runnable() {
								@Override
								public void run() {
									shardOperation(request, listener, retryCount + 1);
								}
							});
							return;
						}
					}
					listener.onFailure(e);
				}
			});
		} else if ("none".equals(operation)) {
			listener.onResponse(new UpdateResponse(getResult.index(), getResult.type(), getResult.id(), getResult
					.version()));
		} else {
			logger.warn("Used update operation [{}] for script [{}], doing nothing...", operation, request.script);
			listener.onResponse(new UpdateResponse(getResult.index(), getResult.type(), getResult.id(), getResult
					.version()));
		}
	}
}
