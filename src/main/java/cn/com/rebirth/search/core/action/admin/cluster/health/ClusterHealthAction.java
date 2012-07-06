/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterHealthAction.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.health;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class ClusterHealthAction.
 *
 * @author l.xue.nong
 */
public class ClusterHealthAction extends
		ClusterAction<ClusterHealthRequest, ClusterHealthResponse, ClusterHealthRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final ClusterHealthAction INSTANCE = new ClusterHealthAction();

	/** The Constant NAME. */
	public static final String NAME = "cluster/health";

	/**
	 * Instantiates a new cluster health action.
	 */
	private ClusterHealthAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public ClusterHealthResponse newResponse() {
		return new ClusterHealthResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.rebirth.search.core.client.ClusterAdminClient)
	 */
	@Override
	public ClusterHealthRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new ClusterHealthRequestBuilder(client);
	}
}
