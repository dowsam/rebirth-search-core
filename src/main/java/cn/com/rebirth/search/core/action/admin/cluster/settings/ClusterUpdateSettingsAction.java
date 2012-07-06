/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterUpdateSettingsAction.java 2012-3-29 15:02:11 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.settings;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;


/**
 * The Class ClusterUpdateSettingsAction.
 *
 * @author l.xue.nong
 */
public class ClusterUpdateSettingsAction extends
		ClusterAction<ClusterUpdateSettingsRequest, ClusterUpdateSettingsResponse, ClusterUpdateSettingsRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final ClusterUpdateSettingsAction INSTANCE = new ClusterUpdateSettingsAction();

	
	/** The Constant NAME. */
	public static final String NAME = "cluster/settings/update";

	
	/**
	 * Instantiates a new cluster update settings action.
	 */
	private ClusterUpdateSettingsAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public ClusterUpdateSettingsResponse newResponse() {
		return new ClusterUpdateSettingsResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.summall.search.core.client.ClusterAdminClient)
	 */
	@Override
	public ClusterUpdateSettingsRequestBuilder newRequestBuilder(ClusterAdminClient client) {
		return new ClusterUpdateSettingsRequestBuilder(client);
	}
}
