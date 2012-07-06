/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeClientModule.java 2012-3-29 15:02:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.node;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.client.AdminClient;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.client.ClusterAdminClient;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class NodeClientModule.
 *
 * @author l.xue.nong
 */
public class NodeClientModule extends AbstractModule {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ClusterAdminClient.class).to(NodeClusterAdminClient.class).asEagerSingleton();
		bind(IndicesAdminClient.class).to(NodeIndicesAdminClient.class).asEagerSingleton();
		bind(AdminClient.class).to(NodeAdminClient.class).asEagerSingleton();
		bind(Client.class).to(NodeClient.class).asEagerSingleton();
	}
}
