/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardGatewayModule.java 2012-7-6 14:29:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway;

import cn.com.rebirth.core.inject.AbstractModule;

/**
 * The Class IndexShardGatewayModule.
 *
 * @author l.xue.nong
 */
public class IndexShardGatewayModule extends AbstractModule {

	/** The index gateway. */
	private final IndexGateway indexGateway;

	/**
	 * Instantiates a new index shard gateway module.
	 *
	 * @param indexGateway the index gateway
	 */
	public IndexShardGatewayModule(IndexGateway indexGateway) {
		this.indexGateway = indexGateway;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexShardGateway.class).to(indexGateway.shardGatewayClass()).asEagerSingleton();

		bind(IndexShardGatewayService.class).asEagerSingleton();
	}
}
