/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestActionModule.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action;

import java.util.List;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.rest.BaseRestHandler;
import cn.com.rebirth.search.core.rest.action.admin.cluster.health.RestClusterHealthAction;
import cn.com.rebirth.search.core.rest.action.admin.cluster.node.info.RestNodesInfoAction;
import cn.com.rebirth.search.core.rest.action.admin.cluster.node.restart.RestNodesRestartAction;
import cn.com.rebirth.search.core.rest.action.admin.cluster.node.shutdown.RestNodesShutdownAction;
import cn.com.rebirth.search.core.rest.action.admin.cluster.node.stats.RestNodesStatsAction;
import cn.com.rebirth.search.core.rest.action.admin.cluster.reroute.RestClusterRerouteAction;
import cn.com.rebirth.search.core.rest.action.admin.cluster.settings.RestClusterGetSettingsAction;
import cn.com.rebirth.search.core.rest.action.admin.cluster.settings.RestClusterUpdateSettingsAction;
import cn.com.rebirth.search.core.rest.action.admin.cluster.state.RestClusterStateAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.alias.RestGetIndicesAliasesAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.alias.RestIndicesAliasesAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.analyze.RestAnalyzeAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.cache.clear.RestClearIndicesCacheAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.close.RestCloseIndexAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.create.RestCreateIndexAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.delete.RestDeleteIndexAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.exists.RestIndicesExistsAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.flush.RestFlushAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.gateway.snapshot.RestGatewaySnapshotAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.mapping.delete.RestDeleteMappingAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.mapping.get.RestGetMappingAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.mapping.put.RestPutMappingAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.open.RestOpenIndexAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.optimize.RestOptimizeAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.refresh.RestRefreshAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.segments.RestIndicesSegmentsAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.settings.RestGetSettingsAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.settings.RestUpdateSettingsAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.stats.RestIndicesStatsAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.status.RestIndicesStatusAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.template.delete.RestDeleteIndexTemplateAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.template.get.RestGetIndexTemplateAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.template.put.RestPutIndexTemplateAction;
import cn.com.rebirth.search.core.rest.action.admin.indices.validate.query.RestValidateQueryAction;
import cn.com.rebirth.search.core.rest.action.bulk.RestBulkAction;
import cn.com.rebirth.search.core.rest.action.count.RestCountAction;
import cn.com.rebirth.search.core.rest.action.delete.RestDeleteAction;
import cn.com.rebirth.search.core.rest.action.deletebyquery.RestDeleteByQueryAction;
import cn.com.rebirth.search.core.rest.action.get.RestGetAction;
import cn.com.rebirth.search.core.rest.action.get.RestMultiGetAction;
import cn.com.rebirth.search.core.rest.action.index.RestIndexAction;
import cn.com.rebirth.search.core.rest.action.main.RestMainAction;
import cn.com.rebirth.search.core.rest.action.mlt.RestMoreLikeThisAction;
import cn.com.rebirth.search.core.rest.action.percolate.RestPercolateAction;
import cn.com.rebirth.search.core.rest.action.search.RestMultiSearchAction;
import cn.com.rebirth.search.core.rest.action.search.RestSearchAction;
import cn.com.rebirth.search.core.rest.action.search.RestSearchScrollAction;
import cn.com.rebirth.search.core.rest.action.update.RestUpdateAction;

import com.google.common.collect.Lists;

/**
 * The Class RestActionModule.
 *
 * @author l.xue.nong
 */
public class RestActionModule extends AbstractModule {

	/** The rest plugins actions. */
	private List<Class<? extends BaseRestHandler>> restPluginsActions = Lists.newArrayList();

	/**
	 * Instantiates a new rest action module.
	 *
	 * @param restPluginsActions the rest plugins actions
	 */
	public RestActionModule(List<Class<? extends BaseRestHandler>> restPluginsActions) {
		this.restPluginsActions = restPluginsActions;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		for (Class<? extends BaseRestHandler> restAction : restPluginsActions) {
			bind(restAction).asEagerSingleton();
		}

		bind(RestMainAction.class).asEagerSingleton();

		bind(RestNodesInfoAction.class).asEagerSingleton();
		bind(RestNodesStatsAction.class).asEagerSingleton();
		bind(RestNodesShutdownAction.class).asEagerSingleton();
		bind(RestNodesRestartAction.class).asEagerSingleton();
		bind(RestClusterStateAction.class).asEagerSingleton();
		bind(RestClusterHealthAction.class).asEagerSingleton();
		bind(RestClusterUpdateSettingsAction.class).asEagerSingleton();
		bind(RestClusterGetSettingsAction.class).asEagerSingleton();
		bind(RestClusterRerouteAction.class).asEagerSingleton();

		bind(RestIndicesExistsAction.class).asEagerSingleton();
		bind(RestIndicesStatsAction.class).asEagerSingleton();
		bind(RestIndicesStatusAction.class).asEagerSingleton();
		bind(RestIndicesSegmentsAction.class).asEagerSingleton();
		bind(RestGetIndicesAliasesAction.class).asEagerSingleton();
		bind(RestIndicesAliasesAction.class).asEagerSingleton();
		bind(RestCreateIndexAction.class).asEagerSingleton();
		bind(RestDeleteIndexAction.class).asEagerSingleton();
		bind(RestCloseIndexAction.class).asEagerSingleton();
		bind(RestOpenIndexAction.class).asEagerSingleton();

		bind(RestUpdateSettingsAction.class).asEagerSingleton();
		bind(RestGetSettingsAction.class).asEagerSingleton();

		bind(RestAnalyzeAction.class).asEagerSingleton();
		bind(RestGetIndexTemplateAction.class).asEagerSingleton();
		bind(RestPutIndexTemplateAction.class).asEagerSingleton();
		bind(RestDeleteIndexTemplateAction.class).asEagerSingleton();

		bind(RestPutMappingAction.class).asEagerSingleton();
		bind(RestDeleteMappingAction.class).asEagerSingleton();
		bind(RestGetMappingAction.class).asEagerSingleton();

		bind(RestGatewaySnapshotAction.class).asEagerSingleton();

		bind(RestRefreshAction.class).asEagerSingleton();
		bind(RestFlushAction.class).asEagerSingleton();
		bind(RestOptimizeAction.class).asEagerSingleton();
		bind(RestClearIndicesCacheAction.class).asEagerSingleton();

		bind(RestIndexAction.class).asEagerSingleton();
		bind(RestGetAction.class).asEagerSingleton();
		bind(RestMultiGetAction.class).asEagerSingleton();
		bind(RestDeleteAction.class).asEagerSingleton();
		bind(RestDeleteByQueryAction.class).asEagerSingleton();
		bind(RestCountAction.class).asEagerSingleton();
		bind(RestBulkAction.class).asEagerSingleton();
		bind(RestUpdateAction.class).asEagerSingleton();
		bind(RestPercolateAction.class).asEagerSingleton();

		bind(RestSearchAction.class).asEagerSingleton();
		bind(RestSearchScrollAction.class).asEagerSingleton();
		bind(RestMultiSearchAction.class).asEagerSingleton();

		bind(RestValidateQueryAction.class).asEagerSingleton();

		bind(RestMoreLikeThisAction.class).asEagerSingleton();
	}
}
