/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterRerouteAction.java 2012-3-29 15:01:44 l.xue.nong$$
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
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public ClusterRerouteResponse newResponse() {
		return new ClusterRerouteResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.summall.search.core.client.ClusterAdminClient)
	 */
	@Override
	public ClusterRerouteRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new ClusterRerouteRequestBuilder(client);
	}
}
