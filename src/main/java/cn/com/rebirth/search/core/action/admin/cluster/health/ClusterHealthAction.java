/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterHealthAction.java 2012-3-29 15:01:10 l.xue.nong$$
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
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public ClusterHealthResponse newResponse() {
		return new ClusterHealthResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.summall.search.core.client.ClusterAdminClient)
	 */
	@Override
	public ClusterHealthRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new ClusterHealthRequestBuilder(client);
	}
}
