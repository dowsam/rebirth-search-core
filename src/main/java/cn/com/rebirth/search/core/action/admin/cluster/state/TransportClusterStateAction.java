/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportClusterStateAction.java 2012-3-29 15:02:07 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.state;

import static cn.com.rebirth.search.core.cluster.ClusterState.newClusterStateBuilder;
import static cn.com.rebirth.search.core.cluster.metadata.MetaData.newMetaDataBuilder;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.IndexTemplateMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportClusterStateAction.
 *
 * @author l.xue.nong
 */
public class TransportClusterStateAction extends
		TransportMasterNodeOperationAction<ClusterStateRequest, ClusterStateResponse> {

	
	/** The cluster name. */
	private final ClusterName clusterName;

	
	/**
	 * Instantiates a new transport cluster state action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param clusterName the cluster name
	 */
	@Inject
	public TransportClusterStateAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, ClusterName clusterName) {
		super(settings, transportService, clusterService, threadPool);
		this.clusterName = clusterName;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return ClusterStateAction.NAME;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected ClusterStateRequest newRequest() {
		return new ClusterStateRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected ClusterStateResponse newResponse() {
		return new ClusterStateResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#localExecute(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest)
	 */
	@Override
	protected boolean localExecute(ClusterStateRequest request) {
		return request.local();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected ClusterStateResponse masterOperation(ClusterStateRequest request, ClusterState state)
			throws RestartException {
		ClusterState currentState = clusterService.state();
		ClusterState.Builder builder = newClusterStateBuilder();
		if (!request.filterNodes()) {
			builder.nodes(currentState.nodes());
		}
		if (!request.filterRoutingTable()) {
			builder.routingTable(currentState.routingTable());
			builder.allocationExplanation(currentState.allocationExplanation());
		}
		if (!request.filterBlocks()) {
			builder.blocks(currentState.blocks());
		}
		if (!request.filterMetaData()) {
			MetaData.Builder mdBuilder = newMetaDataBuilder();
			if (request.filteredIndices().length == 0 && request.filteredIndexTemplates().length == 0) {
				mdBuilder.metaData(currentState.metaData());
			}

			if (request.filteredIndices().length > 0) {
				String[] indices = currentState.metaData().concreteIndicesIgnoreMissing(request.filteredIndices());
				for (String filteredIndex : indices) {
					IndexMetaData indexMetaData = currentState.metaData().index(filteredIndex);
					if (indexMetaData != null) {
						mdBuilder.put(indexMetaData, false);
					}
				}
			}

			if (request.filteredIndexTemplates().length > 0) {
				for (String templateName : request.filteredIndexTemplates()) {
					IndexTemplateMetaData indexTemplateMetaData = currentState.metaData().templates().get(templateName);
					if (indexTemplateMetaData != null) {
						mdBuilder.put(indexTemplateMetaData);
					}
				}
			}

			builder.metaData(mdBuilder);
		}
		return new ClusterStateResponse(clusterName, builder.build());
	}
}