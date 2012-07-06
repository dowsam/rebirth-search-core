/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesModule.java 2012-3-29 15:01:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.indices.analysis.IndicesAnalysisModule;
import cn.com.rebirth.search.core.indices.cache.filter.IndicesFilterCache;
import cn.com.rebirth.search.core.indices.cluster.IndicesClusterStateService;
import cn.com.rebirth.search.core.indices.memory.IndexingMemoryController;
import cn.com.rebirth.search.core.indices.query.IndicesQueriesModule;
import cn.com.rebirth.search.core.indices.recovery.RecoverySettings;
import cn.com.rebirth.search.core.indices.recovery.RecoverySource;
import cn.com.rebirth.search.core.indices.recovery.RecoveryTarget;
import cn.com.rebirth.search.core.indices.store.TransportNodesListShardStoreMetaData;
import cn.com.rebirth.search.core.indices.ttl.IndicesTTLService;

import com.google.common.collect.ImmutableList;

/**
 * The Class IndicesModule.
 *
 * @author l.xue.nong
 */
public class IndicesModule extends AbstractModule implements SpawnModules {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new indices module.
	 *
	 * @param settings the settings
	 */
	public IndicesModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(new IndicesQueriesModule(), new IndicesAnalysisModule());
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndicesLifecycle.class).to(InternalIndicesLifecycle.class).asEagerSingleton();

		bind(IndicesService.class).to(InternalIndicesService.class).asEagerSingleton();

		bind(RecoverySettings.class).asEagerSingleton();
		bind(RecoveryTarget.class).asEagerSingleton();
		bind(RecoverySource.class).asEagerSingleton();

		bind(IndicesClusterStateService.class).asEagerSingleton();
		bind(IndexingMemoryController.class).asEagerSingleton();
		bind(IndicesFilterCache.class).asEagerSingleton();
		bind(TransportNodesListShardStoreMetaData.class).asEagerSingleton();
		bind(IndicesTTLService.class).asEagerSingleton();
	}
}
