/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardStateAction.java 2012-7-6 14:29:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.action.shard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import jsr166y.LinkedTransferQueue;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.ImmutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.IndexShardRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationService;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;

/**
 * The Class ShardStateAction.
 *
 * @author l.xue.nong
 */
public class ShardStateAction extends AbstractComponent {

	/** The transport service. */
	private final TransportService transportService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The allocation service. */
	private final AllocationService allocationService;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The started shards queue. */
	private final BlockingQueue<ShardRouting> startedShardsQueue = new LinkedTransferQueue<ShardRouting>();

	/**
	 * Instantiates a new shard state action.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param allocationService the allocation service
	 * @param threadPool the thread pool
	 */
	@Inject
	public ShardStateAction(Settings settings, ClusterService clusterService, TransportService transportService,
			AllocationService allocationService, ThreadPool threadPool) {
		super(settings);
		this.clusterService = clusterService;
		this.transportService = transportService;
		this.allocationService = allocationService;
		this.threadPool = threadPool;

		transportService.registerHandler(ShardStartedTransportHandler.ACTION, new ShardStartedTransportHandler());
		transportService.registerHandler(ShardFailedTransportHandler.ACTION, new ShardFailedTransportHandler());
	}

	/**
	 * Shard failed.
	 *
	 * @param shardRouting the shard routing
	 * @param reason the reason
	 * @throws RebirthException the rebirth exception
	 */
	public void shardFailed(final ShardRouting shardRouting, final String reason) throws RebirthException {
		logger.warn("sending failed shard for {}, reason [{}]", shardRouting, reason);
		DiscoveryNodes nodes = clusterService.state().nodes();
		if (nodes.localNodeMaster()) {
			innerShardFailed(shardRouting, reason);
		} else {
			transportService.sendRequest(clusterService.state().nodes().masterNode(),
					ShardFailedTransportHandler.ACTION, new ShardRoutingEntry(shardRouting, reason),
					new VoidTransportResponseHandler(ThreadPool.Names.SAME) {
						@Override
						public void handleException(TransportException exp) {
							logger.warn("failed to send failed shard to [{}]", exp, clusterService.state().nodes()
									.masterNode());
						}
					});
		}
	}

	/**
	 * Shard started.
	 *
	 * @param shardRouting the shard routing
	 * @param reason the reason
	 * @throws RebirthException the rebirth exception
	 */
	public void shardStarted(final ShardRouting shardRouting, final String reason) throws RebirthException {
		if (logger.isDebugEnabled()) {
			logger.debug("sending shard started for {}, reason [{}]", shardRouting, reason);
		}
		DiscoveryNodes nodes = clusterService.state().nodes();
		if (nodes.localNodeMaster()) {
			innerShardStarted(shardRouting, reason);
		} else {
			transportService.sendRequest(clusterService.state().nodes().masterNode(),
					ShardStartedTransportHandler.ACTION, new ShardRoutingEntry(shardRouting, reason),
					new VoidTransportResponseHandler(ThreadPool.Names.SAME) {
						@Override
						public void handleException(TransportException exp) {
							logger.warn("failed to send shard started to [{}]", exp, clusterService.state().nodes()
									.masterNode());
						}
					});
		}
	}

	/**
	 * Inner shard failed.
	 *
	 * @param shardRouting the shard routing
	 * @param reason the reason
	 */
	private void innerShardFailed(final ShardRouting shardRouting, final String reason) {
		logger.warn("received shard failed for {}, reason [{}]", shardRouting, reason);
		clusterService.submitStateUpdateTask("shard-failed (" + shardRouting + "), reason [" + reason + "]",
				new ClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						if (logger.isDebugEnabled()) {
							logger.debug("Received failed shard {}, reason [{}]", shardRouting, reason);
						}
						RoutingAllocation.Result routingResult = allocationService.applyFailedShard(currentState,
								shardRouting);
						if (!routingResult.changed()) {
							return currentState;
						}
						if (logger.isDebugEnabled()) {
							logger.debug("Applying failed shard {}, reason [{}]", shardRouting, reason);
						}
						return ClusterState.newClusterStateBuilder().state(currentState).routingResult(routingResult)
								.build();
					}
				});
	}

	/**
	 * Inner shard started.
	 *
	 * @param shardRouting the shard routing
	 * @param reason the reason
	 */
	private void innerShardStarted(final ShardRouting shardRouting, final String reason) {
		if (logger.isDebugEnabled()) {
			logger.debug("received shard started for {}, reason [{}]", shardRouting, reason);
		}

		startedShardsQueue.add(shardRouting);

		clusterService.submitStateUpdateTask("shard-started (" + shardRouting + "), reason [" + reason + "]",
				new ClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {

						List<ShardRouting> shards = new ArrayList<ShardRouting>();
						startedShardsQueue.drainTo(shards);

						if (shards.isEmpty()) {
							return currentState;
						}

						RoutingTable routingTable = currentState.routingTable();

						for (int i = 0; i < shards.size(); i++) {
							ShardRouting shardRouting = shards.get(i);
							IndexRoutingTable indexRoutingTable = routingTable.index(shardRouting.index());

							if (indexRoutingTable == null) {
								shards.remove(i);
							} else {

								IndexShardRoutingTable indexShardRoutingTable = indexRoutingTable.shard(shardRouting
										.id());
								for (ShardRouting entry : indexShardRoutingTable) {
									if (shardRouting.currentNodeId().equals(entry.currentNodeId())) {

										if (entry.started()) {

											shards.remove(i);
										}
									}
								}
							}
						}

						if (shards.isEmpty()) {
							return currentState;
						}

						if (logger.isDebugEnabled()) {
							logger.debug("applying started shards {}, reason [{}]", shards, reason);
						}
						RoutingAllocation.Result routingResult = allocationService.applyStartedShards(currentState,
								shards);
						if (!routingResult.changed()) {
							return currentState;
						}
						return ClusterState.newClusterStateBuilder().state(currentState).routingResult(routingResult)
								.build();
					}
				});
	}

	/**
	 * The Class ShardFailedTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class ShardFailedTransportHandler extends BaseTransportRequestHandler<ShardRoutingEntry> {

		/** The Constant ACTION. */
		static final String ACTION = "cluster/shardFailure";

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public ShardRoutingEntry newInstance() {
			return new ShardRoutingEntry();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(ShardRoutingEntry request, TransportChannel channel) throws Exception {
			innerShardFailed(request.shardRouting, request.reason);
			channel.sendResponse(VoidStreamable.INSTANCE);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}
	}

	/**
	 * The Class ShardStartedTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class ShardStartedTransportHandler extends BaseTransportRequestHandler<ShardRoutingEntry> {

		/** The Constant ACTION. */
		static final String ACTION = "cluster/shardStarted";

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public ShardRoutingEntry newInstance() {
			return new ShardRoutingEntry();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(ShardRoutingEntry request, TransportChannel channel) throws Exception {
			innerShardStarted(request.shardRouting, request.reason);
			channel.sendResponse(VoidStreamable.INSTANCE);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}
	}

	/**
	 * The Class ShardRoutingEntry.
	 *
	 * @author l.xue.nong
	 */
	private static class ShardRoutingEntry implements Streamable {

		/** The shard routing. */
		private ShardRouting shardRouting;

		/** The reason. */
		private String reason;

		/**
		 * Instantiates a new shard routing entry.
		 */
		private ShardRoutingEntry() {
		}

		/**
		 * Instantiates a new shard routing entry.
		 *
		 * @param shardRouting the shard routing
		 * @param reason the reason
		 */
		private ShardRoutingEntry(ShardRouting shardRouting, String reason) {
			this.shardRouting = shardRouting;
			this.reason = reason;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			shardRouting = ImmutableShardRouting.readShardRoutingEntry(in);
			reason = in.readUTF();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			shardRouting.writeTo(out);
			out.writeUTF(reason);
		}
	}
}
