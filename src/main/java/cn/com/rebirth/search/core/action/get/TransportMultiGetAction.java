/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportMultiGetAction.java 2012-3-29 15:02:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.get;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportMultiGetAction.
 *
 * @author l.xue.nong
 */
public class TransportMultiGetAction extends TransportAction<MultiGetRequest, MultiGetResponse> {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The shard action. */
	private final TransportShardMultiGetAction shardAction;

	/**
	 * Instantiates a new transport multi get action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param shardAction the shard action
	 */
	@Inject
	public TransportMultiGetAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterService clusterService, TransportShardMultiGetAction shardAction) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.shardAction = shardAction;

		transportService.registerHandler(MultiGetAction.NAME, new TransportHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.TransportAction#doExecute(cn.com.summall.search.core.action.ActionRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(final MultiGetRequest request, final ActionListener<MultiGetResponse> listener) {
		ClusterState clusterState = clusterService.state();

		clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.READ);

		Map<ShardId, MultiGetShardRequest> shardRequests = new HashMap<ShardId, MultiGetShardRequest>();
		for (int i = 0; i < request.items.size(); i++) {
			MultiGetRequest.Item item = request.items.get(i);
			item.routing(clusterState.metaData().resolveIndexRouting(item.routing(), item.index()));
			item.index(clusterState.metaData().concreteIndex(item.index()));
			ShardId shardId = clusterService.operationRouting()
					.getShards(clusterState, item.index(), item.type(), item.id(), item.routing(), null).shardId();
			MultiGetShardRequest shardRequest = shardRequests.get(shardId);
			if (shardRequest == null) {
				shardRequest = new MultiGetShardRequest(shardId.index().name(), shardId.id());
				shardRequest.preference(request.preference);
				shardRequest.realtime(request.realtime);
				shardRequest.refresh(request.refresh);

				shardRequests.put(shardId, shardRequest);
			}
			shardRequest.add(i, item.type(), item.id(), item.fields());
		}

		final MultiGetItemResponse[] responses = new MultiGetItemResponse[request.items.size()];
		final AtomicInteger counter = new AtomicInteger(shardRequests.size());

		for (final MultiGetShardRequest shardRequest : shardRequests.values()) {
			shardAction.execute(shardRequest, new ActionListener<MultiGetShardResponse>() {
				@Override
				public void onResponse(MultiGetShardResponse response) {
					synchronized (responses) {
						for (int i = 0; i < response.locations.size(); i++) {
							responses[response.locations.get(i)] = new MultiGetItemResponse(response.responses.get(i),
									response.failures.get(i));
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
						for (int i = 0; i < shardRequest.locations.size(); i++) {
							responses[shardRequest.locations.get(i)] = new MultiGetItemResponse(null,
									new MultiGetResponse.Failure(shardRequest.index(), shardRequest.types.get(i),
											shardRequest.ids.get(i), message));
						}
					}
					if (counter.decrementAndGet() == 0) {
						finishHim();
					}
				}

				private void finishHim() {
					listener.onResponse(new MultiGetResponse(responses));
				}
			});
		}
	}

	/**
	 * The Class TransportHandler.
	 *
	 * @author l.xue.nong
	 */
	class TransportHandler extends BaseTransportRequestHandler<MultiGetRequest> {

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public MultiGetRequest newInstance() {
			return new MultiGetRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final MultiGetRequest request, final TransportChannel channel) throws Exception {
			
			request.listenerThreaded(false);
			execute(request, new ActionListener<MultiGetResponse>() {
				@Override
				public void onResponse(MultiGetResponse response) {
					try {
						channel.sendResponse(response);
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(e);
					} catch (Exception e1) {
						logger.warn("Failed to send error response for action [" + MultiGetAction.NAME
								+ "] and request [" + request + "]", e1);
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