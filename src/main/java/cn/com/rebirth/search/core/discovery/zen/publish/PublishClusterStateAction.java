/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PublishClusterStateAction.java 2012-3-29 15:02:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery.zen.publish;

import java.io.IOException;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.io.stream.CachedStreamInput;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.search.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.search.commons.io.stream.HandlesStreamOutput;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.discovery.zen.DiscoveryNodesProvider;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;


/**
 * The Class PublishClusterStateAction.
 *
 * @author l.xue.nong
 */
public class PublishClusterStateAction extends AbstractComponent {

	
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
		void onNewClusterState(ClusterState clusterState);
	}

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The nodes provider. */
	private final DiscoveryNodesProvider nodesProvider;

	
	/** The listener. */
	private final NewClusterStateListener listener;

	
	/**
	 * Instantiates a new publish cluster state action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param nodesProvider the nodes provider
	 * @param listener the listener
	 */
	public PublishClusterStateAction(Settings settings, TransportService transportService,
			DiscoveryNodesProvider nodesProvider, NewClusterStateListener listener) {
		super(settings);
		this.transportService = transportService;
		this.nodesProvider = nodesProvider;
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
	public void publish(ClusterState clusterState) {
		DiscoveryNode localNode = nodesProvider.nodes().localNode();

		
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		byte[] clusterStateInBytes;
		try {
			HandlesStreamOutput stream = cachedEntry.cachedHandlesLzfBytes();
			ClusterState.Builder.writeTo(clusterState, stream);
			stream.flush();
			clusterStateInBytes = cachedEntry.bytes().copiedByteArray();
		} catch (Exception e) {
			logger.warn("failed to serialize cluster_state before publishing it to nodes", e);
			return;
		} finally {
			CachedStreamOutput.pushEntry(cachedEntry);
		}

		for (final DiscoveryNode node : clusterState.nodes()) {
			if (node.equals(localNode)) {
				
				continue;
			}
			transportService.sendRequest(node, PublishClusterStateRequestHandler.ACTION,
					new PublishClusterStateRequest(clusterStateInBytes), TransportRequestOptions.options()
							.withHighType().withCompress(false), 

					new VoidTransportResponseHandler(ThreadPool.Names.SAME) {
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
	class PublishClusterStateRequest implements Streamable {

		
		/** The cluster state in bytes. */
		BytesHolder clusterStateInBytes;

		
		/**
		 * Instantiates a new publish cluster state request.
		 */
		private PublishClusterStateRequest() {
		}

		
		/**
		 * Instantiates a new publish cluster state request.
		 *
		 * @param clusterStateInBytes the cluster state in bytes
		 */
		private PublishClusterStateRequest(byte[] clusterStateInBytes) {
			this.clusterStateInBytes = new BytesHolder(clusterStateInBytes);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			clusterStateInBytes = in.readBytesReference();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeBytesHolder(clusterStateInBytes);
		}
	}

	
	/**
	 * The Class PublishClusterStateRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	private class PublishClusterStateRequestHandler extends BaseTransportRequestHandler<PublishClusterStateRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "discovery/zen/publish";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public PublishClusterStateRequest newInstance() {
			return new PublishClusterStateRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(PublishClusterStateRequest request, TransportChannel channel) throws Exception {
			StreamInput in = CachedStreamInput.cachedHandlesLzf(new BytesStreamInput(request.clusterStateInBytes
					.bytes(), request.clusterStateInBytes.offset(), request.clusterStateInBytes.length(), false));
			ClusterState clusterState = ClusterState.Builder.readFrom(in, nodesProvider.nodes().localNode());
			listener.onNewClusterState(clusterState);
			channel.sendResponse(VoidStreamable.INSTANCE);
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
