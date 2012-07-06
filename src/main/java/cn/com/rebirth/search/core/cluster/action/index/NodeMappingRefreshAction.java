/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeMappingRefreshAction.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.action.index;

import java.io.IOException;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataMappingService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;

/**
 * The Class NodeMappingRefreshAction.
 *
 * @author l.xue.nong
 */
public class NodeMappingRefreshAction extends AbstractComponent {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The transport service. */
	private final TransportService transportService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The meta data mapping service. */
	private final MetaDataMappingService metaDataMappingService;

	/**
	 * Instantiates a new node mapping refresh action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param metaDataMappingService the meta data mapping service
	 */
	@Inject
	public NodeMappingRefreshAction(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterService clusterService, MetaDataMappingService metaDataMappingService) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;
		this.clusterService = clusterService;
		this.metaDataMappingService = metaDataMappingService;
		transportService.registerHandler(NodeMappingRefreshTransportHandler.ACTION,
				new NodeMappingRefreshTransportHandler());
	}

	/**
	 * Node mapping refresh.
	 *
	 * @param request the request
	 * @throws RebirthException the rebirth exception
	 */
	public void nodeMappingRefresh(final NodeMappingRefreshRequest request) throws RebirthException {
		DiscoveryNodes nodes = clusterService.state().nodes();
		if (nodes.localNodeMaster()) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					innerMappingRefresh(request);
				}
			});
		} else {
			transportService.sendRequest(clusterService.state().nodes().masterNode(),
					NodeMappingRefreshTransportHandler.ACTION, request, VoidTransportResponseHandler.INSTANCE_SAME);
		}
	}

	/**
	 * Inner mapping refresh.
	 *
	 * @param request the request
	 */
	private void innerMappingRefresh(NodeMappingRefreshRequest request) {
		metaDataMappingService.refreshMapping(request.index(), request.types());
	}

	/**
	 * The Class NodeMappingRefreshTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class NodeMappingRefreshTransportHandler extends BaseTransportRequestHandler<NodeMappingRefreshRequest> {

		/** The Constant ACTION. */
		static final String ACTION = "cluster/nodeMappingRefresh";

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public NodeMappingRefreshRequest newInstance() {
			return new NodeMappingRefreshRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(NodeMappingRefreshRequest request, TransportChannel channel) throws Exception {
			innerMappingRefresh(request);
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
	 * The Class NodeMappingRefreshRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class NodeMappingRefreshRequest implements Streamable {

		/** The index. */
		private String index;

		/** The types. */
		private String[] types;

		/** The node id. */
		private String nodeId;

		/**
		 * Instantiates a new node mapping refresh request.
		 */
		private NodeMappingRefreshRequest() {
		}

		/**
		 * Instantiates a new node mapping refresh request.
		 *
		 * @param index the index
		 * @param types the types
		 * @param nodeId the node id
		 */
		public NodeMappingRefreshRequest(String index, String[] types, String nodeId) {
			this.index = index;
			this.types = types;
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
		 * Types.
		 *
		 * @return the string[]
		 */
		public String[] types() {
			return types;
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
			out.writeVInt(types.length);
			for (String type : types) {
				out.writeUTF(type);
			}
			out.writeUTF(nodeId);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			index = in.readUTF();
			types = new String[in.readVInt()];
			for (int i = 0; i < types.length; i++) {
				types[i] = in.readUTF();
			}
			nodeId = in.readUTF();
		}
	}
}
