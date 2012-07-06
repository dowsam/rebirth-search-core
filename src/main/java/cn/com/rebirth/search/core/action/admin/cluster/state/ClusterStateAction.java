/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterStateAction.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.state;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class ClusterStateAction.
 *
 * @author l.xue.nong
 */
public class ClusterStateAction extends
		ClusterAction<ClusterStateRequest, ClusterStateResponse, ClusterStateRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final ClusterStateAction INSTANCE = new ClusterStateAction();

	/** The Constant NAME. */
	public static final String NAME = "cluster/state";

	/**
	 * Instantiates a new cluster state action.
	 */
	private ClusterStateAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public ClusterStateResponse newResponse() {
		return new ClusterStateResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.rebirth.search.core.client.ClusterAdminClient)
	 */
	@Override
	public ClusterStateRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new ClusterStateRequestBuilder(client);
	}
}
