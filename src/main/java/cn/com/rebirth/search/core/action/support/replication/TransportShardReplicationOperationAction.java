/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportShardReplicationOperationAction.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.replication;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.io.stream.VoidStreamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.UnavailableShardsException;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.TimeoutClusterStateListener;
import cn.com.rebirth.search.core.cluster.action.shard.ShardStateAction;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.IndexShardMissingException;
import cn.com.rebirth.search.core.index.engine.DocumentAlreadyExistsException;
import cn.com.rebirth.search.core.index.engine.VersionConflictEngineException;
import cn.com.rebirth.search.core.index.shard.IllegalIndexShardStateException;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.node.NodeClosedException;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;

/**
 * The Class TransportShardReplicationOperationAction.
 *
 * @param <Request> the generic type
 * @param <ReplicaRequest> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class TransportShardReplicationOperationAction<Request extends ShardReplicationOperationRequest, ReplicaRequest extends ActionRequest, Response extends ActionResponse>
		extends TransportAction<Request, Response> {

	/** The transport service. */
	protected final TransportService transportService;

	/** The cluster service. */
	protected final ClusterService clusterService;

	/** The indices service. */
	protected final IndicesService indicesService;

	/** The shard state action. */
	protected final ShardStateAction shardStateAction;

	/** The default replication type. */
	protected final ReplicationType defaultReplicationType;

	/** The default write consistency level. */
	protected final WriteConsistencyLevel defaultWriteConsistencyLevel;

	/** The transport action. */
	final String transportAction;

	/** The transport replica action. */
	final String transportReplicaAction;

	/** The executor. */
	final String executor;

	/** The check write consistency. */
	final boolean checkWriteConsistency;

	/**
	 * Instantiates a new transport shard replication operation action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param threadPool the thread pool
	 * @param shardStateAction the shard state action
	 */
	protected TransportShardReplicationOperationAction(Settings settings, TransportService transportService,
			ClusterService clusterService, IndicesService indicesService, ThreadPool threadPool,
			ShardStateAction shardStateAction) {
		super(settings, threadPool);
		this.transportService = transportService;
		this.clusterService = clusterService;
		this.indicesService = indicesService;
		this.shardStateAction = shardStateAction;

		this.transportAction = transportAction();
		this.transportReplicaAction = transportReplicaAction();
		this.executor = executor();
		this.checkWriteConsistency = checkWriteConsistency();

		transportService.registerHandler(transportAction, new OperationTransportHandler());
		transportService.registerHandler(transportReplicaAction, new ReplicaOperationTransportHandler());

		this.defaultReplicationType = ReplicationType.fromString(settings.get("action.replication_type", "sync"));
		this.defaultWriteConsistencyLevel = WriteConsistencyLevel.fromString(settings.get("action.write_consistency",
				"quorum"));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(Request request, ActionListener<Response> listener) {
		new AsyncShardOperationAction(request, listener).start();
	}

	/**
	 * New request instance.
	 *
	 * @return the request
	 */
	protected abstract Request newRequestInstance();

	/**
	 * New replica request instance.
	 *
	 * @return the replica request
	 */
	protected abstract ReplicaRequest newReplicaRequestInstance();

	/**
	 * New response instance.
	 *
	 * @return the response
	 */
	protected abstract Response newResponseInstance();

	/**
	 * Transport action.
	 *
	 * @return the string
	 */
	protected abstract String transportAction();

	/**
	 * Executor.
	 *
	 * @return the string
	 */
	protected abstract String executor();

	/**
	 * Shard operation on primary.
	 *
	 * @param clusterState the cluster state
	 * @param shardRequest the shard request
	 * @return the primary response
	 */
	protected abstract PrimaryResponse<Response, ReplicaRequest> shardOperationOnPrimary(ClusterState clusterState,
			PrimaryOperationRequest shardRequest);

	/**
	 * Shard operation on replica.
	 *
	 * @param shardRequest the shard request
	 */
	protected abstract void shardOperationOnReplica(ReplicaOperationRequest shardRequest);

	/**
	 * Post primary operation.
	 *
	 * @param request the request
	 * @param response the response
	 */
	protected void postPrimaryOperation(Request request, PrimaryResponse<Response, ReplicaRequest> response) {

	}

	/**
	 * Shards.
	 *
	 * @param clusterState the cluster state
	 * @param request the request
	 * @return the shard iterator
	 * @throws RebirthException the rebirth exception
	 */
	protected abstract ShardIterator shards(ClusterState clusterState, Request request) throws RebirthException;

	/**
	 * Check write consistency.
	 *
	 * @return true, if successful
	 */
	protected abstract boolean checkWriteConsistency();

	/**
	 * Check global block.
	 *
	 * @param state the state
	 * @param request the request
	 * @return the cluster block exception
	 */
	protected abstract ClusterBlockException checkGlobalBlock(ClusterState state, Request request);

	/**
	 * Check request block.
	 *
	 * @param state the state
	 * @param request the request
	 * @return the cluster block exception
	 */
	protected abstract ClusterBlockException checkRequestBlock(ClusterState state, Request request);

	/**
	 * Resolve request.
	 *
	 * @param state the state
	 * @param request the request
	 * @param listener the listener
	 * @return true, if successful
	 */
	protected boolean resolveRequest(ClusterState state, Request request, ActionListener<Response> listener) {
		request.index(state.metaData().concreteIndex(request.index()));
		return true;
	}

	/**
	 * Transport options.
	 *
	 * @return the transport request options
	 */
	protected TransportRequestOptions transportOptions() {
		return TransportRequestOptions.EMPTY;
	}

	/**
	 * Ignore replicas.
	 *
	 * @return true, if successful
	 */
	protected boolean ignoreReplicas() {
		return false;
	}

	/**
	 * Transport replica action.
	 *
	 * @return the string
	 */
	private String transportReplicaAction() {
		return transportAction() + "/replica";
	}

	/**
	 * Retry primary exception.
	 *
	 * @param e the e
	 * @return true, if successful
	 */
	protected boolean retryPrimaryException(Throwable e) {
		Throwable cause = ExceptionsHelper.unwrapCause(e);
		return cause instanceof IndexShardMissingException || cause instanceof IllegalIndexShardStateException
				|| cause instanceof IndexMissingException;
	}

	/**
	 * Ignore replica exception.
	 *
	 * @param e the e
	 * @return true, if successful
	 */
	boolean ignoreReplicaException(Throwable e) {
		Throwable cause = ExceptionsHelper.unwrapCause(e);
		if (cause instanceof IllegalIndexShardStateException) {
			return true;
		}
		if (cause instanceof IndexMissingException) {
			return true;
		}
		if (cause instanceof IndexShardMissingException) {
			return true;
		}
		if (cause instanceof ConnectTransportException) {
			return true;
		}

		if (cause instanceof VersionConflictEngineException) {
			return true;
		}

		if (cause instanceof DocumentAlreadyExistsException) {
			return true;
		}
		return false;
	}

	/**
	 * The Class OperationTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	class OperationTransportHandler extends BaseTransportRequestHandler<Request> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public Request newInstance() {
			return newRequestInstance();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final Request request, final TransportChannel channel) throws Exception {

			request.listenerThreaded(false);

			request.operationThreaded(true);
			execute(request, new ActionListener<Response>() {
				@Override
				public void onResponse(Response result) {
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
						logger.warn("Failed to send response for " + transportAction, e1);
					}
				}
			});
		}
	}

	/**
	 * The Class ReplicaOperationTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	class ReplicaOperationTransportHandler extends BaseTransportRequestHandler<ReplicaOperationRequest> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public ReplicaOperationRequest newInstance() {
			return new ReplicaOperationRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return executor;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final ReplicaOperationRequest request, final TransportChannel channel)
				throws Exception {
			shardOperationOnReplica(request);
			channel.sendResponse(VoidStreamable.INSTANCE);
		}
	}

	/**
	 * The Class PrimaryOperationRequest.
	 *
	 * @author l.xue.nong
	 */
	protected class PrimaryOperationRequest implements Streamable {

		/** The shard id. */
		public int shardId;

		/** The request. */
		public Request request;

		/**
		 * Instantiates a new primary operation request.
		 */
		public PrimaryOperationRequest() {
		}

		/**
		 * Instantiates a new primary operation request.
		 *
		 * @param shardId the shard id
		 * @param request the request
		 */
		public PrimaryOperationRequest(int shardId, Request request) {
			this.shardId = shardId;
			this.request = request;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			shardId = in.readVInt();
			request = newRequestInstance();
			request.readFrom(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeVInt(shardId);
			request.writeTo(out);
		}
	}

	/**
	 * The Class ReplicaOperationRequest.
	 *
	 * @author l.xue.nong
	 */
	protected class ReplicaOperationRequest implements Streamable {

		/** The shard id. */
		public int shardId;

		/** The request. */
		public ReplicaRequest request;

		/**
		 * Instantiates a new replica operation request.
		 */
		public ReplicaOperationRequest() {
		}

		/**
		 * Instantiates a new replica operation request.
		 *
		 * @param shardId the shard id
		 * @param request the request
		 */
		public ReplicaOperationRequest(int shardId, ReplicaRequest request) {
			this.shardId = shardId;
			this.request = request;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			shardId = in.readVInt();
			request = newReplicaRequestInstance();
			request.readFrom(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeVInt(shardId);
			request.writeTo(out);
		}
	}

	/**
	 * The Class AsyncShardOperationAction.
	 *
	 * @author l.xue.nong
	 */
	protected class AsyncShardOperationAction {

		/** The listener. */
		private final ActionListener<Response> listener;

		/** The request. */
		private final Request request;

		/** The nodes. */
		private DiscoveryNodes nodes;

		/** The shard it. */
		private ShardIterator shardIt;

		/** The primary operation started. */
		private final AtomicBoolean primaryOperationStarted = new AtomicBoolean();

		/** The replication type. */
		private final ReplicationType replicationType;

		/**
		 * Instantiates a new async shard operation action.
		 *
		 * @param request the request
		 * @param listener the listener
		 */
		AsyncShardOperationAction(Request request, ActionListener<Response> listener) {
			this.request = request;
			this.listener = listener;

			if (request.replicationType() != ReplicationType.DEFAULT) {
				replicationType = request.replicationType();
			} else {
				replicationType = defaultReplicationType;
			}
		}

		/**
		 * Start.
		 */
		public void start() {
			start(false);
		}

		/**
		 * Start.
		 *
		 * @param fromClusterEvent the from cluster event
		 * @return true, if successful
		 * @throws RebirthException the rebirth exception
		 */
		public boolean start(final boolean fromClusterEvent) throws RebirthException {
			final ClusterState clusterState = clusterService.state();
			nodes = clusterState.nodes();
			try {
				ClusterBlockException blockException = checkGlobalBlock(clusterState, request);
				if (blockException != null) {
					if (blockException.retryable()) {
						retry(fromClusterEvent, blockException);
						return false;
					} else {
						throw blockException;
					}
				}

				if (!resolveRequest(clusterState, request, listener)) {
					return true;
				}
				blockException = checkRequestBlock(clusterState, request);
				if (blockException != null) {
					if (blockException.retryable()) {
						retry(fromClusterEvent, blockException);
						return false;
					} else {
						throw blockException;
					}
				}
				shardIt = shards(clusterState, request);
				shardIt = shards(clusterState, request);
			} catch (Exception e) {
				listener.onFailure(e);
				return true;
			}

			if (shardIt.size() == 0) {
				retry(fromClusterEvent, null);
				return false;
			}

			boolean foundPrimary = false;
			ShardRouting shardX;
			while ((shardX = shardIt.nextOrNull()) != null) {
				final ShardRouting shard = shardX;

				if (!shard.primary()) {
					continue;
				}
				if (!shard.active() || !nodes.nodeExists(shard.currentNodeId())) {
					retry(fromClusterEvent, null);
					return false;
				}

				if (checkWriteConsistency) {
					WriteConsistencyLevel consistencyLevel = defaultWriteConsistencyLevel;
					if (request.consistencyLevel() != WriteConsistencyLevel.DEFAULT) {
						consistencyLevel = request.consistencyLevel();
					}
					int requiredNumber = 1;
					if (consistencyLevel == WriteConsistencyLevel.QUORUM && shardIt.size() > 2) {

						requiredNumber = (shardIt.size() / 2) + 1;
					} else if (consistencyLevel == WriteConsistencyLevel.ALL) {
						requiredNumber = shardIt.size();
					}

					if (shardIt.sizeActive() < requiredNumber) {
						retry(fromClusterEvent, null);
						return false;
					}
				}

				if (!primaryOperationStarted.compareAndSet(false, true)) {
					return true;
				}

				foundPrimary = true;
				if (shard.currentNodeId().equals(nodes.localNodeId())) {
					if (request.operationThreaded()) {
						request.beforeLocalFork();
						threadPool.executor(executor).execute(new Runnable() {
							@Override
							public void run() {
								performOnPrimary(shard.id(), fromClusterEvent, shard, clusterState);
							}
						});
					} else {
						performOnPrimary(shard.id(), fromClusterEvent, shard, clusterState);
					}
				} else {
					DiscoveryNode node = nodes.get(shard.currentNodeId());
					transportService.sendRequest(node, transportAction, request, transportOptions(),
							new BaseTransportResponseHandler<Response>() {

								@Override
								public Response newInstance() {
									return newResponseInstance();
								}

								@Override
								public String executor() {
									return ThreadPool.Names.SAME;
								}

								@Override
								public void handleResponse(Response response) {
									listener.onResponse(response);
								}

								@Override
								public void handleException(TransportException exp) {

									if (exp.unwrapCause() instanceof ConnectTransportException
											|| exp.unwrapCause() instanceof NodeClosedException
											|| exp.unwrapCause() instanceof IllegalIndexShardStateException) {
										primaryOperationStarted.set(false);

										retry(false, null);
									} else {
										listener.onFailure(exp);
									}
								}
							});
				}
				break;
			}

			if (!foundPrimary) {
				retry(fromClusterEvent, null);
				return false;
			}
			return true;
		}

		/**
		 * Retry.
		 *
		 * @param fromClusterEvent the from cluster event
		 * @param failure the failure
		 */
		void retry(boolean fromClusterEvent, @Nullable final Throwable failure) {
			if (!fromClusterEvent) {

				request.beforeLocalFork();
				request.operationThreaded(true);
				clusterService.add(request.timeout(), new TimeoutClusterStateListener() {
					@Override
					public void postAdded() {
						if (start(true)) {

							clusterService.remove(this);
						}
					}

					@Override
					public void onClose() {
						clusterService.remove(this);
						listener.onFailure(new NodeClosedException(nodes.localNode()));
					}

					@Override
					public void clusterChanged(ClusterChangedEvent event) {
						if (start(true)) {

							clusterService.remove(this);
						}
					}

					@Override
					public void onTimeout(TimeValue timeValue) {

						if (start(true)) {
							clusterService.remove(this);
							return;
						}
						clusterService.remove(this);
						Throwable listenerFailure = failure;
						if (listenerFailure == null) {
							if (shardIt == null) {
								listenerFailure = new UnavailableShardsException(null,
										"no available shards: Timeout waiting for [" + timeValue + "], request: "
												+ request.toString());
							} else {
								listenerFailure = new UnavailableShardsException(shardIt.shardId(), "["
										+ shardIt.size() + "] shardIt, [" + shardIt.sizeActive()
										+ "] active : Timeout waiting for [" + timeValue + "], request: "
										+ request.toString());
							}
						}
						listener.onFailure(listenerFailure);
					}
				});
			}
		}

		/**
		 * Perform on primary.
		 *
		 * @param primaryShardId the primary shard id
		 * @param fromDiscoveryListener the from discovery listener
		 * @param shard the shard
		 * @param clusterState the cluster state
		 */
		void performOnPrimary(int primaryShardId, boolean fromDiscoveryListener, final ShardRouting shard,
				ClusterState clusterState) {
			try {
				PrimaryResponse<Response, ReplicaRequest> response = shardOperationOnPrimary(clusterState,
						new PrimaryOperationRequest(primaryShardId, request));
				performReplicas(response);
			} catch (Exception e) {

				if (retryPrimaryException(e)) {
					retry(fromDiscoveryListener, null);
					return;
				}
				listener.onFailure(e);
			}
		}

		/**
		 * Perform replicas.
		 *
		 * @param response the response
		 */
		void performReplicas(final PrimaryResponse<Response, ReplicaRequest> response) {
			if (ignoreReplicas() || shardIt.size() == 1) {
				postPrimaryOperation(request, response);
				listener.onResponse(response.response());
				return;
			}

			int replicaCounter = shardIt.assignedReplicasIncludingRelocating();

			if (replicaCounter == 0) {
				postPrimaryOperation(request, response);
				listener.onResponse(response.response());
				return;
			}

			if (replicationType == ReplicationType.ASYNC) {
				postPrimaryOperation(request, response);

				listener.onResponse(response.response());

				replicaCounter = Integer.MIN_VALUE;
			}

			replicaCounter++;

			AtomicInteger counter = new AtomicInteger(replicaCounter);
			shardIt.reset();
			ShardRouting shard;
			while ((shard = shardIt.nextOrNull()) != null) {

				if (shard.unassigned()) {
					continue;
				}

				boolean doOnlyOnRelocating = false;
				if (shard.primary()) {
					if (shard.relocating()) {
						doOnlyOnRelocating = true;
					} else {
						continue;
					}
				}

				if (!doOnlyOnRelocating) {
					performOnReplica(response, counter, shard, shard.currentNodeId());
				}
				if (shard.relocating()) {
					performOnReplica(response, counter, shard, shard.relocatingNodeId());
				}
			}

			postPrimaryOperation(request, response);

			if (counter.decrementAndGet() == 0) {
				listener.onResponse(response.response());
			}
		}

		/**
		 * Perform on replica.
		 *
		 * @param response the response
		 * @param counter the counter
		 * @param shard the shard
		 * @param nodeId the node id
		 */
		void performOnReplica(final PrimaryResponse<Response, ReplicaRequest> response, final AtomicInteger counter,
				final ShardRouting shard, String nodeId) {

			if (!nodes.nodeExists(nodeId)) {
				if (counter.decrementAndGet() == 0) {
					listener.onResponse(response.response());
				}
				return;
			}

			final ReplicaOperationRequest shardRequest = new ReplicaOperationRequest(shardIt.shardId().id(),
					response.replicaRequest());
			if (!nodeId.equals(nodes.localNodeId())) {
				DiscoveryNode node = nodes.get(nodeId);
				transportService.sendRequest(node, transportReplicaAction, shardRequest, transportOptions(),
						new VoidTransportResponseHandler(ThreadPool.Names.SAME) {
							@Override
							public void handleResponse(VoidStreamable vResponse) {
								finishIfPossible();
							}

							@Override
							public void handleException(TransportException exp) {
								if (!ignoreReplicaException(exp.unwrapCause())) {
									logger.warn(
											"Failed to perform " + transportAction + " on replica " + shardIt.shardId(),
											exp);
									shardStateAction.shardFailed(shard, "Failed to perform [" + transportAction
											+ "] on replica, message [" + ExceptionsHelper.detailedMessage(exp) + "]");
								}
								finishIfPossible();
							}

							private void finishIfPossible() {
								if (counter.decrementAndGet() == 0) {
									listener.onResponse(response.response());
								}
							}
						});
			} else {
				if (request.operationThreaded()) {
					request.beforeLocalFork();
					threadPool.executor(executor).execute(new Runnable() {
						@Override
						public void run() {
							try {
								shardOperationOnReplica(shardRequest);
							} catch (Exception e) {
								if (!ignoreReplicaException(e)) {
									logger.warn(
											"Failed to perform " + transportAction + " on replica " + shardIt.shardId(),
											e);
									shardStateAction.shardFailed(shard, "Failed to perform [" + transportAction
											+ "] on replica, message [" + ExceptionsHelper.detailedMessage(e) + "]");
								}
							}
							if (counter.decrementAndGet() == 0) {
								listener.onResponse(response.response());
							}
						}
					});
				} else {
					try {
						shardOperationOnReplica(shardRequest);
					} catch (Exception e) {
						if (!ignoreReplicaException(e)) {
							logger.warn("Failed to perform " + transportAction + " on replica" + shardIt.shardId(), e);
							shardStateAction.shardFailed(shard, "Failed to perform [" + transportAction
									+ "] on replica, message [" + ExceptionsHelper.detailedMessage(e) + "]");
						}
					}
					if (counter.decrementAndGet() == 0) {
						listener.onResponse(response.response());
					}
				}
			}
		}
	}

	/**
	 * The Class PrimaryResponse.
	 *
	 * @param <Response> the generic type
	 * @param <ReplicaRequest> the generic type
	 * @author l.xue.nong
	 */
	public static class PrimaryResponse<Response, ReplicaRequest> {

		/** The replica request. */
		private final ReplicaRequest replicaRequest;

		/** The response. */
		private final Response response;

		/** The payload. */
		private final Object payload;

		/**
		 * Instantiates a new primary response.
		 *
		 * @param replicaRequest the replica request
		 * @param response the response
		 * @param payload the payload
		 */
		public PrimaryResponse(ReplicaRequest replicaRequest, Response response, Object payload) {
			this.replicaRequest = replicaRequest;
			this.response = response;
			this.payload = payload;
		}

		/**
		 * Replica request.
		 *
		 * @return the replica request
		 */
		public ReplicaRequest replicaRequest() {
			return this.replicaRequest;
		}

		/**
		 * Response.
		 *
		 * @return the response
		 */
		public Response response() {
			return response;
		}

		/**
		 * Payload.
		 *
		 * @return the object
		 */
		public Object payload() {
			return payload;
		}
	}
}
