/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RamIndexStore.java 2012-3-29 15:02:01 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store.ram;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.store.DirectoryService;
import cn.com.rebirth.search.core.index.store.support.AbstractIndexStore;
import cn.com.rebirth.search.core.monitor.jvm.JvmInfo;
import cn.com.rebirth.search.core.monitor.jvm.JvmStats;


/**
 * The Class RamIndexStore.
 *
 * @author l.xue.nong
 */
public class RamIndexStore extends AbstractIndexStore {

	
	/**
	 * Instantiates a new ram index store.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indexService the index service
	 */
	@Inject
	public RamIndexStore(Index index, @IndexSettings Settings indexSettings, IndexService indexService) {
		super(index, indexSettings, indexService);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.IndexStore#persistent()
	 */
	@Override
	public boolean persistent() {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.IndexStore#shardDirectory()
	 */
	@Override
	public Class<? extends DirectoryService> shardDirectory() {
		return RamDirectoryService.class;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.IndexStore#backingStoreTotalSpace()
	 */
	@Override
	public ByteSizeValue backingStoreTotalSpace() {
		return JvmInfo.jvmInfo().getMem().heapMax();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.IndexStore#backingStoreFreeSpace()
	 */
	@Override
	public ByteSizeValue backingStoreFreeSpace() {
		return JvmStats.jvmStats().getMem().heapUsed();
	}
}
