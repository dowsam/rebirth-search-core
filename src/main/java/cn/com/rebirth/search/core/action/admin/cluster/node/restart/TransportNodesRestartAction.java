/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportNodesRestartAction.java 2012-7-6 14:30:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.restart;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest;
import cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.node.Node;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportNodesRestartAction.
 *
 * @author l.xue.nong
 */
public class TransportNodesRestartAction
		extends
		TransportNodesOperationAction<NodesRestartRequest, NodesRestartResponse, TransportNodesRestartAction.NodeRestartRequest, NodesRestartResponse.NodeRestartResponse> {

	/** The node. */
	private final Node node;

	/** The disabled. */
	private final boolean disabled;

	/** The restart requested. */
	private AtomicBoolean restartRequested = new AtomicBoolean();

	/**
	 * Instantiates a new transport nodes restart action.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param node the node
	 */
	@Inject
	public TransportNodesRestartAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService, Node node) {
		super(settings, clusterName, threadPool, clusterService, transportService);
		this.node = node;
		disabled = componentSettings.getAsBoolean("disabled", false);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#doExecute(cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(NodesRestartRequest nodesRestartRequest, ActionListener<NodesRestartResponse> listener) {
		listener.onFailure(new RebirthIllegalStateException("restart is disabled (for now) ...."));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return NodesRestartAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newResponse(cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest, java.util.concurrent.atomic.AtomicReferenceArray)
	 */
	@Override
	protected NodesRestartResponse newResponse(NodesRestartRequest nodesShutdownRequest, AtomicReferenceArray responses) {
		final List<NodesRestartResponse.NodeRestartResponse> nodeRestartResponses = newArrayList();
		for (int i = 0; i < responses.length(); i++) {
			Object resp = responses.get(i);
			if (resp instanceof NodesRestartResponse.NodeRestartResponse) {
				nodeRestartResponses.add((NodesRestartResponse.NodeRestartResponse) resp);
			}
		}
		return new NodesRestartResponse(clusterName,
				nodeRestartResponses.toArray(new NodesRestartResponse.NodeRestartResponse[nodeRestartResponses.size()]));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newRequest()
	 */
	@Override
	protected NodesRestartRequest newRequest() {
		return new NodesRestartRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest()
	 */
	@Override
	protected NodeRestartRequest newNodeRequest() {
		return new NodeRestartRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeRequest(java.lang.String, cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest)
	 */
	@Override
	protected NodeRestartRequest newNodeRequest(String nodeId, NodesRestartRequest request) {
		return new NodeRestartRequest(nodeId, request.delay);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#newNodeResponse()
	 */
	@Override
	protected NodesRestartResponse.NodeRestartResponse newNodeResponse() {
		return new NodesRestartResponse.NodeRestartResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#nodeOperation(cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest)
	 */
	@Override
	protected NodesRestartResponse.NodeRestartResponse nodeOperation(NodeRestartRequest request)
			throws RebirthException {
		if (disabled) {
			throw new RebirthIllegalStateException("Restart is disabled");
		}
		if (!restartRequested.compareAndSet(false, true)) {
			return new NodesRestartResponse.NodeRestartResponse(clusterService.state().nodes().localNode());
		}
		logger.info("Restarting in [{}]", request.delay);
		threadPool.schedule(request.delay, ThreadPool.Names.GENERIC, new Runnable() {
			@Override
			public void run() {
				boolean restartWithWrapper = false;
				if (System.getProperty("summallsearch-service") != null) {
					try {
						Class wrapperManager = settings.getClassLoader().loadClass(
								"org.tanukisoftware.wrapper.WrapperManager");
						logger.info("Initiating requested restart (using service)");
						wrapperManager.getMethod("restartAndReturn").invoke(null);
						restartWithWrapper = true;
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				if (!restartWithWrapper) {
					logger.info("Initiating requested restart");
					try {
						node.stop();
						node.start();
					} catch (Exception e) {
						logger.warn("Failed to restart", e);
					} finally {
						restartRequested.set(false);
					}
				}
			}
		});
		return new NodesRestartResponse.NodeRestartResponse(clusterService.state().nodes().localNode());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.TransportNodesOperationAction#accumulateExceptions()
	 */
	@Override
	protected boolean accumulateExceptions() {
		return false;
	}

	/**
	 * The Class NodeRestartRequest.
	 *
	 * @author l.xue.nong
	 */
	protected static class NodeRestartRequest extends NodeOperationRequest {

		/** The delay. */
		TimeValue delay;

		/**
		 * Instantiates a new node restart request.
		 */
		private NodeRestartRequest() {
		}

		/**
		 * Instantiates a new node restart request.
		 *
		 * @param nodeId the node id
		 * @param delay the delay
		 */
		private NodeRestartRequest(String nodeId, TimeValue delay) {
			super(nodeId);
			this.delay = delay;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			delay = TimeValue.readTimeValue(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			delay.writeTo(out);
		}
	}
}