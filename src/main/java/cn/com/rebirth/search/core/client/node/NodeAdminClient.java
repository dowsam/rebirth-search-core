/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeAdminClient.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.node;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.client.AdminClient;
import cn.com.rebirth.search.core.client.ClusterAdminClient;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class NodeAdminClient.
 *
 * @author l.xue.nong
 */
public class NodeAdminClient extends AbstractComponent implements AdminClient {

	/** The indices admin client. */
	private final NodeIndicesAdminClient indicesAdminClient;

	/** The cluster admin client. */
	private final NodeClusterAdminClient clusterAdminClient;

	/**
	 * Instantiates a new node admin client.
	 *
	 * @param settings the settings
	 * @param clusterAdminClient the cluster admin client
	 * @param indicesAdminClient the indices admin client
	 */
	@Inject
	public NodeAdminClient(Settings settings, NodeClusterAdminClient clusterAdminClient,
			NodeIndicesAdminClient indicesAdminClient) {
		super(settings);
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
		return this.clusterAdminClient;
	}
}
