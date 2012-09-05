/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MetaDataUpdateSettingsService.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.metadata;

import static cn.com.rebirth.search.core.cluster.ClusterState.newClusterStateBuilder;

import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.Booleans;
import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationService;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;

import com.google.common.collect.Sets;

/**
 * The Class MetaDataUpdateSettingsService.
 *
 * @author l.xue.nong
 */
public class MetaDataUpdateSettingsService extends AbstractComponent implements ClusterStateListener {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The allocation service. */
	private final AllocationService allocationService;

	/**
	 * Instantiates a new meta data update settings service.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param allocationService the allocation service
	 */
	@Inject
	public MetaDataUpdateSettingsService(Settings settings, ClusterService clusterService,
			AllocationService allocationService) {
		super(settings);
		this.clusterService = clusterService;
		this.clusterService.add(this);
		this.allocationService = allocationService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.rebirth.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(ClusterChangedEvent event) {

		if (!event.state().nodes().localNodeMaster()) {
			return;
		}

		for (final IndexMetaData indexMetaData : event.state().metaData()) {
			String autoExpandReplicas = indexMetaData.settings().get(IndexMetaData.SETTING_AUTO_EXPAND_REPLICAS);
			if (autoExpandReplicas != null && Booleans.parseBoolean(autoExpandReplicas, true)) {
				try {
					int min;
					int max;
					try {
						min = Integer.parseInt(autoExpandReplicas.substring(0, autoExpandReplicas.indexOf('-')));
						String sMax = autoExpandReplicas.substring(autoExpandReplicas.indexOf('-') + 1);
						if (sMax.equals("all")) {
							max = event.state().nodes().dataNodes().size() - 1;
						} else {
							max = Integer.parseInt(sMax);
						}
					} catch (Exception e) {
						logger.warn("failed to set [" + IndexMetaData.SETTING_AUTO_EXPAND_REPLICAS
								+ "], wrong format [" + autoExpandReplicas + "]", e);
						continue;
					}

					int numberOfReplicas = event.state().nodes().dataNodes().size() - 1;
					if (numberOfReplicas < min) {
						numberOfReplicas = min;
					} else if (numberOfReplicas > max) {
						numberOfReplicas = max;
					}

					if (numberOfReplicas == indexMetaData.numberOfReplicas()) {
						continue;
					}

					if (numberOfReplicas >= min && numberOfReplicas <= max) {
						final int fNumberOfReplicas = numberOfReplicas;
						Settings settings = ImmutableSettings.settingsBuilder()
								.put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, fNumberOfReplicas).build();
						updateSettings(settings, new String[] { indexMetaData.index() }, new Listener() {
							@Override
							public void onSuccess() {
								logger.info("[{}] auto expanded replicas to [{}]", indexMetaData.index(),
										fNumberOfReplicas);
							}

							@Override
							public void onFailure(Throwable t) {
								logger.warn("[{}] fail to auto expand replicas to [{}]", indexMetaData.index(),
										fNumberOfReplicas);
							}
						});
					}
				} catch (Exception e) {
					logger.warn("[{}] failed to parse auto expand replicas", e, indexMetaData.index());
				}
			}
		}
	}

	/**
	 * Update settings.
	 *
	 * @param pSettings the settings
	 * @param indices the indices
	 * @param listener the listener
	 */
	public void updateSettings(final Settings pSettings, final String[] indices, final Listener listener) {
		ImmutableSettings.Builder updatedSettingsBuilder = ImmutableSettings.settingsBuilder();
		for (Map.Entry<String, String> entry : pSettings.getAsMap().entrySet()) {
			if (entry.getKey().equals("index")) {
				continue;
			}
			if (!entry.getKey().startsWith("index.")) {
				updatedSettingsBuilder.put("index." + entry.getKey(), entry.getValue());
			} else {
				updatedSettingsBuilder.put(entry.getKey(), entry.getValue());
			}
		}

		for (String key : updatedSettingsBuilder.internalMap().keySet()) {
			if (key.equals(IndexMetaData.SETTING_NUMBER_OF_SHARDS)) {
				listener.onFailure(new RebirthIllegalArgumentException("can't change the number of shards for an index"));
				return;
			}
		}

		final Settings closeSettings = updatedSettingsBuilder.build();

		final Set<String> removedSettings = Sets.newHashSet();
		for (String key : updatedSettingsBuilder.internalMap().keySet()) {
			if (!IndexMetaData.hasDynamicSetting(key)) {
				removedSettings.add(key);
			}
		}
		if (!removedSettings.isEmpty()) {
			for (String removedSetting : removedSettings) {
				updatedSettingsBuilder.remove(removedSetting);
			}
		}
		final Settings openSettings = updatedSettingsBuilder.build();

		clusterService.submitStateUpdateTask("update-settings", new ProcessedClusterStateUpdateTask() {
			@Override
			public ClusterState execute(ClusterState currentState) {
				try {
					String[] actualIndices = currentState.metaData().concreteIndices(indices);
					RoutingTable.Builder routingTableBuilder = RoutingTable.builder().routingTable(
							currentState.routingTable());
					MetaData.Builder metaDataBuilder = MetaData.newMetaDataBuilder().metaData(currentState.metaData());

					int updatedNumberOfReplicas = openSettings.getAsInt(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, -1);
					if (updatedNumberOfReplicas != -1) {
						routingTableBuilder.updateNumberOfReplicas(updatedNumberOfReplicas, actualIndices);
						metaDataBuilder.updateNumberOfReplicas(updatedNumberOfReplicas, actualIndices);
						logger.info("updating number_of_replicas to [{}] for indices {}", updatedNumberOfReplicas,
								actualIndices);
					}

					ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks());
					Boolean updatedReadOnly = openSettings.getAsBoolean(IndexMetaData.SETTING_READ_ONLY, null);
					if (updatedReadOnly != null) {
						for (String index : actualIndices) {
							if (updatedReadOnly) {
								blocks.addIndexBlock(index, IndexMetaData.INDEX_READ_ONLY_BLOCK);
							} else {
								blocks.removeIndexBlock(index, IndexMetaData.INDEX_READ_ONLY_BLOCK);
							}
						}
					}
					Boolean updateMetaDataBlock = openSettings
							.getAsBoolean(IndexMetaData.SETTING_BLOCKS_METADATA, null);
					if (updateMetaDataBlock != null) {
						for (String index : actualIndices) {
							if (updateMetaDataBlock) {
								blocks.addIndexBlock(index, IndexMetaData.INDEX_METADATA_BLOCK);
							} else {
								blocks.removeIndexBlock(index, IndexMetaData.INDEX_METADATA_BLOCK);
							}
						}
					}

					Boolean updateWriteBlock = openSettings.getAsBoolean(IndexMetaData.SETTING_BLOCKS_WRITE, null);
					if (updateWriteBlock != null) {
						for (String index : actualIndices) {
							if (updateWriteBlock) {
								blocks.addIndexBlock(index, IndexMetaData.INDEX_WRITE_BLOCK);
							} else {
								blocks.removeIndexBlock(index, IndexMetaData.INDEX_WRITE_BLOCK);
							}
						}
					}

					Boolean updateReadBlock = openSettings.getAsBoolean(IndexMetaData.SETTING_BLOCKS_READ, null);
					if (updateReadBlock != null) {
						for (String index : actualIndices) {
							if (updateReadBlock) {
								blocks.addIndexBlock(index, IndexMetaData.INDEX_READ_BLOCK);
							} else {
								blocks.removeIndexBlock(index, IndexMetaData.INDEX_READ_BLOCK);
							}
						}
					}

					Set<String> openIndices = Sets.newHashSet();
					Set<String> closeIndices = Sets.newHashSet();
					for (String index : actualIndices) {
						if (currentState.metaData().index(index).state() == IndexMetaData.State.OPEN) {
							openIndices.add(index);
						} else {
							closeIndices.add(index);
						}
					}

					if (!openIndices.isEmpty()) {
						String[] indices = openIndices.toArray(new String[openIndices.size()]);
						if (!removedSettings.isEmpty()) {
							logger.warn("{} ignoring non dynamic index level settings for open indices: {}", indices,
									removedSettings);
						}
						metaDataBuilder.updateSettings(openSettings, indices);
					}

					if (!closeIndices.isEmpty()) {
						String[] indices = closeIndices.toArray(new String[closeIndices.size()]);
						metaDataBuilder.updateSettings(closeSettings, indices);
					}

					ClusterState updatedState = ClusterState.builder().state(currentState).metaData(metaDataBuilder)
							.routingTable(routingTableBuilder).blocks(blocks).build();

					RoutingAllocation.Result routingResult = allocationService.reroute(updatedState);
					updatedState = newClusterStateBuilder().state(updatedState).routingResult(routingResult).build();

					return updatedState;
				} catch (Exception e) {
					listener.onFailure(e);
					return currentState;
				}
			}

			@Override
			public void clusterStateProcessed(ClusterState clusterState) {
				listener.onSuccess();
			}
		});
	}

	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		/**
		 * On success.
		 */
		void onSuccess();

		/**
		 * On failure.
		 *
		 * @param t the t
		 */
		void onFailure(Throwable t);
	}
}
