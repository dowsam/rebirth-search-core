/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalTransportAdminClient.java 2012-3-29 15:00:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.transport.support;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
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
	 * @see cn.com.summall.search.core.client.AdminClient#indices()
	 */
	@Override
	public IndicesAdminClient indices() {
		return indicesAdminClient;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.client.AdminClient#cluster()
	 */
	@Override
	public ClusterAdminClient cluster() {
		return clusterAdminClient;
	}
}
