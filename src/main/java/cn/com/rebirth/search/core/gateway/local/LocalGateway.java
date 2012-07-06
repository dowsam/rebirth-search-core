/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalGateway.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.local;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Set;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.io.FileSystemUtils;
import cn.com.rebirth.search.core.action.FailedNodeException;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.gateway.Gateway;
import cn.com.rebirth.search.core.gateway.GatewayException;
import cn.com.rebirth.search.core.gateway.local.state.meta.LocalGatewayMetaState;
import cn.com.rebirth.search.core.gateway.local.state.meta.TransportNodesListGatewayMetaState;
import cn.com.rebirth.search.core.gateway.local.state.shards.LocalGatewayShardsState;
import cn.com.rebirth.search.core.index.gateway.local.LocalIndexGatewayModule;

import com.google.common.collect.Sets;

/**
 * The Class LocalGateway.
 *
 * @author l.xue.nong
 */
public class LocalGateway extends AbstractLifecycleComponent<Gateway> implements Gateway, ClusterStateListener {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The node env. */
	private final NodeEnvironment nodeEnv;

	/** The shards state. */
	private final LocalGatewayShardsState shardsState;

	/** The meta state. */
	private final LocalGatewayMetaState metaState;

	/** The list gateway meta state. */
	private final TransportNodesListGatewayMetaState listGatewayMetaState;

	/** The initial meta. */
	private final String initialMeta;

	/**
	 * Instantiates a new local gateway.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param nodeEnv the node env
	 * @param shardsState the shards state
	 * @param metaState the meta state
	 * @param listGatewayMetaState the list gateway meta state
	 */
	@Inject
	public LocalGateway(Settings settings, ClusterService clusterService, NodeEnvironment nodeEnv,
			LocalGatewayShardsState shardsState, LocalGatewayMetaState metaState,
			TransportNodesListGatewayMetaState listGatewayMetaState) {
		super(settings);
		this.clusterService = clusterService;
		this.nodeEnv = nodeEnv;
		this.metaState = metaState;
		this.listGatewayMetaState = listGatewayMetaState;

		this.shardsState = shardsState;

		clusterService.addLast(this);

		this.initialMeta = componentSettings.get("initial_meta",
				settings.get("discovery.zen.minimum_master_nodes", "1"));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#type()
	 */
	@Override
	public String type() {
		return "local";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
		clusterService.remove(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#performStateRecovery(cn.com.rebirth.search.core.gateway.Gateway.GatewayStateRecoveredListener)
	 */
	@Override
	public void performStateRecovery(final GatewayStateRecoveredListener listener) throws GatewayException {
		Set<String> nodesIds = Sets.newHashSet();
		nodesIds.addAll(clusterService.state().nodes().masterNodes().keySet());
		TransportNodesListGatewayMetaState.NodesLocalGatewayMetaState nodesState = listGatewayMetaState.list(nodesIds,
				null).actionGet();

		int requiredAllocation = 1;
		try {
			if ("quorum".equals(initialMeta)) {
				if (nodesIds.size() > 2) {
					requiredAllocation = (nodesIds.size() / 2) + 1;
				}
			} else if ("quorum-1".equals(initialMeta) || "half".equals(initialMeta)) {
				if (nodesIds.size() > 2) {
					requiredAllocation = ((1 + nodesIds.size()) / 2);
				}
			} else if ("one".equals(initialMeta)) {
				requiredAllocation = 1;
			} else if ("full".equals(initialMeta) || "all".equals(initialMeta)) {
				requiredAllocation = nodesIds.size();
			} else if ("full-1".equals(initialMeta) || "all-1".equals(initialMeta)) {
				if (nodesIds.size() > 1) {
					requiredAllocation = nodesIds.size() - 1;
				}
			} else {
				requiredAllocation = Integer.parseInt(initialMeta);
			}
		} catch (Exception e) {
			logger.warn("failed to derived initial_meta from value {}", initialMeta);
		}

		if (nodesState.failures().length > 0) {
			for (FailedNodeException failedNodeException : nodesState.failures()) {
				logger.warn("failed to fetch state from node", failedNodeException);
			}
		}

		MetaData.Builder metaDataBuilder = MetaData.builder();
		TObjectIntHashMap<String> indices = new TObjectIntHashMap<String>();
		MetaData electedGlobalState = null;
		int found = 0;
		for (TransportNodesListGatewayMetaState.NodeLocalGatewayMetaState nodeState : nodesState) {
			if (nodeState.metaData() == null) {
				continue;
			}
			found++;
			if (electedGlobalState == null) {
				electedGlobalState = nodeState.metaData();
			} else if (nodeState.metaData().version() > electedGlobalState.version()) {
				electedGlobalState = nodeState.metaData();
			}
			for (IndexMetaData indexMetaData : nodeState.metaData().indices().values()) {
				indices.adjustOrPutValue(indexMetaData.index(), 1, 1);
			}
		}
		if (found < requiredAllocation) {
			listener.onFailure("found [" + found + "] metadata states, required [" + requiredAllocation + "]");
			return;
		}

		metaDataBuilder.metaData(electedGlobalState).removeAllIndices();
		for (String index : indices.keySet()) {
			IndexMetaData electedIndexMetaData = null;
			int indexMetaDataCount = 0;
			for (TransportNodesListGatewayMetaState.NodeLocalGatewayMetaState nodeState : nodesState) {
				if (nodeState.metaData() == null) {
					continue;
				}
				IndexMetaData indexMetaData = nodeState.metaData().index(index);
				if (indexMetaData == null) {
					continue;
				}
				if (electedIndexMetaData == null) {
					electedIndexMetaData = indexMetaData;
				} else if (indexMetaData.version() > electedIndexMetaData.version()) {
					electedIndexMetaData = indexMetaData;
				}
				indexMetaDataCount++;
			}
			if (electedIndexMetaData != null) {
				if (indexMetaDataCount < requiredAllocation) {
					logger.debug("[" + index + "] found [{}], required [{}], not adding", indexMetaDataCount,
							requiredAllocation);
				}
				metaDataBuilder.put(electedIndexMetaData, false);
			}
		}
		ClusterState.Builder builder = ClusterState.builder();
		builder.metaData(metaDataBuilder);
		listener.onSuccess(builder.build());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#suggestIndexGateway()
	 */
	@Override
	public Class<? extends Module> suggestIndexGateway() {
		return LocalIndexGatewayModule.class;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#reset()
	 */
	@Override
	public void reset() throws Exception {
		FileSystemUtils.deleteRecursively(nodeEnv.nodeDataLocations());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.rebirth.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(final ClusterChangedEvent event) {

		if (event.state().blocks().disableStatePersistence()) {
			return;
		}
		metaState.clusterChanged(event);
		shardsState.clusterChanged(event);
	}
}
