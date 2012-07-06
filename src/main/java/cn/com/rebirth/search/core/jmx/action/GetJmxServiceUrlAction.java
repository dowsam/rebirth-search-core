/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GetJmxServiceUrlAction.java 2012-7-6 14:28:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.jmx.action;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.stream.StringStreamable;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.jmx.JmxService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.FutureTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class GetJmxServiceUrlAction.
 *
 * @author l.xue.nong
 */
public class GetJmxServiceUrlAction extends AbstractComponent {

	/** The jmx service. */
	private final JmxService jmxService;

	/** The transport service. */
	private final TransportService transportService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/**
	 * Instantiates a new gets the jmx service url action.
	 *
	 * @param settings the settings
	 * @param jmxService the jmx service
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 */
	@Inject
	public GetJmxServiceUrlAction(Settings settings, JmxService jmxService, TransportService transportService,
			ClusterService clusterService) {
		super(settings);
		this.jmxService = jmxService;
		this.transportService = transportService;
		this.clusterService = clusterService;

		transportService.registerHandler(GetJmxServiceUrlTransportHandler.ACTION,
				new GetJmxServiceUrlTransportHandler());
	}

	/**
	 * Obtain publish url.
	 *
	 * @param node the node
	 * @return the string
	 * @throws RebirthException the rebirth exception
	 */
	public String obtainPublishUrl(final DiscoveryNode node) throws RebirthException {
		if (clusterService.state().nodes().localNodeId().equals(node.id())) {
			return jmxService.publishUrl();
		} else {
			return transportService
					.submitRequest(node, GetJmxServiceUrlTransportHandler.ACTION, VoidStreamable.INSTANCE,
							new FutureTransportResponseHandler<StringStreamable>() {
								@Override
								public StringStreamable newInstance() {
									return new StringStreamable();
								}
							}).txGet().get();
		}
	}

	/**
	 * The Class GetJmxServiceUrlTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class GetJmxServiceUrlTransportHandler extends BaseTransportRequestHandler<VoidStreamable> {

		/** The Constant ACTION. */
		static final String ACTION = "jmx/publishUrl";

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public VoidStreamable newInstance() {
			return VoidStreamable.INSTANCE;
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
		public void messageReceived(VoidStreamable request, TransportChannel channel) throws Exception {
			channel.sendResponse(new StringStreamable(jmxService.publishUrl()));
		}
	}
}