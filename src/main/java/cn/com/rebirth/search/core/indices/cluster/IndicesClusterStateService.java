/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesClusterStateService.java 2012-3-29 15:00:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices.cluster;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.action.index.NodeAliasesUpdatedAction;
import cn.com.rebirth.search.core.cluster.action.index.NodeIndexCreatedAction;
import cn.com.rebirth.search.core.cluster.action.index.NodeIndexDeletedAction;
import cn.com.rebirth.search.core.cluster.action.index.NodeMappingCreatedAction;
import cn.com.rebirth.search.core.cluster.action.index.NodeMappingRefreshAction;
import cn.com.rebirth.search.core.cluster.action.shard.ShardStateAction;
import cn.com.rebirth.search.core.cluster.metadata.AliasMetaData;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MappingMetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.IndexShardRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.IndexShardAlreadyExistsException;
import cn.com.rebirth.search.core.index.IndexShardMissingException;
import cn.com.rebirth.search.core.index.aliases.IndexAlias;
import cn.com.rebirth.search.core.index.aliases.IndexAliasesService;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewayRecoveryException;
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewayService;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.indices.recovery.RecoveryFailedException;
import cn.com.rebirth.search.core.indices.recovery.RecoveryTarget;
import cn.com.rebirth.search.core.indices.recovery.StartRecoveryRequest;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.Lists;


/**
 * The Class IndicesClusterStateService.
 *
 * @author l.xue.nong
 */
public class IndicesClusterStateService extends AbstractLifecycleComponent<IndicesClusterStateService> implements
		ClusterStateListener {

	
	/** The indices service. */
	private final IndicesService indicesService;

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The recovery target. */
	private final RecoveryTarget recoveryTarget;

	
	/** The shard state action. */
	private final ShardStateAction shardStateAction;

	
	/** The node index created action. */
	private final NodeIndexCreatedAction nodeIndexCreatedAction;

	
	/** The node index deleted action. */
	private final NodeIndexDeletedAction nodeIndexDeletedAction;

	
	/** The node mapping created action. */
	private final NodeMappingCreatedAction nodeMappingCreatedAction;

	
	/** The node mapping refresh action. */
	private final NodeMappingRefreshAction nodeMappingRefreshAction;

	
	/** The node aliases updated action. */
	private final NodeAliasesUpdatedAction nodeAliasesUpdatedAction;

	
	
	
	/** The seen mappings. */
	private final ConcurrentMap<Tuple<String, String>, Boolean> seenMappings = ConcurrentCollections.newConcurrentMap();

	
	/** The mutex. */
	private final Object mutex = new Object();

	
	/** The failed engine handler. */
	private final FailedEngineHandler failedEngineHandler = new FailedEngineHandler();

	
	/**
	 * Instantiates a new indices cluster state service.
	 *
	 * @param settings the settings
	 * @param indicesService the indices service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param recoveryTarget the recovery target
	 * @param shardStateAction the shard state action
	 * @param nodeIndexCreatedAction the node index created action
	 * @param nodeIndexDeletedAction the node index deleted action
	 * @param nodeMappingCreatedAction the node mapping created action
	 * @param nodeMappingRefreshAction the node mapping refresh action
	 * @param nodeAliasesUpdatedAction the node aliases updated action
	 */
	@Inject
	public IndicesClusterStateService(Settings settings, IndicesService indicesService, ClusterService clusterService,
			ThreadPool threadPool, RecoveryTarget recoveryTarget, ShardStateAction shardStateAction,
			NodeIndexCreatedAction nodeIndexCreatedAction, NodeIndexDeletedAction nodeIndexDeletedAction,
			NodeMappingCreatedAction nodeMappingCreatedAction, NodeMappingRefreshAction nodeMappingRefreshAction,
			NodeAliasesUpdatedAction nodeAliasesUpdatedAction) {
		super(settings);
		this.indicesService = indicesService;
		this.clusterService = clusterService;
		this.threadPool = threadPool;
		this.recoveryTarget = recoveryTarget;
		this.shardStateAction = shardStateAction;
		this.nodeIndexCreatedAction = nodeIndexCreatedAction;
		this.nodeIndexDeletedAction = nodeIndexDeletedAction;
		this.nodeMappingCreatedAction = nodeMappingCreatedAction;
		this.nodeMappingRefreshAction = nodeMappingRefreshAction;
		this.nodeAliasesUpdatedAction = nodeAliasesUpdatedAction;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
		clusterService.add(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
		clusterService.remove(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.summall.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(final ClusterChangedEvent event) {
		if (!indicesService.changesAllowed())
			return;

		if (!lifecycle.started()) {
			return;
		}

		synchronized (mutex) {
			if (event.state().blocks().disableStatePersistence()) {
				for (final String index : indicesService.indices()) {
					IndexService indexService = indicesService.indexService(index);
					for (Integer shardId : indexService.shardIds()) {
						logger.debug("[{}][{}] removing shard (disabled block persistence)", index, shardId);
						try {
							indexService.removeShard(shardId, "removing shard (disabled block persistence)");
						} catch (Exception e) {
							logger.warn("[{}] failed to remove shard (disabled block persistence)", e, index);
						}
					}
					indicesService.cleanIndex(index, "cleaning index (disabled block persistence)");
				}
				return;
			}

			applyNewIndices(event);
			applyMappings(event);
			applyAliases(event);
			applyNewOrUpdatedShards(event);
			applyDeletedIndices(event);
			applyDeletedShards(event);
			applyCleanedIndices(event);
			applySettings(event);
			sendIndexLifecycleEvents(event);
		}
	}

	
	/**
	 * Send index lifecycle events.
	 *
	 * @param event the event
	 */
	private void sendIndexLifecycleEvents(final ClusterChangedEvent event) {
		for (String index : event.indicesCreated()) {
			try {
				nodeIndexCreatedAction.nodeIndexCreated(index, event.state().nodes().localNodeId());
			} catch (Exception e) {
				logger.debug("failed to send to master index {} created event", index);
			}
		}
		for (String index : event.indicesDeleted()) {
			try {
				nodeIndexDeletedAction.nodeIndexDeleted(index, event.state().nodes().localNodeId());
			} catch (Exception e) {
				logger.debug("failed to send to master index {} deleted event", index);
			}
		}
	}

	
	/**
	 * Apply cleaned indices.
	 *
	 * @param event the event
	 */
	private void applyCleanedIndices(final ClusterChangedEvent event) {
		
		
		for (final String index : indicesService.indices()) {
			IndexMetaData indexMetaData = event.state().metaData().index(index);
			if (indexMetaData != null && indexMetaData.state() == IndexMetaData.State.CLOSE) {
				IndexService indexService = indicesService.indexService(index);
				for (Integer shardId : indexService.shardIds()) {
					logger.debug("[{}][{}] removing shard (index is closed)", index, shardId);
					try {
						indexService.removeShard(shardId, "removing shard (index is closed)");
					} catch (Exception e) {
						logger.warn("[{}] failed to remove shard (index is closed)", e, index);
					}
				}
			}
		}
		for (final String index : indicesService.indices()) {
			if (indicesService.indexService(index).shardIds().isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("[{}] cleaning index (no shards allocated)", index);
				}
				
				try {
					indicesService.cleanIndex(index, "cleaning index (no shards allocated)");
				} catch (Exception e) {
					logger.warn("[{}] failed to clean index (no shards of that index are allocated on this node)", e,
							index);
				}
			}
		}
	}

	
	/**
	 * Apply deleted indices.
	 *
	 * @param event the event
	 */
	private void applyDeletedIndices(final ClusterChangedEvent event) {
		for (final String index : indicesService.indices()) {
			if (!event.state().metaData().hasIndex(index)) {
				if (logger.isDebugEnabled()) {
					logger.debug("[{}] deleting index", index);
				}
				try {
					indicesService.deleteIndex(index, "deleting index");
				} catch (Exception e) {
					logger.warn("failed to delete index", e);
				}
				
				for (Tuple<String, String> tuple : seenMappings.keySet()) {
					if (tuple.v1().equals(index)) {
						seenMappings.remove(tuple);
					}
				}
			}
		}
	}

	
	/**
	 * Apply deleted shards.
	 *
	 * @param event the event
	 */
	private void applyDeletedShards(final ClusterChangedEvent event) {
		RoutingNode routingNodes = event.state().readOnlyRoutingNodes().nodesToShards()
				.get(event.state().nodes().localNodeId());
		if (routingNodes == null) {
			return;
		}
		for (final String index : indicesService.indices()) {
			IndexMetaData indexMetaData = event.state().metaData().index(index);
			if (indexMetaData != null) {
				
				Set<Integer> newShardIds = newHashSet();
				for (final ShardRouting shardRouting : routingNodes) {
					if (shardRouting.index().equals(index)) {
						newShardIds.add(shardRouting.id());
					}
				}
				final IndexService indexService = indicesService.indexService(index);
				if (indexService == null) {
					continue;
				}
				for (Integer existingShardId : indexService.shardIds()) {
					if (!newShardIds.contains(existingShardId)) {
						if (indexMetaData.state() == IndexMetaData.State.CLOSE) {
							if (logger.isDebugEnabled()) {
								logger.debug("[{}][{}] removing shard (index is closed)", index, existingShardId);
							}
							indexService.removeShard(existingShardId, "removing shard (index is closed)");
						} else {
							
							
							if (logger.isDebugEnabled()) {
								logger.debug("[{}][{}] removing shard (not allocated)", index, existingShardId);
							}
							indexService.removeShard(existingShardId, "removing shard (not allocated)");
						}
					}
				}
			}
		}
	}

	
	/**
	 * Apply new indices.
	 *
	 * @param event the event
	 */
	private void applyNewIndices(final ClusterChangedEvent event) {
		
		RoutingNode routingNode = event.state().readOnlyRoutingNodes().nodesToShards()
				.get(event.state().nodes().localNodeId());
		if (routingNode == null) {
			return;
		}
		for (MutableShardRouting shard : routingNode) {
			if (!indicesService.hasIndex(shard.index())) {
				final IndexMetaData indexMetaData = event.state().metaData().index(shard.index());
				if (logger.isDebugEnabled()) {
					logger.debug("[{}] creating index", indexMetaData.index());
				}
				indicesService.createIndex(indexMetaData.index(), indexMetaData.settings(), event.state().nodes()
						.localNode().id());
			}
		}
	}

	
	/**
	 * Apply settings.
	 *
	 * @param event the event
	 */
	private void applySettings(ClusterChangedEvent event) {
		if (!event.metaDataChanged()) {
			return;
		}
		for (IndexMetaData indexMetaData : event.state().metaData()) {
			if (!indicesService.hasIndex(indexMetaData.index())) {
				
				continue;
			}
			
			if (!event.indexMetaDataChanged(indexMetaData)) {
				continue;
			}
			String index = indexMetaData.index();
			IndexService indexService = indicesService.indexServiceSafe(index);
			IndexSettingsService indexSettingsService = indexService.injector().getInstance(IndexSettingsService.class);
			indexSettingsService.refreshSettings(indexMetaData.settings());
		}
	}

	
	/**
	 * Apply mappings.
	 *
	 * @param event the event
	 */
	private void applyMappings(ClusterChangedEvent event) {
		
		for (IndexMetaData indexMetaData : event.state().metaData()) {
			if (!indicesService.hasIndex(indexMetaData.index())) {
				
				continue;
			}
			List<String> typesToRefresh = null;
			String index = indexMetaData.index();
			IndexService indexService = indicesService.indexServiceSafe(index);
			MapperService mapperService = indexService.mapperService();
			
			if (indexMetaData.mappings().containsKey(MapperService.DEFAULT_MAPPING)) {
				processMapping(event, index, mapperService, MapperService.DEFAULT_MAPPING,
						indexMetaData.mapping(MapperService.DEFAULT_MAPPING).source());
			}

			
			for (MappingMetaData mappingMd : indexMetaData.mappings().values()) {
				String mappingType = mappingMd.type();
				CompressedString mappingSource = mappingMd.source();
				if (mappingType.equals(MapperService.DEFAULT_MAPPING)) { 
					continue;
				}
				boolean requireRefresh = processMapping(event, index, mapperService, mappingType, mappingSource);
				if (requireRefresh) {
					if (typesToRefresh == null) {
						typesToRefresh = Lists.newArrayList();
					}
					typesToRefresh.add(mappingType);
				}
			}
			if (typesToRefresh != null) {
				nodeMappingRefreshAction.nodeMappingRefresh(new NodeMappingRefreshAction.NodeMappingRefreshRequest(
						index, typesToRefresh.toArray(new String[typesToRefresh.size()]), event.state().nodes()
								.localNodeId()));
			}
			
			for (DocumentMapper documentMapper : mapperService) {
				if (seenMappings.containsKey(new Tuple<String, String>(index, documentMapper.type()))
						&& !indexMetaData.mappings().containsKey(documentMapper.type())) {
					
					mapperService.remove(documentMapper.type());
					seenMappings.remove(new Tuple<String, String>(index, documentMapper.type()));
				}
			}
		}
	}

	
	/**
	 * Process mapping.
	 *
	 * @param event the event
	 * @param index the index
	 * @param mapperService the mapper service
	 * @param mappingType the mapping type
	 * @param mappingSource the mapping source
	 * @return true, if successful
	 */
	private boolean processMapping(ClusterChangedEvent event, String index, MapperService mapperService,
			String mappingType, CompressedString mappingSource) {
		if (!seenMappings.containsKey(new Tuple<String, String>(index, mappingType))) {
			seenMappings.put(new Tuple<String, String>(index, mappingType), true);
		}

		boolean requiresRefresh = false;
		try {
			if (!mapperService.hasMapping(mappingType)) {
				if (logger.isDebugEnabled()) {
					logger.debug("[" + index + "] adding mapping [{}], source [{}]", mappingType,
							mappingSource.string());
				}
				mapperService.add(mappingType, mappingSource.string());
				if (!mapperService.documentMapper(mappingType).mappingSource().equals(mappingSource)) {
					
					logger.debug("[" + index + "] parsed mapping [" + mappingType
							+ "], and got different sources\noriginal:\n{}\nparsed:\n{}", mappingSource, mapperService
							.documentMapper(mappingType).mappingSource());
					requiresRefresh = true;
				}
				nodeMappingCreatedAction.nodeMappingCreated(new NodeMappingCreatedAction.NodeMappingCreatedResponse(
						index, mappingType, event.state().nodes().localNodeId()));
			} else {
				DocumentMapper existingMapper = mapperService.documentMapper(mappingType);
				if (!mappingSource.equals(existingMapper.mappingSource())) {
					
					if (logger.isDebugEnabled()) {
						logger.debug("[" + index + "] updating mapping [{}], source [{}]", mappingType,
								mappingSource.string());
					}
					mapperService.add(mappingType, mappingSource.string());
					if (!mapperService.documentMapper(mappingType).mappingSource().equals(mappingSource)) {
						requiresRefresh = true;
						
						logger.debug("[" + index + "] parsed mapping [" + mappingType
								+ "], and got different sources\noriginal:\n{}\nparsed:\n{}", mappingSource,
								mapperService.documentMapper(mappingType).mappingSource());
					}
					nodeMappingCreatedAction
							.nodeMappingCreated(new NodeMappingCreatedAction.NodeMappingCreatedResponse(index,
									mappingType, event.state().nodes().localNodeId()));
				}
			}
		} catch (Exception e) {
			logger.warn("[" + index + "] failed to add mapping [" + mappingType + "], source [" + mappingSource + "]",
					e);
		}
		return requiresRefresh;
	}

	
	/**
	 * Aliases changed.
	 *
	 * @param event the event
	 * @return true, if successful
	 */
	private boolean aliasesChanged(ClusterChangedEvent event) {
		return !event.state().metaData().aliases().equals(event.previousState().metaData().aliases())
				|| !event.state().routingTable().equals(event.previousState().routingTable());
	}

	
	/**
	 * Apply aliases.
	 *
	 * @param event the event
	 */
	private void applyAliases(ClusterChangedEvent event) {
		
		if (aliasesChanged(event)) {
			
			for (IndexMetaData indexMetaData : event.state().metaData()) {
				if (!indicesService.hasIndex(indexMetaData.index())) {
					
					continue;
				}
				String index = indexMetaData.index();
				IndexService indexService = indicesService.indexService(index);
				IndexAliasesService indexAliasesService = indexService.aliasesService();
				for (AliasMetaData aliasesMd : indexMetaData.aliases().values()) {
					processAlias(index, aliasesMd.alias(), aliasesMd.filter(), indexAliasesService);
				}
				
				for (IndexAlias indexAlias : indexAliasesService) {
					if (!indexMetaData.aliases().containsKey(indexAlias.alias())) {
						
						indexAliasesService.remove(indexAlias.alias());
					}
				}
			}
			
			nodeAliasesUpdatedAction.nodeAliasesUpdated(new NodeAliasesUpdatedAction.NodeAliasesUpdatedResponse(event
					.state().nodes().localNodeId(), event.state().version()));
		}
	}

	
	/**
	 * Process alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @param filter the filter
	 * @param indexAliasesService the index aliases service
	 */
	private void processAlias(String index, String alias, CompressedString filter,
			IndexAliasesService indexAliasesService) {
		try {
			if (!indexAliasesService.hasAlias(alias)) {
				if (logger.isDebugEnabled()) {
					logger.debug("[" + index + "] adding alias [{}], filter [{}]", alias, filter);
				}
				indexAliasesService.add(alias, filter);
			} else {
				if ((filter == null && indexAliasesService.alias(alias).filter() != null)
						|| (filter != null && !filter.equals(indexAliasesService.alias(alias).filter()))) {
					if (logger.isDebugEnabled()) {
						logger.debug("[" + index + "] updating alias [{}], filter [{}]", alias, filter);
					}
					indexAliasesService.add(alias, filter);
				}
			}
		} catch (Exception e) {
			logger.warn("[" + index + "] failed to add alias [" + alias + "], filter [" + filter + "]", e);
		}

	}

	
	/**
	 * Apply new or updated shards.
	 *
	 * @param event the event
	 * @throws SumMallSearchException the sum mall search exception
	 */
	private void applyNewOrUpdatedShards(final ClusterChangedEvent event) throws RestartException {
		if (!indicesService.changesAllowed())
			return;

		RoutingTable routingTable = event.state().routingTable();
		RoutingNode routingNodes = event.state().readOnlyRoutingNodes().nodesToShards()
				.get(event.state().nodes().localNodeId());
		if (routingNodes == null) {
			return;
		}
		DiscoveryNodes nodes = event.state().nodes();

		for (final ShardRouting shardRouting : routingNodes) {
			final IndexService indexService = indicesService.indexService(shardRouting.index());
			if (indexService == null) {
				
				continue;
			}

			final int shardId = shardRouting.id();

			if (!indexService.hasShard(shardId) && shardRouting.started()) {
				
				logger.warn(
						"["
								+ shardRouting.index()
								+ "][{}] master [{}] marked shard as started, but shard have not been created, mark shard as failed",
						shardId, nodes.masterNode());
				shardStateAction.shardFailed(shardRouting, "master " + nodes.masterNode()
						+ " marked shard as started, but shard have not been created, mark shard as failed");
				continue;
			}

			if (indexService.hasShard(shardId)) {
				InternalIndexShard indexShard = (InternalIndexShard) indexService.shard(shardId);
				if (!shardRouting.equals(indexShard.routingEntry())) {
					indexShard.routingEntry(shardRouting);
					indexService.shardInjector(shardId).getInstance(IndexShardGatewayService.class)
							.routingStateChanged();
				}
			}

			if (shardRouting.initializing()) {
				applyInitializingShard(routingTable, nodes,
						routingTable.index(shardRouting.index()).shard(shardRouting.id()), shardRouting);
			}
		}
	}

	
	/**
	 * Apply initializing shard.
	 *
	 * @param routingTable the routing table
	 * @param nodes the nodes
	 * @param indexShardRouting the index shard routing
	 * @param shardRouting the shard routing
	 * @throws SumMallSearchException the sum mall search exception
	 */
	private void applyInitializingShard(final RoutingTable routingTable, final DiscoveryNodes nodes,
			final IndexShardRoutingTable indexShardRouting, final ShardRouting shardRouting)
			throws RestartException {
		final IndexService indexService = indicesService.indexServiceSafe(shardRouting.index());
		final int shardId = shardRouting.id();

		if (indexService.hasShard(shardId)) {
			IndexShard indexShard = indexService.shardSafe(shardId);
			if (indexShard.state() == IndexShardState.STARTED) {
				
				
				if (logger.isTraceEnabled()) {
					logger.trace("[{}][{}] master [{}] marked shard as initializing, but shard already created, mark shard as started");
				}
				shardStateAction.shardStarted(shardRouting, "master " + nodes.masterNode()
						+ " marked shard as initializing, but shard already started, mark shard as started");
				return;
			} else {
				if (indexShard.ignoreRecoveryAttempt()) {
					return;
				}
			}
		}
		
		if (!indexService.hasShard(shardId)) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("[{}][{}] creating shard", shardRouting.index(), shardId);
				}
				InternalIndexShard indexShard = (InternalIndexShard) indexService.createShard(shardId);
				indexShard.routingEntry(shardRouting);
				indexShard.engine().addFailedEngineListener(failedEngineHandler);
			} catch (IndexShardAlreadyExistsException e) {
				
			} catch (Exception e) {
				logger.warn("[" + shardRouting.index() + "][" + shardRouting.id() + "] failed to create shard", e);
				try {
					indexService.removeShard(shardId, "failed to create [" + ExceptionsHelper.detailedMessage(e) + "]");
				} catch (IndexShardMissingException e1) {
					
				} catch (Exception e1) {
					logger.warn("[" + shardRouting.index() + "][" + shardRouting.id()
							+ "] failed to remove shard after failed creation", e1);
				}
				shardStateAction.shardFailed(shardRouting,
						"Failed to create shard, message [" + ExceptionsHelper.detailedMessage(e) + "]");
				return;
			} catch (OutOfMemoryError e) {
				logger.warn("[" + shardRouting.index() + "][" + shardRouting.id() + "] failed to create shard", e);
				try {
					indexService.removeShard(shardId, "failed to create [" + ExceptionsHelper.detailedMessage(e) + "]");
				} catch (IndexShardMissingException e1) {
					
				} catch (Exception e1) {
					logger.warn("[" + shardRouting.index() + "][" + shardRouting.id()
							+ "] failed to remove shard after failed creation", e1);
				}
				shardStateAction.shardFailed(shardRouting,
						"Failed to create shard, message [" + ExceptionsHelper.detailedMessage(e) + "]");
				return;
			}
		}
		final InternalIndexShard indexShard = (InternalIndexShard) indexService.shardSafe(shardId);

		if (indexShard.ignoreRecoveryAttempt()) {
			
			
			return;
		}

		if (!shardRouting.primary()) {
			
			IndexShardRoutingTable shardRoutingTable = routingTable.index(shardRouting.index())
					.shard(shardRouting.id());
			for (ShardRouting entry : shardRoutingTable) {
				if (entry.primary() && entry.started()) {
					
					final DiscoveryNode sourceNode = nodes.get(entry.currentNodeId());
					try {
						
						final StartRecoveryRequest request = new StartRecoveryRequest(indexShard.shardId(), sourceNode,
								nodes.localNode(), false, indexShard.store().list());
						recoveryTarget.startRecovery(request, false, new PeerRecoveryListener(request, shardRouting,
								indexService));
					} catch (Exception e) {
						handleRecoveryFailure(indexService, shardRouting, true, e);
						break;
					}
					break;
				}
			}
		} else {
			if (shardRouting.relocatingNodeId() == null) {
				
				
				boolean indexShouldExists = indexShardRouting.allocatedPostApi();
				IndexShardGatewayService shardGatewayService = indexService.shardInjector(shardId).getInstance(
						IndexShardGatewayService.class);
				shardGatewayService.recover(indexShouldExists, new IndexShardGatewayService.RecoveryListener() {
					@Override
					public void onRecoveryDone() {
						shardStateAction.shardStarted(shardRouting, "after recovery from gateway");
					}

					@Override
					public void onIgnoreRecovery(String reason) {
					}

					@Override
					public void onRecoveryFailed(IndexShardGatewayRecoveryException e) {
						handleRecoveryFailure(indexService, shardRouting, true, e);
					}
				});
			} else {
				
				final DiscoveryNode sourceNode = nodes.get(shardRouting.relocatingNodeId());
				try {
					
					
					final StartRecoveryRequest request = new StartRecoveryRequest(indexShard.shardId(), sourceNode,
							nodes.localNode(), false, indexShard.store().list());
					recoveryTarget.startRecovery(request, false, new PeerRecoveryListener(request, shardRouting,
							indexService));
				} catch (Exception e) {
					handleRecoveryFailure(indexService, shardRouting, true, e);
				}
			}
		}
	}

	
	/**
	 * The listener interface for receiving peerRecovery events.
	 * The class that is interested in processing a peerRecovery
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addPeerRecoveryListener<code> method. When
	 * the peerRecovery event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see PeerRecoveryEvent
	 */
	private class PeerRecoveryListener implements RecoveryTarget.RecoveryListener {

		
		/** The request. */
		private final StartRecoveryRequest request;

		
		/** The shard routing. */
		private final ShardRouting shardRouting;

		
		/** The index service. */
		private final IndexService indexService;

		
		/**
		 * Instantiates a new peer recovery listener.
		 *
		 * @param request the request
		 * @param shardRouting the shard routing
		 * @param indexService the index service
		 */
		private PeerRecoveryListener(StartRecoveryRequest request, ShardRouting shardRouting, IndexService indexService) {
			this.request = request;
			this.shardRouting = shardRouting;
			this.indexService = indexService;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.indices.recovery.RecoveryTarget.RecoveryListener#onRecoveryDone()
		 */
		@Override
		public void onRecoveryDone() {
			shardStateAction.shardStarted(shardRouting, "after recovery (replica) from node [" + request.sourceNode()
					+ "]");
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.indices.recovery.RecoveryTarget.RecoveryListener#onRetryRecovery(cn.com.summall.search.commons.unit.TimeValue)
		 */
		@Override
		public void onRetryRecovery(TimeValue retryAfter) {
			threadPool.schedule(retryAfter, ThreadPool.Names.GENERIC, new Runnable() {
				@Override
				public void run() {
					recoveryTarget.startRecovery(request, true, PeerRecoveryListener.this);
				}
			});
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.indices.recovery.RecoveryTarget.RecoveryListener#onIgnoreRecovery(boolean, java.lang.String)
		 */
		@Override
		public void onIgnoreRecovery(boolean removeShard, String reason) {
			if (!removeShard) {
				return;
			}
			synchronized (mutex) {
				if (indexService.hasShard(shardRouting.shardId().id())) {
					if (logger.isDebugEnabled()) {
						logger.debug("[" + shardRouting.index()
								+ "][{}] removing shard on ignored recovery, reason [{}]", shardRouting.shardId().id(),
								reason);
					}
					try {
						indexService.removeShard(shardRouting.shardId().id(), "ignore recovery: " + reason);
					} catch (IndexShardMissingException e) {
						
					} catch (Exception e1) {
						logger.warn("[" + indexService.index().name() + "][" + shardRouting.shardId().id()
								+ "] failed to delete shard after ignore recovery", e1);
					}
				}
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.indices.recovery.RecoveryTarget.RecoveryListener#onRecoveryFailure(cn.com.summall.search.core.indices.recovery.RecoveryFailedException, boolean)
		 */
		@Override
		public void onRecoveryFailure(RecoveryFailedException e, boolean sendShardFailure) {
			handleRecoveryFailure(indexService, shardRouting, sendShardFailure, e);
		}
	}

	
	/**
	 * Handle recovery failure.
	 *
	 * @param indexService the index service
	 * @param shardRouting the shard routing
	 * @param sendShardFailure the send shard failure
	 * @param failure the failure
	 */
	private void handleRecoveryFailure(IndexService indexService, ShardRouting shardRouting, boolean sendShardFailure,
			Throwable failure) {
		logger.warn("[" + indexService.index().name() + "][" + shardRouting.shardId().id() + "] failed to start shard",
				failure);
		synchronized (mutex) {
			if (indexService.hasShard(shardRouting.shardId().id())) {
				try {
					indexService.removeShard(shardRouting.shardId().id(),
							"recovery failure [" + ExceptionsHelper.detailedMessage(failure) + "]");
				} catch (IndexShardMissingException e) {
					
				} catch (Exception e1) {
					logger.warn("[" + indexService.index().name() + "][" + shardRouting.shardId().id()
							+ "] failed to delete shard after failed startup", e1);
				}
			}
			if (sendShardFailure) {
				try {
					shardStateAction.shardFailed(shardRouting,
							"Failed to start shard, message [" + ExceptionsHelper.detailedMessage(failure) + "]");
				} catch (Exception e1) {
					logger.warn("[" + indexService.index().name() + "][" + shardRouting.id()
							+ "] failed to mark shard as failed after a failed start", e1);
				}
			}
		}
	}

	
	/**
	 * The Class FailedEngineHandler.
	 *
	 * @author l.xue.nong
	 */
	private class FailedEngineHandler implements Engine.FailedEngineListener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.FailedEngineListener#onFailedEngine(cn.com.summall.search.core.index.shard.ShardId, java.lang.Throwable)
		 */
		@Override
		public void onFailedEngine(final ShardId shardId, final Throwable failure) {
			ShardRouting shardRouting = null;
			final IndexService indexService = indicesService.indexService(shardId.index().name());
			if (indexService != null) {
				IndexShard indexShard = indexService.shard(shardId.id());
				if (indexShard != null) {
					shardRouting = indexShard.routingEntry();
				}
			}
			if (shardRouting == null) {
				logger.warn("[{}][{}] engine failed, but can't find index shard", shardId.index().name(), shardId.id());
				return;
			}
			final ShardRouting fShardRouting = shardRouting;
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					synchronized (mutex) {
						if (indexService.hasShard(shardId.id())) {
							try {
								indexService.removeShard(shardId.id(),
										"engine failure [" + ExceptionsHelper.detailedMessage(failure) + "]");
							} catch (IndexShardMissingException e) {
								
							} catch (Exception e1) {
								logger.warn("[" + indexService.index().name() + "][" + shardId.id()
										+ "] failed to delete shard after failed engine", e1);
							}
						}
						try {
							shardStateAction.shardFailed(fShardRouting,
									"engine failure, message [" + ExceptionsHelper.detailedMessage(failure) + "]");
						} catch (Exception e1) {
							logger.warn("[" + indexService.index().name() + "][" + shardId.id()
									+ "] failed to mark shard as failed after a failed engine", e1);
						}
					}
				}
			});
		}
	}
}
