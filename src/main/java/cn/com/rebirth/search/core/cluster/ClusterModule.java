/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterModule.java 2012-7-6 14:30:18 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.core.inject.Module;
import cn.com.rebirth.core.inject.SpawnModules;
import cn.com.rebirth.search.core.cluster.action.index.MappingUpdatedAction;
import cn.com.rebirth.search.core.cluster.action.index.NodeAliasesUpdatedAction;
import cn.com.rebirth.search.core.cluster.action.index.NodeIndexCreatedAction;
import cn.com.rebirth.search.core.cluster.action.index.NodeIndexDeletedAction;
import cn.com.rebirth.search.core.cluster.action.index.NodeMappingCreatedAction;
import cn.com.rebirth.search.core.cluster.action.index.NodeMappingRefreshAction;
import cn.com.rebirth.search.core.cluster.action.shard.ShardStateAction;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataCreateIndexService;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataDeleteIndexService;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataIndexAliasesService;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataIndexTemplateService;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataMappingService;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataService;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataStateIndexService;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataUpdateSettingsService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodeService;
import cn.com.rebirth.search.core.cluster.routing.RoutingService;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationModule;
import cn.com.rebirth.search.core.cluster.routing.operation.OperationRoutingModule;
import cn.com.rebirth.search.core.cluster.service.InternalClusterService;

import com.google.common.collect.ImmutableList;

/**
 * The Class ClusterModule.
 *
 * @author l.xue.nong
 */
public class ClusterModule extends AbstractModule implements SpawnModules {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new cluster module.
	 *
	 * @param settings the settings
	 */
	public ClusterModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(new AllocationModule(settings), new OperationRoutingModule(settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(DiscoveryNodeService.class).asEagerSingleton();
		bind(ClusterService.class).to(InternalClusterService.class).asEagerSingleton();

		bind(MetaDataService.class).asEagerSingleton();
		bind(MetaDataCreateIndexService.class).asEagerSingleton();
		bind(MetaDataDeleteIndexService.class).asEagerSingleton();
		bind(MetaDataStateIndexService.class).asEagerSingleton();
		bind(MetaDataMappingService.class).asEagerSingleton();
		bind(MetaDataIndexAliasesService.class).asEagerSingleton();
		bind(MetaDataUpdateSettingsService.class).asEagerSingleton();
		bind(MetaDataIndexTemplateService.class).asEagerSingleton();

		bind(RoutingService.class).asEagerSingleton();

		bind(ShardStateAction.class).asEagerSingleton();
		bind(NodeIndexCreatedAction.class).asEagerSingleton();
		bind(NodeIndexDeletedAction.class).asEagerSingleton();
		bind(NodeMappingCreatedAction.class).asEagerSingleton();
		bind(NodeMappingRefreshAction.class).asEagerSingleton();
		bind(MappingUpdatedAction.class).asEagerSingleton();
		bind(NodeAliasesUpdatedAction.class).asEagerSingleton();
	}
}