/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NioFsIndexStore.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store.fs;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.store.DirectoryService;

/**
 * The Class NioFsIndexStore.
 *
 * @author l.xue.nong
 */
public class NioFsIndexStore extends FsIndexStore {

	/**
	 * Instantiates a new nio fs index store.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indexService the index service
	 * @param nodeEnv the node env
	 */
	@Inject
	public NioFsIndexStore(Index index, @IndexSettings Settings indexSettings, IndexService indexService,
			NodeEnvironment nodeEnv) {
		super(index, indexSettings, indexService, nodeEnv);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.IndexStore#shardDirectory()
	 */
	@Override
	public Class<? extends DirectoryService> shardDirectory() {
		return NioFsDirectoryService.class;
	}
}