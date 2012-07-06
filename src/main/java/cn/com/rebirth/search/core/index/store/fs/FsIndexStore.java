/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FsIndexStore.java 2012-7-6 14:30:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store.fs;

import java.io.File;
import java.io.IOException;

import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.io.FileSystemUtils;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.support.AbstractIndexStore;

/**
 * The Class FsIndexStore.
 *
 * @author l.xue.nong
 */
public abstract class FsIndexStore extends AbstractIndexStore {

	/** The node env. */
	private final NodeEnvironment nodeEnv;

	/** The locations. */
	private final File[] locations;

	/**
	 * Instantiates a new fs index store.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indexService the index service
	 * @param nodeEnv the node env
	 */
	public FsIndexStore(Index index, @IndexSettings Settings indexSettings, IndexService indexService,
			NodeEnvironment nodeEnv) {
		super(index, indexSettings, indexService);
		this.nodeEnv = nodeEnv;
		if (nodeEnv.hasNodeFile()) {
			this.locations = nodeEnv.indexLocations(index);
		} else {
			this.locations = null;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.IndexStore#persistent()
	 */
	@Override
	public boolean persistent() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.IndexStore#backingStoreTotalSpace()
	 */
	@Override
	public ByteSizeValue backingStoreTotalSpace() {
		if (locations == null) {
			return new ByteSizeValue(0);
		}
		long totalSpace = 0;
		for (File location : locations) {
			totalSpace += location.getTotalSpace();
		}
		return new ByteSizeValue(totalSpace);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.IndexStore#backingStoreFreeSpace()
	 */
	@Override
	public ByteSizeValue backingStoreFreeSpace() {
		if (locations == null) {
			return new ByteSizeValue(0);
		}
		long usableSpace = 0;
		for (File location : locations) {
			usableSpace += location.getUsableSpace();
		}
		return new ByteSizeValue(usableSpace);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.support.AbstractIndexStore#canDeleteUnallocated(cn.com.rebirth.search.core.index.shard.ShardId)
	 */
	@Override
	public boolean canDeleteUnallocated(ShardId shardId) {
		if (locations == null) {
			return false;
		}
		if (indexService.hasShard(shardId.id())) {
			return false;
		}
		for (File location : shardLocations(shardId)) {
			if (location.exists()) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.support.AbstractIndexStore#deleteUnallocated(cn.com.rebirth.search.core.index.shard.ShardId)
	 */
	@Override
	public void deleteUnallocated(ShardId shardId) throws IOException {
		if (locations == null) {
			return;
		}
		if (indexService.hasShard(shardId.id())) {
			throw new RebirthIllegalStateException(shardId + " allocated, can't be deleted");
		}
		FileSystemUtils.deleteRecursively(shardLocations(shardId));
	}

	/**
	 * Shard locations.
	 *
	 * @param shardId the shard id
	 * @return the file[]
	 */
	public File[] shardLocations(ShardId shardId) {
		return nodeEnv.shardLocations(shardId);
	}

	/**
	 * Shard index locations.
	 *
	 * @param shardId the shard id
	 * @return the file[]
	 */
	public File[] shardIndexLocations(ShardId shardId) {
		File[] shardLocations = shardLocations(shardId);
		File[] shardIndexLocations = new File[shardLocations.length];
		for (int i = 0; i < shardLocations.length; i++) {
			shardIndexLocations[i] = new File(shardLocations[i], "index");
		}
		return shardIndexLocations;
	}

	/**
	 * Shard translog locations.
	 *
	 * @param shardId the shard id
	 * @return the file[]
	 */
	public File[] shardTranslogLocations(ShardId shardId) {
		File[] shardLocations = shardLocations(shardId);
		File[] shardTranslogLocations = new File[shardLocations.length];
		for (int i = 0; i < shardLocations.length; i++) {
			shardTranslogLocations[i] = new File(shardLocations[i], "translog");
		}
		return shardTranslogLocations;
	}
}
