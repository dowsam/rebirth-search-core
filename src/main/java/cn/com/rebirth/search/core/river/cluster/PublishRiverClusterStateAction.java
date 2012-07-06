/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PublishRiverClusterStateAction.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river.cluster;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;

/**
 * The Class PublishRiverClusterStateAction.
 *
 * @author l.xue.nong
 */
public class PublishRiverClusterStateAction extends AbstractComponent {

	/**
	 * The listener interface for receiving newClusterState events.
	 * The class that is interested in processing a newClusterState
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addNewClusterStateListener<code> method. When
	 * the newClusterState event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see NewClusterStateEvent
	 */
	public static interface NewClusterStateListener {

		/**
		 * On new cluster state.
		 *
		 * @param clusterState the cluster state
		 */
		void onNewClusterState(RiverClusterState clusterState);
	}

	/** The transport service. */
	private final TransportService transportService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The listener. */
	private final NewClusterStateListener listener;

	/**
	 * Instantiates a new publish river cluster state action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param listener the listener
	 */
	public PublishRiverClusterStateAction(Settings settings, TransportService transportService,
			ClusterService clusterService, NewClusterStateListener listener) {
		super(settings);
		this.transportService = transportService;
		this.clusterService = clusterService;
		this.listener = listener;
		transportService.registerHandler(PublishClusterStateRequestHandler.ACTION,
				new PublishClusterStateRequestHandler());
	}

	/**
	 * Close.
	 */
	public void close() {
		transportService.removeHandler(PublishClusterStateRequestHandler.ACTION);
	}

	/**
	 * Publish.
	 *
	 * @param clusterState the cluster state
	 */
	public void publish(RiverClusterState clusterState) {
		final DiscoveryNodes discoNodes = clusterService.state().nodes();
		for (final DiscoveryNode node : discoNodes) {
			if (node.equals(discoNodes.localNode())) {

				continue;
			}

			if (!node.masterNode() && !RiverNodeHelper.isRiverNode(node)) {
				continue;
			}

			transportService.sendRequest(node, PublishClusterStateRequestHandler.ACTION,
					new PublishClusterStateRequest(clusterState), new VoidTransportResponseHandler(
							ThreadPool.Names.SAME) {
						@Override
						public void handleException(TransportException exp) {
							logger.debug("failed to send cluster state to [{}], should be detected as failed soon...",
									exp, node);
						}
					});
		}
	}

	/**
	 * The Class PublishClusterStateRequest.
	 *
	 * @author l.xue.nong
	 */
	private class PublishClusterStateRequest implements Streamable {

		/** The cluster state. */
		private RiverClusterState clusterState;

		/**
		 * Instantiates a new publish cluster state request.
		 */
		private PublishClusterStateRequest() {
		}

		/**
		 * Instantiates a new publish cluster state request.
		 *
		 * @param clusterState the cluster state
		 */
		private PublishClusterStateRequest(RiverClusterState clusterState) {
			this.clusterState = clusterState;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			clusterState = RiverClusterState.Builder.readFrom(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			RiverClusterState.Builder.writeTo(clusterState, out);
		}
	}

	/**
	 * The Class PublishClusterStateRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	private class PublishClusterStateRequestHandler extends BaseTransportRequestHandler<PublishClusterStateRequest> {

		/** The Constant ACTION. */
		static final String ACTION = "river/state/publish";

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public PublishClusterStateRequest newInstance() {
			return new PublishClusterStateRequest();
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
		public void messageReceived(PublishClusterStateRequest request, TransportChannel channel) throws Exception {
			listener.onNewClusterState(request.clusterState);
			channel.sendResponse(VoidStreamable.INSTANCE);
		}
	}
}
