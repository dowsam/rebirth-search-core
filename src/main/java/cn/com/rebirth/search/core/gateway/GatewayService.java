/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GatewayService.java 2012-3-29 15:01:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.gateway;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataStateIndexService;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationService;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;
import cn.com.rebirth.search.core.discovery.Discovery;
import cn.com.rebirth.search.core.discovery.DiscoveryService;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.threadpool.ThreadPool;


/**
 * The Class GatewayService.
 *
 * @author l.xue.nong
 */
public class GatewayService extends AbstractLifecycleComponent<GatewayService> implements ClusterStateListener {

	
	/** The Constant STATE_NOT_RECOVERED_BLOCK. */
	public static final ClusterBlock STATE_NOT_RECOVERED_BLOCK = new ClusterBlock(1,
			"state not recovered / initialized", true, true, RestStatus.SERVICE_UNAVAILABLE, ClusterBlockLevel.ALL);

	
	/** The gateway. */
	private final Gateway gateway;

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The allocation service. */
	private final AllocationService allocationService;

	
	/** The cluster service. */
	private final ClusterService clusterService;

	
	/** The discovery service. */
	private final DiscoveryService discoveryService;

	
	/** The recover after time. */
	private final TimeValue recoverAfterTime;

	
	/** The recover after nodes. */
	private final int recoverAfterNodes;

	
	/** The expected nodes. */
	private final int expectedNodes;

	
	/** The recover after data nodes. */
	private final int recoverAfterDataNodes;

	
	/** The expected data nodes. */
	private final int expectedDataNodes;

	
	/** The recover after master nodes. */
	private final int recoverAfterMasterNodes;

	
	/** The expected master nodes. */
	private final int expectedMasterNodes;

	
	/** The recovered. */
	private final AtomicBoolean recovered = new AtomicBoolean();

	
	/** The scheduled recovery. */
	private final AtomicBoolean scheduledRecovery = new AtomicBoolean();

	
	/**
	 * Instantiates a new gateway service.
	 *
	 * @param settings the settings
	 * @param gateway the gateway
	 * @param allocationService the allocation service
	 * @param clusterService the cluster service
	 * @param discoveryService the discovery service
	 * @param threadPool the thread pool
	 */
	@Inject
	public GatewayService(Settings settings, Gateway gateway, AllocationService allocationService,
			ClusterService clusterService, DiscoveryService discoveryService, ThreadPool threadPool) {
		super(settings);
		this.gateway = gateway;
		this.allocationService = allocationService;
		this.clusterService = clusterService;
		this.discoveryService = discoveryService;
		this.threadPool = threadPool;
		
		this.recoverAfterTime = componentSettings.getAsTime("recover_after_time", null);
		this.recoverAfterNodes = componentSettings.getAsInt("recover_after_nodes", -1);
		this.expectedNodes = componentSettings.getAsInt("expected_nodes", -1);
		this.recoverAfterDataNodes = componentSettings.getAsInt("recover_after_data_nodes", -1);
		this.expectedDataNodes = componentSettings.getAsInt("expected_data_nodes", -1);
		
		this.recoverAfterMasterNodes = componentSettings.getAsInt("recover_after_master_nodes",
				settings.getAsInt("discovery.zen.minimum_master_nodes", -1));
		this.expectedMasterNodes = componentSettings.getAsInt("expected_master_nodes", -1);

		
		this.clusterService.addInitialStateBlock(STATE_NOT_RECOVERED_BLOCK);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
		gateway.start();
		if (discoveryService.initialStateReceived()) {
			ClusterState clusterState = clusterService.state();
			DiscoveryNodes nodes = clusterState.nodes();
			if (clusterState.nodes().localNodeMaster()
					&& clusterState.blocks().hasGlobalBlock(STATE_NOT_RECOVERED_BLOCK)) {
				if (clusterState.blocks().hasGlobalBlock(Discovery.NO_MASTER_BLOCK)) {
					logger.debug("not recovering from gateway, no master elected yet");
				} else if (recoverAfterNodes != -1 && (nodes.masterAndDataNodes().size()) < recoverAfterNodes) {
					logger.debug("not recovering from gateway, nodes_size (data+master) ["
							+ nodes.masterAndDataNodes().size() + "] < recover_after_nodes [" + recoverAfterNodes + "]");
				} else if (recoverAfterDataNodes != -1 && nodes.dataNodes().size() < recoverAfterDataNodes) {
					logger.debug("not recovering from gateway, nodes_size (data) [" + nodes.dataNodes().size()
							+ "] < recover_after_data_nodes [" + recoverAfterDataNodes + "]");
				} else if (recoverAfterMasterNodes != -1 && nodes.masterNodes().size() < recoverAfterMasterNodes) {
					logger.debug("not recovering from gateway, nodes_size (master) [" + nodes.masterNodes().size()
							+ "] < recover_after_master_nodes [" + recoverAfterMasterNodes + "]");
				} else {
					boolean ignoreRecoverAfterTime;
					if (expectedNodes == -1 && expectedMasterNodes == -1 && expectedDataNodes == -1) {
						
						ignoreRecoverAfterTime = false;
					} else {
						
						ignoreRecoverAfterTime = true;
						if (expectedNodes != -1 && (nodes.masterAndDataNodes().size() < expectedNodes)) { 
							ignoreRecoverAfterTime = false;
						}
						if (expectedMasterNodes != -1 && (nodes.masterNodes().size() < expectedMasterNodes)) { 
							ignoreRecoverAfterTime = false;
						}
						if (expectedDataNodes != -1 && (nodes.dataNodes().size() < expectedDataNodes)) { 
							ignoreRecoverAfterTime = false;
						}
					}
					performStateRecovery(ignoreRecoverAfterTime);
				}
			}
		} else {
			logger.debug("can't wait on start for (possibly) reading state from gateway, will do it asynchronously");
		}
		clusterService.addLast(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
		clusterService.remove(this);
		gateway.stop();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
		gateway.close();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.summall.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(final ClusterChangedEvent event) {
		if (lifecycle.stoppedOrClosed()) {
			return;
		}
		if (event.state().blocks().hasGlobalBlock(Discovery.NO_MASTER_BLOCK)) {
			
			
			recovered.set(false);
			scheduledRecovery.set(false);
		}
		if (event.localNodeMaster() && event.state().blocks().hasGlobalBlock(STATE_NOT_RECOVERED_BLOCK)) {
			ClusterState clusterState = event.state();
			DiscoveryNodes nodes = clusterState.nodes();
			if (event.state().blocks().hasGlobalBlock(Discovery.NO_MASTER_BLOCK)) {
				logger.debug("not recovering from gateway, no master elected yet");
			} else if (recoverAfterNodes != -1 && (nodes.masterAndDataNodes().size()) < recoverAfterNodes) {
				logger.debug("not recovering from gateway, nodes_size (data+master) ["
						+ nodes.masterAndDataNodes().size() + "] < recover_after_nodes [" + recoverAfterNodes + "]");
			} else if (recoverAfterDataNodes != -1 && nodes.dataNodes().size() < recoverAfterDataNodes) {
				logger.debug("not recovering from gateway, nodes_size (data) [" + nodes.dataNodes().size()
						+ "] < recover_after_data_nodes [" + recoverAfterDataNodes + "]");
			} else if (recoverAfterMasterNodes != -1 && nodes.masterNodes().size() < recoverAfterMasterNodes) {
				logger.debug("not recovering from gateway, nodes_size (master) [" + nodes.masterNodes().size()
						+ "] < recover_after_master_nodes [" + recoverAfterMasterNodes + "]");
			} else {
				boolean ignoreRecoverAfterTime;
				if (expectedNodes == -1 && expectedMasterNodes == -1 && expectedDataNodes == -1) {
					
					ignoreRecoverAfterTime = false;
				} else {
					
					ignoreRecoverAfterTime = true;
					if (expectedNodes != -1 && (nodes.masterAndDataNodes().size() < expectedNodes)) { 
						ignoreRecoverAfterTime = false;
					}
					if (expectedMasterNodes != -1 && (nodes.masterNodes().size() < expectedMasterNodes)) { 
						ignoreRecoverAfterTime = false;
					}
					if (expectedDataNodes != -1 && (nodes.dataNodes().size() < expectedDataNodes)) { 
						ignoreRecoverAfterTime = false;
					}
				}
				final boolean fIgnoreRecoverAfterTime = ignoreRecoverAfterTime;
				threadPool.generic().execute(new Runnable() {
					@Override
					public void run() {
						performStateRecovery(fIgnoreRecoverAfterTime);
					}
				});
			}
		}
	}

	
	/**
	 * Perform state recovery.
	 *
	 * @param ignoreRecoverAfterTime the ignore recover after time
	 */
	private void performStateRecovery(boolean ignoreRecoverAfterTime) {
		final Gateway.GatewayStateRecoveredListener recoveryListener = new GatewayRecoveryListener(
				new CountDownLatch(1));

		if (!ignoreRecoverAfterTime && recoverAfterTime != null) {
			if (scheduledRecovery.compareAndSet(false, true)) {
				logger.debug("delaying initial state recovery for [{}]", recoverAfterTime);
				threadPool.schedule(recoverAfterTime, ThreadPool.Names.GENERIC, new Runnable() {
					@Override
					public void run() {
						if (recovered.compareAndSet(false, true)) {
							gateway.performStateRecovery(recoveryListener);
						}
					}
				});
			}
		} else {
			if (recovered.compareAndSet(false, true)) {
				gateway.performStateRecovery(recoveryListener);
			}
		}
	}

	
	/**
	 * The listener interface for receiving gatewayRecovery events.
	 * The class that is interested in processing a gatewayRecovery
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addGatewayRecoveryListener<code> method. When
	 * the gatewayRecovery event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see GatewayRecoveryEvent
	 */
	class GatewayRecoveryListener implements Gateway.GatewayStateRecoveredListener {

		
		/** The latch. */
		private final CountDownLatch latch;

		
		/**
		 * Instantiates a new gateway recovery listener.
		 *
		 * @param latch the latch
		 */
		GatewayRecoveryListener(CountDownLatch latch) {
			this.latch = latch;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.gateway.Gateway.GatewayStateRecoveredListener#onSuccess(cn.com.summall.search.core.cluster.ClusterState)
		 */
		@Override
		public void onSuccess(final ClusterState recoveredState) {
			clusterService.submitStateUpdateTask("local-gateway-elected-state", new ProcessedClusterStateUpdateTask() {
				@Override
				public ClusterState execute(ClusterState currentState) {
					assert currentState.metaData().indices().isEmpty();

					
					ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks())
							.blocks(recoveredState.blocks()).removeGlobalBlock(STATE_NOT_RECOVERED_BLOCK);

					MetaData.Builder metaDataBuilder = MetaData.newMetaDataBuilder()
							.metaData(recoveredState.metaData());

					if (recoveredState.metaData().settings().getAsBoolean(MetaData.SETTING_READ_ONLY, false)
							|| currentState.metaData().settings().getAsBoolean(MetaData.SETTING_READ_ONLY, false)) {
						blocks.addGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);
					}

					for (IndexMetaData indexMetaData : recoveredState.metaData()) {
						metaDataBuilder.put(indexMetaData, false);
						if (indexMetaData.state() == IndexMetaData.State.CLOSE) {
							blocks.addIndexBlock(indexMetaData.index(), MetaDataStateIndexService.INDEX_CLOSED_BLOCK);
						}
						if (indexMetaData.settings().getAsBoolean(IndexMetaData.SETTING_READ_ONLY, false)) {
							blocks.addIndexBlock(indexMetaData.index(), IndexMetaData.INDEX_READ_ONLY_BLOCK);
						}
						if (indexMetaData.settings().getAsBoolean(IndexMetaData.SETTING_BLOCKS_READ, false)) {
							blocks.addIndexBlock(indexMetaData.index(), IndexMetaData.INDEX_READ_BLOCK);
						}
						if (indexMetaData.settings().getAsBoolean(IndexMetaData.SETTING_BLOCKS_WRITE, false)) {
							blocks.addIndexBlock(indexMetaData.index(), IndexMetaData.INDEX_WRITE_BLOCK);
						}
						if (indexMetaData.settings().getAsBoolean(IndexMetaData.SETTING_BLOCKS_METADATA, false)) {
							blocks.addIndexBlock(indexMetaData.index(), IndexMetaData.INDEX_METADATA_BLOCK);
						}
					}

					
					ClusterState updatedState = ClusterState.newClusterStateBuilder().state(currentState)
							.blocks(blocks).metaData(metaDataBuilder).build();

					
					RoutingTable.Builder routingTableBuilder = RoutingTable.builder().routingTable(
							updatedState.routingTable());
					for (IndexMetaData indexMetaData : updatedState.metaData().indices().values()) {
						if (indexMetaData.state() == IndexMetaData.State.OPEN) {
							IndexRoutingTable.Builder indexRoutingBuilder = new IndexRoutingTable.Builder(indexMetaData
									.index()).initializeEmpty(updatedState.metaData().index(indexMetaData.index()),
									false );
							routingTableBuilder.add(indexRoutingBuilder);
						}
					}
					
					routingTableBuilder.version(0);

					
					RoutingAllocation.Result routingResult = allocationService.reroute(ClusterState
							.newClusterStateBuilder().state(updatedState).routingTable(routingTableBuilder).build());

					return ClusterState.newClusterStateBuilder().state(updatedState).routingResult(routingResult)
							.build();
				}

				@Override
				public void clusterStateProcessed(ClusterState clusterState) {
					logger.info("recovered [{}] indices into cluster_state", clusterState.metaData().indices().size());
					latch.countDown();
				}
			});
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.gateway.Gateway.GatewayStateRecoveredListener#onFailure(java.lang.String)
		 */
		@Override
		public void onFailure(String message) {
			recovered.set(false);
			scheduledRecovery.set(false);
			
			logger.info("metadata state not restored, reason: {}", message);
		}
	}
}
