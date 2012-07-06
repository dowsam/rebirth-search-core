/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core LocalGatewayShardsState.java 2012-3-29 15:02:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.gateway.local.state.shards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.compress.lzf.LZF;
import cn.com.rebirth.commons.io.stream.CachedStreamInput;
import cn.com.rebirth.commons.io.stream.LZFStreamInput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.FileSystemUtils;
import cn.com.rebirth.search.commons.io.Streams;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.search.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentHelper;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.IndexShardRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.MutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRoutingState;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;


/**
 * The Class LocalGatewayShardsState.
 *
 * @author l.xue.nong
 */
public class LocalGatewayShardsState extends AbstractComponent implements ClusterStateListener {

	
	/** The node env. */
	private final NodeEnvironment nodeEnv;

	
	/** The current state. */
	private volatile Map<ShardId, ShardStateInfo> currentState = Maps.newHashMap();

	
	/**
	 * Instantiates a new local gateway shards state.
	 *
	 * @param settings the settings
	 * @param nodeEnv the node env
	 * @param listGatewayStartedShards the list gateway started shards
	 * @throws Exception the exception
	 */
	@Inject
	public LocalGatewayShardsState(Settings settings, NodeEnvironment nodeEnv,
			TransportNodesListGatewayStartedShards listGatewayStartedShards) throws Exception {
		super(settings);
		this.nodeEnv = nodeEnv;
		listGatewayStartedShards.initGateway(this);

		if (DiscoveryNode.dataNode(settings)) {
			try {
				pre019Upgrade();
				long start = System.currentTimeMillis();
				loadStartedShards();
				logger.debug("took {} to load started shards state",
						TimeValue.timeValueMillis(System.currentTimeMillis() - start));
			} catch (Exception e) {
				logger.error("failed to read local state (started shards), exiting...", e);
				throw e;
			}
		}
	}

	
	/**
	 * Current started shards.
	 *
	 * @return the map
	 */
	public Map<ShardId, ShardStateInfo> currentStartedShards() {
		return this.currentState;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.summall.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(ClusterChangedEvent event) {
		if (event.state().blocks().disableStatePersistence()) {
			return;
		}

		if (!event.state().nodes().localNode().dataNode()) {
			return;
		}

		if (!event.routingTableChanged()) {
			return;
		}

		Map<ShardId, ShardStateInfo> newState = Maps.newHashMap();
		newState.putAll(this.currentState);

		
		
		
		
		for (IndexRoutingTable indexRoutingTable : event.state().routingTable()) {
			for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {
				if (indexShardRoutingTable.countWithState(ShardRoutingState.STARTED) == indexShardRoutingTable.size()) {
					newState.remove(indexShardRoutingTable.shardId());
				}
			}
		}
		
		for (ShardId shardId : currentState.keySet()) {
			if (!event.state().metaData().hasIndex(shardId.index().name())) {
				newState.remove(shardId);
			}
		}
		
		RoutingNode routingNode = event.state().readOnlyRoutingNodes().node(event.state().nodes().localNodeId());
		if (routingNode != null) {
			
			for (MutableShardRouting shardRouting : routingNode) {
				if (shardRouting.active()) {
					newState.put(shardRouting.shardId(),
							new ShardStateInfo(shardRouting.version(), shardRouting.primary()));
				}
			}
		}

		
		for (Iterator<Map.Entry<ShardId, ShardStateInfo>> it = newState.entrySet().iterator(); it.hasNext();) {
			Map.Entry<ShardId, ShardStateInfo> entry = it.next();
			ShardId shardId = entry.getKey();
			ShardStateInfo shardStateInfo = entry.getValue();

			String writeReason = null;
			ShardStateInfo currentShardStateInfo = currentState.get(shardId);
			if (currentShardStateInfo == null) {
				writeReason = "freshly started, version [" + shardStateInfo.version + "]";
			} else if (currentShardStateInfo.version != shardStateInfo.version) {
				writeReason = "version changed from [" + currentShardStateInfo.version + "] to ["
						+ shardStateInfo.version + "]";
			}

			
			if (writeReason == null) {
				continue;
			}

			try {
				writeShardState(writeReason, shardId, shardStateInfo, currentShardStateInfo);
			} catch (Exception e) {
				
				
				it.remove();
			}
		}

		
		for (Map.Entry<ShardId, ShardStateInfo> entry : currentState.entrySet()) {
			ShardId shardId = entry.getKey();
			if (!newState.containsKey(shardId)) {
				deleteShardState(shardId);
			}
		}

		this.currentState = newState;
	}

	
	/**
	 * Load started shards.
	 *
	 * @throws Exception the exception
	 */
	private void loadStartedShards() throws Exception {
		Set<ShardId> shardIds = nodeEnv.findAllShardIds();
		long highestVersion = -1;
		Map<ShardId, ShardStateInfo> shardsState = Maps.newHashMap();
		for (ShardId shardId : shardIds) {
			long highestShardVersion = -1;
			ShardStateInfo highestShardState = null;
			for (File shardLocation : nodeEnv.shardLocations(shardId)) {
				File shardStateDir = new File(shardLocation, "_state");
				if (!shardStateDir.exists() || !shardStateDir.isDirectory()) {
					continue;
				}
				
				File[] stateFiles = shardStateDir.listFiles();
				if (stateFiles == null) {
					continue;
				}
				for (File stateFile : stateFiles) {
					if (!stateFile.getName().startsWith("state-")) {
						continue;
					}
					try {
						long version = Long.parseLong(stateFile.getName().substring("state-".length()));
						if (version > highestShardVersion) {
							byte[] data = Streams.copyToByteArray(new FileInputStream(stateFile));
							if (data.length == 0) {
								logger.debug("[{}][{}]: not data for [" + stateFile.getAbsolutePath()
										+ "], ignoring...", shardId.index().name(), shardId.id());
								continue;
							}
							ShardStateInfo readState = readShardState(data);
							if (readState == null) {
								logger.debug("[{}][{}]: not data for [" + stateFile.getAbsolutePath()
										+ "], ignoring...", shardId.index().name(), shardId.id());
								continue;
							}
							assert readState.version == version;
							highestShardState = readState;
							highestShardVersion = version;
						}
					} catch (Exception e) {
						logger.debug("[" + shardId.index().name() + "][" + shardId.id() + "]: failed to read ["
								+ stateFile.getAbsolutePath() + "], ignoring...", e);
					}
				}
			}
			
			if (highestShardState == null) {
				continue;
			}

			shardsState.put(shardId, highestShardState);

			
			if (highestShardVersion > highestVersion) {
				highestVersion = highestShardVersion;
			}
		}
		
		if (highestVersion != -1) {
			currentState = shardsState;
		}
	}

	
	/**
	 * Read shard state.
	 *
	 * @param data the data
	 * @return the shard state info
	 * @throws Exception the exception
	 */
	@Nullable
	private ShardStateInfo readShardState(byte[] data) throws Exception {
		XContentParser parser = null;
		try {
			parser = XContentHelper.createParser(data, 0, data.length);
			XContentParser.Token token = parser.nextToken();
			if (token == null) {
				return null;
			}
			long version = -1;
			Boolean primary = null;
			String currentFieldName = null;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token.isValue()) {
					if ("version".equals(currentFieldName)) {
						version = parser.longValue();
					} else if ("primary".equals(currentFieldName)) {
						primary = parser.booleanValue();
					}
				}
			}
			return new ShardStateInfo(version, primary);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
	}

	
	/**
	 * Write shard state.
	 *
	 * @param reason the reason
	 * @param shardId the shard id
	 * @param shardStateInfo the shard state info
	 * @param previousStateInfo the previous state info
	 * @throws Exception the exception
	 */
	private void writeShardState(String reason, ShardId shardId, ShardStateInfo shardStateInfo,
			@Nullable ShardStateInfo previousStateInfo) throws Exception {
		logger.info("[" + shardId.index().name() + "][" + shardId.id() + "] writing shard state, reason [{}]", reason);
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON, cachedEntry.cachedBytes());
			builder.prettyPrint();
			builder.startObject();
			builder.field("version", shardStateInfo.version);
			if (shardStateInfo.primary != null) {
				builder.field("primary", shardStateInfo.primary);
			}
			builder.endObject();
			builder.flush();

			Exception lastFailure = null;
			boolean wroteAtLeastOnce = false;
			for (File shardLocation : nodeEnv.shardLocations(shardId)) {
				File shardStateDir = new File(shardLocation, "_state");
				FileSystemUtils.mkdirs(shardStateDir);
				File stateFile = new File(shardStateDir, "state-" + shardStateInfo.version);

				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(stateFile);
					fos.write(cachedEntry.bytes().underlyingBytes(), 0, cachedEntry.bytes().size());
					fos.getChannel().force(true);
					Closeables.closeQuietly(fos);
					wroteAtLeastOnce = true;
				} catch (Exception e) {
					lastFailure = e;
				} finally {
					Closeables.closeQuietly(fos);
				}
			}

			if (!wroteAtLeastOnce) {
				logger.warn("[" + shardId.index().name() + "][" + shardId.id() + "]: failed to write shard state",
						lastFailure);
				throw new IOException("failed to write shard state for " + shardId, lastFailure);
			}

			
			if (previousStateInfo != null && previousStateInfo.version != shardStateInfo.version) {
				for (File shardLocation : nodeEnv.shardLocations(shardId)) {
					File stateFile = new File(new File(shardLocation, "_state"), "state-" + previousStateInfo.version);
					stateFile.delete();
				}
			}
		} finally {
			CachedStreamOutput.pushEntry(cachedEntry);
		}
	}

	
	/**
	 * Delete shard state.
	 *
	 * @param shardId the shard id
	 */
	private void deleteShardState(ShardId shardId) {
		logger.trace("[{}][{}] delete shard state", shardId.index().name(), shardId.id());
		File[] shardLocations = nodeEnv.shardLocations(shardId);
		for (File shardLocation : shardLocations) {
			if (!shardLocation.exists()) {
				continue;
			}
			FileSystemUtils.deleteRecursively(new File(shardLocation, "_state"));
		}
	}

	
	/**
	 * Pre019 upgrade.
	 *
	 * @throws Exception the exception
	 */
	private void pre019Upgrade() throws Exception {
		long index = -1;
		File latest = null;
		for (File dataLocation : nodeEnv.nodeDataLocations()) {
			File stateLocation = new File(dataLocation, "_state");
			if (!stateLocation.exists()) {
				continue;
			}
			File[] stateFiles = stateLocation.listFiles();
			if (stateFiles == null) {
				continue;
			}
			for (File stateFile : stateFiles) {
				if (logger.isTraceEnabled()) {
					logger.trace("[find_latest_state]: processing [" + stateFile.getName() + "]");
				}
				String name = stateFile.getName();
				if (!name.startsWith("shards-")) {
					continue;
				}
				long fileIndex = Long.parseLong(name.substring(name.indexOf('-') + 1));
				if (fileIndex >= index) {
					
					try {
						byte[] data = Streams.copyToByteArray(new FileInputStream(stateFile));
						if (data.length == 0) {
							logger.debug("[upgrade]: not data for [" + name + "], ignoring...");
						}
						pre09ReadState(data);
						index = fileIndex;
						latest = stateFile;
					} catch (IOException e) {
						logger.warn("[upgrade]: failed to read state from [" + name + "], ignoring...", e);
					}
				}
			}
		}
		if (latest == null) {
			return;
		}

		logger.info(
				"found old shards state, loading started shards from [{}] and converting to new shards state locations...",
				latest.getAbsolutePath());
		Map<ShardId, ShardStateInfo> shardsState = pre09ReadState(Streams.copyToByteArray(new FileInputStream(latest)));

		for (Map.Entry<ShardId, ShardStateInfo> entry : shardsState.entrySet()) {
			writeShardState("upgrade", entry.getKey(), entry.getValue(), null);
		}

		
		File backupFile = new File(latest.getParentFile(), "backup-" + latest.getName());
		if (!latest.renameTo(backupFile)) {
			throw new IOException("failed to rename old state to backup state [" + latest.getAbsolutePath() + "]");
		}

		
		for (File dataLocation : nodeEnv.nodeDataLocations()) {
			File stateLocation = new File(dataLocation, "_state");
			if (!stateLocation.exists()) {
				continue;
			}
			File[] stateFiles = stateLocation.listFiles();
			if (stateFiles == null) {
				continue;
			}
			for (File stateFile : stateFiles) {
				String name = stateFile.getName();
				if (!name.startsWith("shards-")) {
					continue;
				}
				stateFile.delete();
			}
		}

		logger.info("conversion to new shards state location and format done, backup create at [{}]",
				backupFile.getAbsolutePath());
	}

	
	/**
	 * Pre09 read state.
	 *
	 * @param data the data
	 * @return the map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Map<ShardId, ShardStateInfo> pre09ReadState(byte[] data) throws IOException {
		XContentParser parser = null;
		try {
			Map<ShardId, ShardStateInfo> shardsState = Maps.newHashMap();

			if (LZF.isCompressed(data)) {
				BytesStreamInput siBytes = new BytesStreamInput(data, false);
				LZFStreamInput siLzf = CachedStreamInput.cachedLzf(siBytes);
				parser = XContentFactory.xContent(XContentType.JSON).createParser(siLzf);
			} else {
				parser = XContentFactory.xContent(XContentType.JSON).createParser(data);
			}

			String currentFieldName = null;
			XContentParser.Token token = parser.nextToken();
			if (token == null) {
				
				return shardsState;
			}
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token == XContentParser.Token.START_ARRAY) {
					if ("shards".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
							if (token == XContentParser.Token.START_OBJECT) {
								String shardIndex = null;
								int shardId = -1;
								long version = -1;
								while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
									if (token == XContentParser.Token.FIELD_NAME) {
										currentFieldName = parser.currentName();
									} else if (token.isValue()) {
										if ("index".equals(currentFieldName)) {
											shardIndex = parser.text();
										} else if ("id".equals(currentFieldName)) {
											shardId = parser.intValue();
										} else if ("version".equals(currentFieldName)) {
											version = parser.longValue();
										}
									}
								}
								shardsState.put(new ShardId(shardIndex, shardId), new ShardStateInfo(version, null));
							}
						}
					}
				}
			}
			return shardsState;
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
	}
}
