/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeIndexDeletedAction.java 2012-3-29 15:02:43 l.xue.nong$$
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
 * The Class NodeIndexDeletedAction.
 *
 * @author l.xue.nong
 */
public class NodeIndexDeletedAction extends AbstractComponent {

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The listeners. */
	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	
	/**
	 * Instantiates a new node index deleted action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 */
	@Inject
	public NodeIndexDeletedAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterService clusterService) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;
		this.clusterService = clusterService;
		transportService.registerHandler(NodeIndexDeletedTransportHandler.ACTION,
				new NodeIndexDeletedTransportHandler());
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
	 * Node index deleted.
	 *
	 * @param index the index
	 * @param nodeId the node id
	 * @throws SumMallSearchException the sum mall search exception
	 */
	public void nodeIndexDeleted(final String index, final String nodeId) throws RestartException {
		DiscoveryNodes nodes = clusterService.state().nodes();
		if (nodes.localNodeMaster()) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					innerNodeIndexDeleted(index, nodeId);
				}
			});
		} else {
			transportService.sendRequest(clusterService.state().nodes().masterNode(),
					NodeIndexDeletedTransportHandler.ACTION, new NodeIndexDeletedMessage(index, nodeId),
					VoidTransportResponseHandler.INSTANCE_SAME);
		}
	}

	
	/**
	 * Inner node index deleted.
	 *
	 * @param index the index
	 * @param nodeId the node id
	 */
	private void innerNodeIndexDeleted(String index, String nodeId) {
		for (Listener listener : listeners) {
			listener.onNodeIndexDeleted(index, nodeId);
		}
	}

	
	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		
		/**
		 * On node index deleted.
		 *
		 * @param index the index
		 * @param nodeId the node id
		 */
		void onNodeIndexDeleted(String index, String nodeId);
	}

	
	/**
	 * The Class NodeIndexDeletedTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class NodeIndexDeletedTransportHandler extends BaseTransportRequestHandler<NodeIndexDeletedMessage> {

		
		/** The Constant ACTION. */
		static final String ACTION = "cluster/nodeIndexDeleted";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public NodeIndexDeletedMessage newInstance() {
			return new NodeIndexDeletedMessage();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(NodeIndexDeletedMessage message, TransportChannel channel) throws Exception {
			innerNodeIndexDeleted(message.index, message.nodeId);
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
	 * The Class NodeIndexDeletedMessage.
	 *
	 * @author l.xue.nong
	 */
	private static class NodeIndexDeletedMessage implements Streamable {

		
		/** The index. */
		String index;

		
		/** The node id. */
		String nodeId;

		
		/**
		 * Instantiates a new node index deleted message.
		 */
		private NodeIndexDeletedMessage() {
		}

		
		/**
		 * Instantiates a new node index deleted message.
		 *
		 * @param index the index
		 * @param nodeId the node id
		 */
		private NodeIndexDeletedMessage(String index, String nodeId) {
			this.index = index;
			this.nodeId = nodeId;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(index);
			out.writeUTF(nodeId);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			index = in.readUTF();
			nodeId = in.readUTF();
		}
	}
}