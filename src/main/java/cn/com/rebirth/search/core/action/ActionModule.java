/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ActionModule.java 2012-7-6 14:29:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import java.util.Map;

import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.core.inject.multibindings.MapBinder;
import cn.com.rebirth.search.core.action.admin.cluster.health.ClusterHealthAction;
import cn.com.rebirth.search.core.action.admin.cluster.health.TransportClusterHealthAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.TransportNodesInfoAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.NodesRestartAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.restart.TransportNodesRestartAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.NodesShutdownAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.shutdown.TransportNodesShutdownAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.NodesStatsAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.stats.TransportNodesStatsAction;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.ClusterRerouteAction;
import cn.com.rebirth.search.core.action.admin.cluster.reroute.TransportClusterRerouteAction;
import cn.com.rebirth.search.core.action.admin.cluster.settings.ClusterUpdateSettingsAction;
import cn.com.rebirth.search.core.action.admin.cluster.settings.TransportClusterUpdateSettingsAction;
import cn.com.rebirth.search.core.action.admin.cluster.state.ClusterStateAction;
import cn.com.rebirth.search.core.action.admin.cluster.state.TransportClusterStateAction;
import cn.com.rebirth.search.core.action.admin.indices.alias.IndicesAliasesAction;
import cn.com.rebirth.search.core.action.admin.indices.alias.TransportIndicesAliasesAction;
import cn.com.rebirth.search.core.action.admin.indices.analyze.AnalyzeAction;
import cn.com.rebirth.search.core.action.admin.indices.analyze.TransportAnalyzeAction;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.ClearIndicesCacheAction;
import cn.com.rebirth.search.core.action.admin.indices.cache.clear.TransportClearIndicesCacheAction;
import cn.com.rebirth.search.core.action.admin.indices.close.CloseIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.close.TransportCloseIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.create.CreateIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.create.TransportCreateIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.delete.DeleteIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.delete.TransportDeleteIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.exists.IndicesExistsAction;
import cn.com.rebirth.search.core.action.admin.indices.exists.TransportIndicesExistsAction;
import cn.com.rebirth.search.core.action.admin.indices.flush.FlushAction;
import cn.com.rebirth.search.core.action.admin.indices.flush.TransportFlushAction;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.GatewaySnapshotAction;
import cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot.TransportGatewaySnapshotAction;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.DeleteMappingAction;
import cn.com.rebirth.search.core.action.admin.indices.mapping.delete.TransportDeleteMappingAction;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.PutMappingAction;
import cn.com.rebirth.search.core.action.admin.indices.mapping.put.TransportPutMappingAction;
import cn.com.rebirth.search.core.action.admin.indices.open.OpenIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.open.TransportOpenIndexAction;
import cn.com.rebirth.search.core.action.admin.indices.optimize.OptimizeAction;
import cn.com.rebirth.search.core.action.admin.indices.optimize.TransportOptimizeAction;
import cn.com.rebirth.search.core.action.admin.indices.refresh.RefreshAction;
import cn.com.rebirth.search.core.action.admin.indices.refresh.TransportRefreshAction;
import cn.com.rebirth.search.core.action.admin.indices.segments.IndicesSegmentsAction;
import cn.com.rebirth.search.core.action.admin.indices.segments.TransportIndicesSegmentsAction;
import cn.com.rebirth.search.core.action.admin.indices.settings.TransportUpdateSettingsAction;
import cn.com.rebirth.search.core.action.admin.indices.settings.UpdateSettingsAction;
import cn.com.rebirth.search.core.action.admin.indices.stats.IndicesStatsAction;
import cn.com.rebirth.search.core.action.admin.indices.stats.TransportIndicesStatsAction;
import cn.com.rebirth.search.core.action.admin.indices.status.IndicesStatusAction;
import cn.com.rebirth.search.core.action.admin.indices.status.TransportIndicesStatusAction;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.DeleteIndexTemplateAction;
import cn.com.rebirth.search.core.action.admin.indices.template.delete.TransportDeleteIndexTemplateAction;
import cn.com.rebirth.search.core.action.admin.indices.template.put.PutIndexTemplateAction;
import cn.com.rebirth.search.core.action.admin.indices.template.put.TransportPutIndexTemplateAction;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.TransportValidateQueryAction;
import cn.com.rebirth.search.core.action.admin.indices.validate.query.ValidateQueryAction;
import cn.com.rebirth.search.core.action.bulk.BulkAction;
import cn.com.rebirth.search.core.action.bulk.TransportBulkAction;
import cn.com.rebirth.search.core.action.bulk.TransportShardBulkAction;
import cn.com.rebirth.search.core.action.count.CountAction;
import cn.com.rebirth.search.core.action.count.TransportCountAction;
import cn.com.rebirth.search.core.action.delete.DeleteAction;
import cn.com.rebirth.search.core.action.delete.TransportDeleteAction;
import cn.com.rebirth.search.core.action.delete.index.TransportIndexDeleteAction;
import cn.com.rebirth.search.core.action.delete.index.TransportShardDeleteAction;
import cn.com.rebirth.search.core.action.deletebyquery.DeleteByQueryAction;
import cn.com.rebirth.search.core.action.deletebyquery.TransportDeleteByQueryAction;
import cn.com.rebirth.search.core.action.deletebyquery.TransportIndexDeleteByQueryAction;
import cn.com.rebirth.search.core.action.deletebyquery.TransportShardDeleteByQueryAction;
import cn.com.rebirth.search.core.action.get.GetAction;
import cn.com.rebirth.search.core.action.get.MultiGetAction;
import cn.com.rebirth.search.core.action.get.TransportGetAction;
import cn.com.rebirth.search.core.action.get.TransportMultiGetAction;
import cn.com.rebirth.search.core.action.get.TransportShardMultiGetAction;
import cn.com.rebirth.search.core.action.index.IndexAction;
import cn.com.rebirth.search.core.action.index.TransportIndexAction;
import cn.com.rebirth.search.core.action.mlt.MoreLikeThisAction;
import cn.com.rebirth.search.core.action.mlt.TransportMoreLikeThisAction;
import cn.com.rebirth.search.core.action.percolate.PercolateAction;
import cn.com.rebirth.search.core.action.percolate.TransportPercolateAction;
import cn.com.rebirth.search.core.action.search.MultiSearchAction;
import cn.com.rebirth.search.core.action.search.SearchAction;
import cn.com.rebirth.search.core.action.search.SearchScrollAction;
import cn.com.rebirth.search.core.action.search.TransportMultiSearchAction;
import cn.com.rebirth.search.core.action.search.TransportSearchAction;
import cn.com.rebirth.search.core.action.search.TransportSearchScrollAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchCache;
import cn.com.rebirth.search.core.action.search.type.TransportSearchDfsQueryAndFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchDfsQueryThenFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchQueryAndFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchQueryThenFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchScanAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchScrollQueryAndFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchScrollQueryThenFetchAction;
import cn.com.rebirth.search.core.action.search.type.TransportSearchScrollScanAction;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.action.update.TransportUpdateAction;
import cn.com.rebirth.search.core.action.update.UpdateAction;

import com.google.common.collect.Maps;

/**
 * The Class ActionModule.
 *
 * @author l.xue.nong
 */
public class ActionModule extends AbstractModule {

	/** The actions. */
	private final Map<String, ActionEntry> actions = Maps.newHashMap();

	/**
	 * The Class ActionEntry.
	 *
	 * @param <Request> the generic type
	 * @param <Response> the generic type
	 * @author l.xue.nong
	 */
	static class ActionEntry<Request extends ActionRequest, Response extends ActionResponse> {

		/** The action. */
		public final GenericAction<Request, Response> action;

		/** The transport action. */
		public final Class<? extends TransportAction<Request, Response>> transportAction;

		/** The support transport actions. */
		public final Class[] supportTransportActions;

		/**
		 * Instantiates a new action entry.
		 *
		 * @param action the action
		 * @param transportAction the transport action
		 * @param supportTransportActions the support transport actions
		 */
		ActionEntry(GenericAction<Request, Response> action,
				Class<? extends TransportAction<Request, Response>> transportAction, Class... supportTransportActions) {
			this.action = action;
			this.transportAction = transportAction;
			this.supportTransportActions = supportTransportActions;
		}

	}

	/** The proxy. */
	private final boolean proxy;

	/**
	 * Instantiates a new action module.
	 *
	 * @param proxy the proxy
	 */
	public ActionModule(boolean proxy) {
		this.proxy = proxy;
	}

	/**
	 * Register action.
	 *
	 * @param <Request> the generic type
	 * @param <Response> the generic type
	 * @param action the action
	 * @param transportAction the transport action
	 * @param supportTransportActions the support transport actions
	 */
	public <Request extends ActionRequest, Response extends ActionResponse> void registerAction(
			GenericAction<Request, Response> action,
			Class<? extends TransportAction<Request, Response>> transportAction, Class... supportTransportActions) {
		actions.put(action.name(), new ActionEntry<Request, Response>(action, transportAction, supportTransportActions));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {

		registerAction(NodesInfoAction.INSTANCE, TransportNodesInfoAction.class);
		registerAction(NodesStatsAction.INSTANCE, TransportNodesStatsAction.class);
		registerAction(NodesShutdownAction.INSTANCE, TransportNodesShutdownAction.class);
		registerAction(NodesRestartAction.INSTANCE, TransportNodesRestartAction.class);

		registerAction(ClusterStateAction.INSTANCE, TransportClusterStateAction.class);
		registerAction(ClusterHealthAction.INSTANCE, TransportClusterHealthAction.class);
		registerAction(ClusterUpdateSettingsAction.INSTANCE, TransportClusterUpdateSettingsAction.class);
		registerAction(ClusterRerouteAction.INSTANCE, TransportClusterRerouteAction.class);

		registerAction(IndicesStatsAction.INSTANCE, TransportIndicesStatsAction.class);
		registerAction(IndicesStatusAction.INSTANCE, TransportIndicesStatusAction.class);
		registerAction(IndicesSegmentsAction.INSTANCE, TransportIndicesSegmentsAction.class);
		registerAction(CreateIndexAction.INSTANCE, TransportCreateIndexAction.class);
		registerAction(DeleteIndexAction.INSTANCE, TransportDeleteIndexAction.class);
		registerAction(OpenIndexAction.INSTANCE, TransportOpenIndexAction.class);
		registerAction(CloseIndexAction.INSTANCE, TransportCloseIndexAction.class);
		registerAction(IndicesExistsAction.INSTANCE, TransportIndicesExistsAction.class);
		registerAction(PutMappingAction.INSTANCE, TransportPutMappingAction.class);
		registerAction(DeleteMappingAction.INSTANCE, TransportDeleteMappingAction.class);
		registerAction(IndicesAliasesAction.INSTANCE, TransportIndicesAliasesAction.class);
		registerAction(UpdateSettingsAction.INSTANCE, TransportUpdateSettingsAction.class);
		registerAction(AnalyzeAction.INSTANCE, TransportAnalyzeAction.class);
		registerAction(PutIndexTemplateAction.INSTANCE, TransportPutIndexTemplateAction.class);
		registerAction(DeleteIndexTemplateAction.INSTANCE, TransportDeleteIndexTemplateAction.class);
		registerAction(ValidateQueryAction.INSTANCE, TransportValidateQueryAction.class);
		registerAction(GatewaySnapshotAction.INSTANCE, TransportGatewaySnapshotAction.class);
		registerAction(RefreshAction.INSTANCE, TransportRefreshAction.class);
		registerAction(FlushAction.INSTANCE, TransportFlushAction.class);
		registerAction(OptimizeAction.INSTANCE, TransportOptimizeAction.class);
		registerAction(ClearIndicesCacheAction.INSTANCE, TransportClearIndicesCacheAction.class);

		registerAction(IndexAction.INSTANCE, TransportIndexAction.class);
		registerAction(GetAction.INSTANCE, TransportGetAction.class);
		registerAction(DeleteAction.INSTANCE, TransportDeleteAction.class, TransportIndexDeleteAction.class,
				TransportShardDeleteAction.class);
		registerAction(CountAction.INSTANCE, TransportCountAction.class);
		registerAction(UpdateAction.INSTANCE, TransportUpdateAction.class);
		registerAction(MultiGetAction.INSTANCE, TransportMultiGetAction.class, TransportShardMultiGetAction.class);
		registerAction(BulkAction.INSTANCE, TransportBulkAction.class, TransportShardBulkAction.class);
		registerAction(DeleteByQueryAction.INSTANCE, TransportDeleteByQueryAction.class,
				TransportIndexDeleteByQueryAction.class, TransportShardDeleteByQueryAction.class);
		registerAction(SearchAction.INSTANCE, TransportSearchAction.class, TransportSearchCache.class,
				TransportSearchDfsQueryThenFetchAction.class, TransportSearchQueryThenFetchAction.class,
				TransportSearchDfsQueryAndFetchAction.class, TransportSearchQueryAndFetchAction.class,
				TransportSearchScanAction.class);
		registerAction(SearchScrollAction.INSTANCE, TransportSearchScrollAction.class,
				TransportSearchScrollScanAction.class, TransportSearchScrollQueryThenFetchAction.class,
				TransportSearchScrollQueryAndFetchAction.class);
		registerAction(MultiSearchAction.INSTANCE, TransportMultiSearchAction.class);
		registerAction(MoreLikeThisAction.INSTANCE, TransportMoreLikeThisAction.class);
		registerAction(PercolateAction.INSTANCE, TransportPercolateAction.class);

		MapBinder<String, GenericAction> actionsBinder = MapBinder.newMapBinder(binder(), String.class,
				GenericAction.class);

		for (Map.Entry<String, ActionEntry> entry : actions.entrySet()) {
			actionsBinder.addBinding(entry.getKey()).toInstance(entry.getValue().action);
		}

		if (!proxy) {
			MapBinder<GenericAction, TransportAction> transportActionsBinder = MapBinder.newMapBinder(binder(),
					GenericAction.class, TransportAction.class);
			for (Map.Entry<String, ActionEntry> entry : actions.entrySet()) {

				bind(entry.getValue().transportAction).asEagerSingleton();
				transportActionsBinder.addBinding(entry.getValue().action).to(entry.getValue().transportAction)
						.asEagerSingleton();
				for (Class supportAction : entry.getValue().supportTransportActions) {
					bind(supportAction).asEagerSingleton();
				}
			}
		}
	}
}
