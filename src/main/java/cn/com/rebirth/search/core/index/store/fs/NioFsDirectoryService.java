/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NioFsDirectoryService.java 2012-3-29 15:02:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store.fs;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.FileSystemUtils;
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
	 * @see cn.com.summall.search.core.index.store.DirectoryService#build()
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
