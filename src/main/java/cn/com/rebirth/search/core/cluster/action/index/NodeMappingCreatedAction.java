/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeMappingCreatedAction.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.action.index;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.io.stream.VoidStreamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;

/**
 * The Class NodeMappingCreatedAction.
 *
 * @author l.xue.nong
 */
public class NodeMappingCreatedAction extends AbstractComponent {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The transport service. */
	private final TransportService transportService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The listeners. */
	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	/**
	 * Instantiates a new node mapping created action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 */
	@Inject
	public NodeMappingCreatedAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterService clusterService) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;
		this.clusterService = clusterService;
		transportService.registerHandler(NodeMappingCreatedTransportHandler.ACTION,
				new NodeMappingCreatedTransportHandler());
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
	 * Node mapping created.
	 *
	 * @param response the response
	 * @throws RebirthException the rebirth exception
	 */
	public void nodeMappingCreated(final NodeMappingCreatedResponse response) throws RebirthException {
		DiscoveryNodes nodes = clusterService.state().nodes();
		if (nodes.localNodeMaster()) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					innerNodeIndexCreated(response);
				}
			});
		} else {
			transportService.sendRequest(clusterService.state().nodes().masterNode(),
					NodeMappingCreatedTransportHandler.ACTION, response, VoidTransportResponseHandler.INSTANCE_SAME);
		}
	}

	/**
	 * Inner node index created.
	 *
	 * @param response the response
	 */
	private void innerNodeIndexCreated(NodeMappingCreatedResponse response) {
		for (Listener listener : listeners) {
			listener.onNodeMappingCreated(response);
		}
	}

	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		/**
		 * On node mapping created.
		 *
		 * @param response the response
		 */
		void onNodeMappingCreated(NodeMappingCreatedResponse response);

		/**
		 * On timeout.
		 */
		void onTimeout();
	}

	/**
	 * The Class NodeMappingCreatedTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class NodeMappingCreatedTransportHandler extends BaseTransportRequestHandler<NodeMappingCreatedResponse> {

		/** The Constant ACTION. */
		static final String ACTION = "cluster/nodeMappingCreated";

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public NodeMappingCreatedResponse newInstance() {
			return new NodeMappingCreatedResponse();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(NodeMappingCreatedResponse response, TransportChannel channel) throws Exception {
			innerNodeIndexCreated(response);
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
	 * The Class NodeMappingCreatedResponse.
	 *
	 * @author l.xue.nong
	 */
	public static class NodeMappingCreatedResponse implements Streamable {

		/** The index. */
		private String index;

		/** The type. */
		private String type;

		/** The node id. */
		private String nodeId;

		/**
		 * Instantiates a new node mapping created response.
		 */
		private NodeMappingCreatedResponse() {
		}

		/**
		 * Instantiates a new node mapping created response.
		 *
		 * @param index the index
		 * @param type the type
		 * @param nodeId the node id
		 */
		public NodeMappingCreatedResponse(String index, String type, String nodeId) {
			this.index = index;
			this.type = type;
			this.nodeId = nodeId;
		}

		/**
		 * Index.
		 *
		 * @return the string
		 */
		public String index() {
			return index;
		}

		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return type;
		}

		/**
		 * Node id.
		 *
		 * @return the string
		 */
		public String nodeId() {
			return nodeId;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(index);
			out.writeUTF(type);
			out.writeUTF(nodeId);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			index = in.readUTF();
			type = in.readUTF();
			nodeId = in.readUTF();
		}
	}
}
