/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesInfoAction.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.info;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Class NodesInfoAction.
 *
 * @author l.xue.nong
 */
public class NodesInfoAction extends ClusterAction<NodesInfoRequest, NodesInfoResponse, NodesInfoRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final NodesInfoAction INSTANCE = new NodesInfoAction();

	/** The Constant NAME. */
	public static final String NAME = "cluster/nodes/info";

	/**
	 * Instantiates a new nodes info action.
	 */
	private NodesInfoAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public NodesInfoResponse newResponse() {
		return new NodesInfoResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.rebirth.search.core.client.ClusterAdminClient)
	 */
	@Override
	public NodesInfoRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new NodesInfoRequestBuilder(client);
	}
}
