/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MmapFsIndexStore.java 2012-3-29 15:02:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store.fs;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.store.DirectoryService;


/**
 * The Class MmapFsIndexStore.
 *
 * @author l.xue.nong
 */
public class MmapFsIndexStore extends FsIndexStore {

    
    /**
     * Instantiates a new mmap fs index store.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param indexService the index service
     * @param nodeEnv the node env
     */
    @Inject
    public MmapFsIndexStore(Index index, @IndexSettings Settings indexSettings, IndexService indexService, NodeEnvironment nodeEnv) {
        super(index, indexSettings, indexService, nodeEnv);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.store.IndexStore#shardDirectory()
     */
    @Override
    public Class<? extends DirectoryService> shardDirectory() {
        return MmapFsDirectoryService.class;
    }
}