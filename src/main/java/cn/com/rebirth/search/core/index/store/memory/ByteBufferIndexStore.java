/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ByteBufferIndexStore.java 2012-7-6 14:29:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store.memory;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeUnit;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cache.memory.ByteBufferCache;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.store.DirectoryService;
import cn.com.rebirth.search.core.index.store.support.AbstractIndexStore;
import cn.com.rebirth.search.core.monitor.jvm.JvmInfo;
import cn.com.rebirth.search.core.monitor.jvm.JvmStats;

/**
 * The Class ByteBufferIndexStore.
 *
 * @author l.xue.nong
 */
public class ByteBufferIndexStore extends AbstractIndexStore {

	/** The direct. */
	private final boolean direct;

	/**
	 * Instantiates a new byte buffer index store.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indexService the index service
	 * @param byteBufferCache the byte buffer cache
	 */
	@Inject
	public ByteBufferIndexStore(Index index, @IndexSettings Settings indexSettings, IndexService indexService,
			ByteBufferCache byteBufferCache) {
		super(index, indexSettings, indexService);
		this.direct = byteBufferCache.direct();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.IndexStore#persistent()
	 */
	@Override
	public boolean persistent() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.IndexStore#shardDirectory()
	 */
	@Override
	public Class<? extends DirectoryService> shardDirectory() {
		return ByteBufferDirectoryService.class;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.IndexStore#backingStoreTotalSpace()
	 */
	@Override
	public ByteSizeValue backingStoreTotalSpace() {
		if (direct) {

			return new ByteSizeValue(-1, ByteSizeUnit.BYTES);
		}
		return JvmInfo.jvmInfo().mem().heapMax();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.store.IndexStore#backingStoreFreeSpace()
	 */
	@Override
	public ByteSizeValue backingStoreFreeSpace() {
		if (direct) {
			return new ByteSizeValue(-1, ByteSizeUnit.BYTES);
		}
		return JvmStats.jvmStats().mem().heapUsed();
	}
}