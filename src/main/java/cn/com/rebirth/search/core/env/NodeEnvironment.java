/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeEnvironment.java 2012-7-6 14:28:51 l.xue.nong$$
 */

package cn.com.rebirth.search.core.env;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.lucene.store.Lock;
import org.apache.lucene.store.NativeFSLockFactory;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.io.FileSystemUtils;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

/**
 * The Class NodeEnvironment.
 *
 * @author l.xue.nong
 */
public class NodeEnvironment extends AbstractComponent {

	/** The node files. */
	private final File[] nodeFiles;

	/** The node indices locations. */
	private final File[] nodeIndicesLocations;

	/** The locks. */
	private final Lock[] locks;

	/** The local node id. */
	private final int localNodeId;

	/**
	 * Instantiates a new node environment.
	 *
	 * @param settings the settings
	 * @param environment the environment
	 */
	@Inject
	public NodeEnvironment(Settings settings, Environment environment) {
		super(settings);

		if (!DiscoveryNode.nodeRequiresLocalStorage(settings)) {
			nodeFiles = null;
			nodeIndicesLocations = null;
			locks = null;
			localNodeId = -1;
			return;
		}

		File[] nodesFiles = new File[environment.dataWithClusterFiles().length];
		Lock[] locks = new Lock[environment.dataWithClusterFiles().length];
		int localNodeId = -1;
		IOException lastException = null;
		int maxLocalStorageNodes = settings.getAsInt("node.max_local_storage_nodes", 50);
		for (int possibleLockId = 0; possibleLockId < maxLocalStorageNodes; possibleLockId++) {
			for (int dirIndex = 0; dirIndex < environment.dataWithClusterFiles().length; dirIndex++) {
				File dir = new File(new File(environment.dataWithClusterFiles()[dirIndex], "nodes"),
						Integer.toString(possibleLockId));
				if (!dir.exists()) {
					FileSystemUtils.mkdirs(dir);
				}
				logger.trace("obtaining node lock on {} ...", dir.getAbsolutePath());
				try {
					NativeFSLockFactory lockFactory = new NativeFSLockFactory(dir);
					Lock tmpLock = lockFactory.makeLock("node.lock");
					boolean obtained = tmpLock.obtain();
					if (obtained) {
						locks[dirIndex] = tmpLock;
						nodesFiles[dirIndex] = dir;
						localNodeId = possibleLockId;
					} else {
						logger.trace("failed to obtain node lock on {}", dir.getAbsolutePath());

						for (int i = 0; i < locks.length; i++) {
							if (locks[i] != null) {
								try {
									locks[i].release();
								} catch (Exception e1) {

								}
							}
							locks[i] = null;
						}
						break;
					}
				} catch (IOException e) {
					logger.trace("failed to obtain node lock on {}", e, dir.getAbsolutePath());
					lastException = new IOException("failed to obtain lock on " + dir.getAbsolutePath(), e);

					for (int i = 0; i < locks.length; i++) {
						if (locks[i] != null) {
							try {
								locks[i].release();
							} catch (Exception e1) {

							}
						}
						locks[i] = null;
					}
					break;
				}
			}
			if (locks[0] != null) {

				break;
			}
		}
		if (locks[0] == null) {
			throw new RebirthIllegalStateException("Failed to obtain node lock, is the following location writable?: "
					+ Arrays.toString(environment.dataWithClusterFiles()), lastException);
		}

		this.localNodeId = localNodeId;
		this.locks = locks;
		this.nodeFiles = nodesFiles;
		if (logger.isDebugEnabled()) {
			logger.debug("using node location [{}], local_node_id [{}]", nodesFiles, localNodeId);
		}
		if (logger.isTraceEnabled()) {
			StringBuilder sb = new StringBuilder("node data locations details:\n");
			for (File file : nodesFiles) {
				sb.append(" -> ").append(file.getAbsolutePath()).append(", free_space [")
						.append(new ByteSizeValue(file.getFreeSpace())).append("], usable_space [")
						.append(new ByteSizeValue(file.getUsableSpace())).append("]\n");
			}
			logger.trace(sb.toString());
		}

		this.nodeIndicesLocations = new File[nodeFiles.length];
		for (int i = 0; i < nodeFiles.length; i++) {
			nodeIndicesLocations[i] = new File(nodeFiles[i], "indices");
		}
	}

	/**
	 * Local node id.
	 *
	 * @return the int
	 */
	public int localNodeId() {
		return this.localNodeId;
	}

	/**
	 * Checks for node file.
	 *
	 * @return true, if successful
	 */
	public boolean hasNodeFile() {
		return nodeFiles != null && locks != null;
	}

	/**
	 * Node data locations.
	 *
	 * @return the file[]
	 */
	public File[] nodeDataLocations() {
		return nodeFiles;
	}

	/**
	 * Indices locations.
	 *
	 * @return the file[]
	 */
	public File[] indicesLocations() {
		return nodeIndicesLocations;
	}

	/**
	 * Index locations.
	 *
	 * @param index the index
	 * @return the file[]
	 */
	public File[] indexLocations(Index index) {
		File[] indexLocations = new File[nodeFiles.length];
		for (int i = 0; i < nodeFiles.length; i++) {
			indexLocations[i] = new File(new File(nodeFiles[i], "indices"), index.name());
		}
		return indexLocations;
	}

	/**
	 * Shard locations.
	 *
	 * @param shardId the shard id
	 * @return the file[]
	 */
	public File[] shardLocations(ShardId shardId) {
		File[] shardLocations = new File[nodeFiles.length];
		for (int i = 0; i < nodeFiles.length; i++) {
			shardLocations[i] = new File(new File(new File(nodeFiles[i], "indices"), shardId.index().name()),
					Integer.toString(shardId.id()));
		}
		return shardLocations;
	}

	/**
	 * Find all indices.
	 *
	 * @return the sets the
	 * @throws Exception the exception
	 */
	public Set<String> findAllIndices() throws Exception {
		if (nodeFiles == null || locks == null) {
			throw new RebirthIllegalStateException("node is not configured to store local location");
		}
		Set<String> indices = Sets.newHashSet();
		for (File indicesLocation : nodeIndicesLocations) {
			File[] indicesList = indicesLocation.listFiles();
			if (indicesList == null) {
				continue;
			}
			for (File indexLocation : indicesList) {
				if (indexLocation.isDirectory()) {
					indices.add(indexLocation.getName());
				}
			}
		}
		return indices;
	}

	/**
	 * Find all shard ids.
	 *
	 * @return the sets the
	 * @throws Exception the exception
	 */
	public Set<ShardId> findAllShardIds() throws Exception {
		if (nodeFiles == null || locks == null) {
			throw new RebirthIllegalStateException("node is not configured to store local location");
		}
		Set<ShardId> shardIds = Sets.newHashSet();
		for (File indicesLocation : nodeIndicesLocations) {
			File[] indicesList = indicesLocation.listFiles();
			if (indicesList == null) {
				continue;
			}
			for (File indexLocation : indicesList) {
				if (!indexLocation.isDirectory()) {
					continue;
				}
				String indexName = indexLocation.getName();
				File[] shardsList = indexLocation.listFiles();
				if (shardsList == null) {
					continue;
				}
				for (File shardLocation : shardsList) {
					if (!shardLocation.isDirectory()) {
						continue;
					}
					Integer shardId = Ints.tryParse(shardLocation.getName());
					if (shardId != null) {
						shardIds.add(new ShardId(indexName, shardId));
					}
				}
			}
		}
		return shardIds;
	}

	/**
	 * Close.
	 */
	public void close() {
		if (locks != null) {
			for (Lock lock : locks) {
				try {
					lock.release();
				} catch (IOException e) {

				}
			}
		}
	}
}
