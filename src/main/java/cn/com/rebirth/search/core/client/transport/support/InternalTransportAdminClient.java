/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalTransportAdminClient.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.transport.support;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.client.AdminClient;
import cn.com.rebirth.search.core.client.ClusterAdminClient;
import cn.com.rebirth.search.core.client.IndicesAdminClient;
import cn.com.rebirth.search.core.client.transport.TransportClientNodesService;

/**
 * The Class InternalTransportAdminClient.
 *
 * @author l.xue.nong
 */
public class InternalTransportAdminClient extends AbstractComponent implements AdminClient {

	/** The nodes service. */
	private final TransportClientNodesService nodesService;

	/** The indices admin client. */
	private final InternalTransportIndicesAdminClient indicesAdminClient;

	/** The cluster admin client. */
	private final InternalTransportClusterAdminClient clusterAdminClient;

	/**
	 * Instantiates a new internal transport admin client.
	 *
	 * @param settings the settings
	 * @param nodesService the nodes service
	 * @param indicesAdminClient the indices admin client
	 * @param clusterAdminClient the cluster admin client
	 */
	@Inject
	public InternalTransportAdminClient(Settings settings, TransportClientNodesService nodesService,
			InternalTransportIndicesAdminClient indicesAdminClient,
			InternalTransportClusterAdminClient clusterAdminClient) {
		super(settings);
		this.nodesService = nodesService;
		this.indicesAdminClient = indicesAdminClient;
		this.clusterAdminClient = clusterAdminClient;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.AdminClient#indices()
	 */
	@Override
	public IndicesAdminClient indices() {
		return indicesAdminClient;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.client.AdminClient#cluster()
	 */
	@Override
	public ClusterAdminClient cluster() {
		return clusterAdminClient;
	}
}
