/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NioFsDirectoryService.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store.fs;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import cn.com.rebirth.commons.io.FileSystemUtils;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.IndexStore;

/**
 * The Class NioFsDirectoryService.
 *
 * @author l.xue.nong
 */
public class NioFsDirectoryService extends FsDirectoryService {

	/**
	 * Instantiates a new nio fs directory service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexStore the index store
	 */
	@Inject
	public NioFsDirectoryService(ShardId shardId, @IndexSettings Settings indexSettings, IndexStore indexStore) {
		super(shardId, indexSettings, indexStore);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.DirectoryService#build()
	 */
	@Override
	public Directory[] build() throws IOException {
		File[] locations = indexStore.shardIndexLocations(shardId);
		Directory[] dirs = new Directory[locations.length];
		for (int i = 0; i < dirs.length; i++) {
			FileSystemUtils.mkdirs(locations[i]);
			dirs[i] = new NIOFSDirectory(locations[i], buildLockFactory());
		}
		return dirs;
	}
}
