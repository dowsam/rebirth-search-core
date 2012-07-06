/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiversRouter.java 2012-7-6 14:30:01 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river.routing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.action.NoShardAvailableActionException;
import cn.com.rebirth.search.core.action.get.GetResponse;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MappingMetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.river.RiverIndexName;
import cn.com.rebirth.search.core.river.RiverName;
import cn.com.rebirth.search.core.river.cluster.RiverClusterService;
import cn.com.rebirth.search.core.river.cluster.RiverClusterState;
import cn.com.rebirth.search.core.river.cluster.RiverClusterStateUpdateTask;
import cn.com.rebirth.search.core.river.cluster.RiverNodeHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class RiversRouter.
 *
 * @author l.xue.nong
 */
public class RiversRouter extends AbstractLifecycleComponent<RiversRouter> implements ClusterStateListener {

	/** The river index name. */
	private final String riverIndexName;

	/** The client. */
	private final Client client;

	/** The river cluster service. */
	private final RiverClusterService riverClusterService;

	/**
	 * Instantiates a new rivers router.
	 *
	 * @param settings the settings
	 * @param client the client
	 * @param clusterService the cluster service
	 * @param riverClusterService the river cluster service
	 */
	@Inject
	public RiversRouter(Settings settings, Client client, ClusterService clusterService,
			RiverClusterService riverClusterService) {
		super(settings);
		this.riverIndexName = RiverIndexName.Conf.indexName(settings);
		this.riverClusterService = riverClusterService;
		this.client = client;
		clusterService.add(this);
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
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.rebirth.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(final ClusterChangedEvent event) {
		if (!event.localNodeMaster()) {
			return;
		}
		riverClusterService.submitStateUpdateTask("reroute_rivers_node_changed", new RiverClusterStateUpdateTask() {
			@Override
			public RiverClusterState execute(RiverClusterState currentState) {
				if (!event.state().metaData().hasIndex(riverIndexName)) {

					if (!currentState.routing().isEmpty()) {
						return RiverClusterState.builder().state(currentState).routing(RiversRouting.builder()).build();
					}
					return currentState;
				}

				RiversRouting.Builder routingBuilder = RiversRouting.builder().routing(currentState.routing());
				boolean dirty = false;

				IndexMetaData indexMetaData = event.state().metaData().index(riverIndexName);

				for (MappingMetaData mappingMd : indexMetaData.mappings().values()) {
					String mappingType = mappingMd.type();
					if (!currentState.routing().hasRiverByName(mappingType)) {

						try {
							GetResponse getResponse = client.prepareGet(riverIndexName, mappingType, "_meta").execute()
									.actionGet();
							if (getResponse.exists()) {
								String riverType = XContentMapValues.nodeStringValue(
										getResponse.sourceAsMap().get("type"), null);
								if (riverType == null) {
									logger.warn("no river type provided for [{}], ignoring...", riverIndexName);
								} else {
									routingBuilder.put(new RiverRouting(new RiverName(riverType, mappingType), null));
									dirty = true;
								}
							}
						} catch (NoShardAvailableActionException e) {

						} catch (ClusterBlockException e) {

						} catch (IndexMissingException e) {

						} catch (Exception e) {
							logger.warn("failed to get/parse _meta for [{}]", e, mappingType);
						}
					}
				}

				for (RiverRouting routing : currentState.routing()) {
					if (!indexMetaData.mappings().containsKey(routing.riverName().name())) {
						routingBuilder.remove(routing);
						dirty = true;
					} else if (routing.node() != null && !event.state().nodes().nodeExists(routing.node().id())) {
						routingBuilder.remove(routing);
						routingBuilder.put(new RiverRouting(routing.riverName(), null));
						dirty = true;
					}
				}

				Map<DiscoveryNode, List<RiverRouting>> nodesToRivers = Maps.newHashMap();

				for (DiscoveryNode node : event.state().nodes()) {
					if (RiverNodeHelper.isRiverNode(node)) {
						nodesToRivers.put(node, Lists.<RiverRouting> newArrayList());
					}
				}

				List<RiverRouting> unassigned = Lists.newArrayList();
				for (RiverRouting routing : routingBuilder.build()) {
					if (routing.node() == null) {
						unassigned.add(routing);
					} else {
						List<RiverRouting> l = nodesToRivers.get(routing.node());
						if (l == null) {
							l = Lists.newArrayList();
							nodesToRivers.put(routing.node(), l);
						}
						l.add(routing);
					}
				}
				for (Iterator<RiverRouting> it = unassigned.iterator(); it.hasNext();) {
					RiverRouting routing = it.next();
					DiscoveryNode smallest = null;
					int smallestSize = Integer.MAX_VALUE;
					for (Map.Entry<DiscoveryNode, List<RiverRouting>> entry : nodesToRivers.entrySet()) {
						if (RiverNodeHelper.isRiverNode(entry.getKey(), routing.riverName())) {
							if (entry.getValue().size() < smallestSize) {
								smallestSize = entry.getValue().size();
								smallest = entry.getKey();
							}
						}
					}
					if (smallest != null) {
						dirty = true;
						it.remove();
						routing.node(smallest);
						nodesToRivers.get(smallest).add(routing);
					}
				}

				if (dirty) {
					return RiverClusterState.builder().state(currentState).routing(routingBuilder).build();
				}
				return currentState;
			}
		});
	}
}
