/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractIndexStore.java 2012-3-29 15:01:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store.support;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.IndexStore;


/**
 * The Class AbstractIndexStore.
 *
 * @author l.xue.nong
 */
public abstract class AbstractIndexStore extends AbstractIndexComponent implements IndexStore {

	
	/** The index service. */
	protected final IndexService indexService;

	
	/**
	 * Instantiates a new abstract index store.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indexService the index service
	 */
	protected AbstractIndexStore(Index index, @IndexSettings Settings indexSettings, IndexService indexService) {
		super(index, indexSettings);
		this.indexService = indexService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.IndexStore#canDeleteUnallocated(cn.com.summall.search.core.index.shard.ShardId)
	 */
	@Override
	public boolean canDeleteUnallocated(ShardId shardId) {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.IndexStore#deleteUnallocated(cn.com.summall.search.core.index.shard.ShardId)
	 */
	@Override
	public void deleteUnallocated(ShardId shardId) throws IOException {
		
	}
}
