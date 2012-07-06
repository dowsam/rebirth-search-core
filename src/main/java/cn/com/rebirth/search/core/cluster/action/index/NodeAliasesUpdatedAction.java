/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeAliasesUpdatedAction.java 2012-3-29 15:01:54 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.action.index;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;


/**
 * The Class NodeAliasesUpdatedAction.
 *
 * @author l.xue.nong
 */
public class NodeAliasesUpdatedAction extends AbstractComponent {

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The listeners. */
	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	
	/**
	 * Instantiates a new node aliases updated action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 */
	@Inject
	public NodeAliasesUpdatedAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterService clusterService) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;
		this.clusterService = clusterService;
		transportService.registerHandler(NodeAliasesUpdatedTransportHandler.ACTION,
				new NodeAliasesUpdatedTransportHandler());
	}

	
	/**
	 * Adds the.
	 *
	 * @param listener the listener
	 * @param timeout the timeout
	 */
	public void add(final Listener listener, TimeValue timeout) {
		listeners.add(listener);
		threadPool.schedule(timeout, ThreadPool.Names.GENERIC, new Runnable() {
			@Override
			public void run() {
				boolean removed = listeners.remove(listener);
				if (removed) {
					listener.onTimeout();
				}
			}
		});
	}

	
	/**
	 * Removes the.
	 *
	 * @param listener the listener
	 */
	public void remove(Listener listener) {
		listeners.remove(listener);
	}

	
	/**
	 * Node aliases updated.
	 *
	 * @param response the response
	 * @throws SumMallSearchException the sum mall search exception
	 */
	public void nodeAliasesUpdated(final NodeAliasesUpdatedResponse response) throws RestartException {
		DiscoveryNodes nodes = clusterService.state().nodes();
		if (nodes.localNodeMaster()) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					innerNodeAliasesUpdated(response);
				}
			});
		} else {
			transportService.sendRequest(clusterService.state().nodes().masterNode(),
					NodeAliasesUpdatedTransportHandler.ACTION, response, VoidTransportResponseHandler.INSTANCE_SAME);
		}
	}

	
	/**
	 * Inner node aliases updated.
	 *
	 * @param response the response
	 */
	private void innerNodeAliasesUpdated(NodeAliasesUpdatedResponse response) {
		for (Listener listener : listeners) {
			listener.onAliasesUpdated(response);
		}
	}

	
	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		
		/**
		 * On aliases updated.
		 *
		 * @param response the response
		 */
		void onAliasesUpdated(NodeAliasesUpdatedResponse response);

		
		/**
		 * On timeout.
		 */
		void onTimeout();
	}

	
	/**
	 * The Class NodeAliasesUpdatedTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class NodeAliasesUpdatedTransportHandler extends BaseTransportRequestHandler<NodeAliasesUpdatedResponse> {

		
		/** The Constant ACTION. */
		static final String ACTION = "cluster/nodeAliasesUpdated";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public NodeAliasesUpdatedResponse newInstance() {
			return new NodeAliasesUpdatedResponse();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(NodeAliasesUpdatedResponse response, TransportChannel channel) throws Exception {
			innerNodeAliasesUpdated(response);
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

	
	/**
	 * The Class NodeAliasesUpdatedResponse.
	 *
	 * @author l.xue.nong
	 */
	public static class NodeAliasesUpdatedResponse implements Streamable {

		
		/** The node id. */
		private String nodeId;

		
		/** The version. */
		private long version;

		
		/**
		 * Instantiates a new node aliases updated response.
		 */
		private NodeAliasesUpdatedResponse() {
		}

		
		/**
		 * Instantiates a new node aliases updated response.
		 *
		 * @param nodeId the node id
		 * @param version the version
		 */
		public NodeAliasesUpdatedResponse(String nodeId, long version) {
			this.nodeId = nodeId;
			this.version = version;
		}

		
		/**
		 * Node id.
		 *
		 * @return the string
		 */
		public String nodeId() {
			return nodeId;
		}

		
		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return version;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(nodeId);
			out.writeLong(version);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			nodeId = in.readUTF();
			version = in.readLong();
		}
	}
}
