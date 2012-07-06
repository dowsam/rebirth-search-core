/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterRerouteAction.java 2012-7-6 14:30:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.reroute;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class ClusterRerouteAction.
 *
 * @author l.xue.nong
 */
public class ClusterRerouteAction extends
		ClusterAction<ClusterRerouteRequest, ClusterRerouteResponse, ClusterRerouteRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final ClusterRerouteAction INSTANCE = new ClusterRerouteAction();

	/** The Constant NAME. */
	public static final String NAME = "cluster/reroute";

	/**
	 * Instantiates a new cluster reroute action.
	 */
	private ClusterRerouteAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public ClusterRerouteResponse newResponse() {
		return new ClusterRerouteResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.rebirth.search.core.client.ClusterAdminClient)
	 */
	@Override
	public ClusterRerouteRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new ClusterRerouteRequestBuilder(client);
	}
}
