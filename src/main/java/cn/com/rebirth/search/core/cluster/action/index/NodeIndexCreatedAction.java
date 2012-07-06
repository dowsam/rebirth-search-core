/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeIndexCreatedAction.java 2012-7-6 14:29:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.action.index;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
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
 * The Class NodeIndexCreatedAction.
 *
 * @author l.xue.nong
 */
public class NodeIndexCreatedAction extends AbstractComponent {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The transport service. */
	private final TransportService transportService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The listeners. */
	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	/**
	 * Instantiates a new node index created action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 */
	@Inject
	public NodeIndexCreatedAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterService clusterService) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;
		this.clusterService = clusterService;
		transportService.registerHandler(NodeIndexCreatedTransportHandler.ACTION,
				new NodeIndexCreatedTransportHandler());
	}

	/**
	 * Adds the.
	 *
	 * @param listener the listener
	 */
	public void add(Listener listener) {
		listeners.add(listener);
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
	 * Node index created.
	 *
	 * @param index the index
	 * @param nodeId the node id
	 * @throws RebirthException the rebirth exception
	 */
	public void nodeIndexCreated(final String index, final String nodeId) throws RebirthException {
		DiscoveryNodes nodes = clusterService.state().nodes();
		if (nodes.localNodeMaster()) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					innerNodeIndexCreated(index, nodeId);
				}
			});
		} else {
			transportService.sendRequest(clusterService.state().nodes().masterNode(),
					NodeIndexCreatedTransportHandler.ACTION, new NodeIndexCreatedMessage(index, nodeId),
					VoidTransportResponseHandler.INSTANCE_SAME);
		}
	}

	/**
	 * Inner node index created.
	 *
	 * @param index the index
	 * @param nodeId the node id
	 */
	private void innerNodeIndexCreated(String index, String nodeId) {
		for (Listener listener : listeners) {
			listener.onNodeIndexCreated(index, nodeId);
		}
	}

	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		/**
		 * On node index created.
		 *
		 * @param index the index
		 * @param nodeId the node id
		 */
		void onNodeIndexCreated(String index, String nodeId);
	}

	/**
	 * The Class NodeIndexCreatedTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class NodeIndexCreatedTransportHandler extends BaseTransportRequestHandler<NodeIndexCreatedMessage> {

		/** The Constant ACTION. */
		static final String ACTION = "cluster/nodeIndexCreated";

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public NodeIndexCreatedMessage newInstance() {
			return new NodeIndexCreatedMessage();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(NodeIndexCreatedMessage message, TransportChannel channel) throws Exception {
			innerNodeIndexCreated(message.index, message.nodeId);
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
	 * The Class NodeIndexCreatedMessage.
	 *
	 * @author l.xue.nong
	 */
	private static class NodeIndexCreatedMessage implements Streamable {

		/** The index. */
		String index;

		/** The node id. */
		String nodeId;

		/**
		 * Instantiates a new node index created message.
		 */
		private NodeIndexCreatedMessage() {
		}

		/**
		 * Instantiates a new node index created message.
		 *
		 * @param index the index
		 * @param nodeId the node id
		 */
		private NodeIndexCreatedMessage(String index, String nodeId) {
			this.index = index;
			this.nodeId = nodeId;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(index);
			out.writeUTF(nodeId);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			index = in.readUTF();
			nodeId = in.readUTF();
		}
	}
}
