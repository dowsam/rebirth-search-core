/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardModule.java 2012-7-6 14:29:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.jmx.JmxService;

/**
 * The Class IndexShardModule.
 *
 * @author l.xue.nong
 */
public class IndexShardModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/** The shard id. */
	private final ShardId shardId;

	/**
	 * Instantiates a new index shard module.
	 *
	 * @param settings the settings
	 * @param shardId the shard id
	 */
	public IndexShardModule(Settings settings, ShardId shardId) {
		this.settings = settings;
		this.shardId = shardId;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ShardId.class).toInstance(shardId);
		bind(IndexShard.class).to(InternalIndexShard.class).asEagerSingleton();
		if (JmxService.shouldExport(settings)) {
			bind(IndexShardManagement.class).asEagerSingleton();
		}
	}
}