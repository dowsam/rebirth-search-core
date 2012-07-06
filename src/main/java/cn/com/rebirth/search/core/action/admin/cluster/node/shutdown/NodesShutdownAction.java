/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesShutdownAction.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.shutdown;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class NodesShutdownAction.
 *
 * @author l.xue.nong
 */
public class NodesShutdownAction extends
		ClusterAction<NodesShutdownRequest, NodesShutdownResponse, NodesShutdownRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final NodesShutdownAction INSTANCE = new NodesShutdownAction();

	/** The Constant NAME. */
	public static final String NAME = "cluster/nodes/shutdown";

	/**
	 * Instantiates a new nodes shutdown action.
	 */
	private NodesShutdownAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public NodesShutdownResponse newResponse() {
		return new NodesShutdownResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.rebirth.search.core.client.ClusterAdminClient)
	 */
	@Override
	public NodesShutdownRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new NodesShutdownRequestBuilder(client);
	}
}
