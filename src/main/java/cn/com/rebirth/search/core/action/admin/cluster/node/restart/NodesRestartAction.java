/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesRestartAction.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.restart;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class NodesRestartAction.
 *
 * @author l.xue.nong
 */
public class NodesRestartAction extends
		ClusterAction<NodesRestartRequest, NodesRestartResponse, NodesRestartRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final NodesRestartAction INSTANCE = new NodesRestartAction();

	/** The Constant NAME. */
	public static final String NAME = "cluster/nodes/restart";

	/**
	 * Instantiates a new nodes restart action.
	 */
	private NodesRestartAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public NodesRestartResponse newResponse() {
		return new NodesRestartResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.rebirth.search.core.client.ClusterAdminClient)
	 */
	@Override
	public NodesRestartRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new NodesRestartRequestBuilder(client);
	}
}
