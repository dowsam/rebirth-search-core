/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core KeepOnlyLastDeletionPolicy.java 2012-7-6 14:29:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.deletionpolicy;

import java.util.List;

import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class KeepOnlyLastDeletionPolicy.
 *
 * @author l.xue.nong
 */
public class KeepOnlyLastDeletionPolicy extends AbstractIndexShardComponent implements IndexDeletionPolicy {

	/**
	 * Instantiates a new keep only last deletion policy.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 */
	@Inject
	public KeepOnlyLastDeletionPolicy(ShardId shardId, @IndexSettings Settings indexSettings) {
		super(shardId, indexSettings);
		logger.debug("Using [keep_only_last] deletion policy");
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.index.IndexDeletionPolicy#onInit(java.util.List)
	 */
	public void onInit(List<? extends IndexCommit> commits) {

		onCommit(commits);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.index.IndexDeletionPolicy#onCommit(java.util.List)
	 */
	public void onCommit(List<? extends IndexCommit> commits) {

		int size = commits.size();
		for (int i = 0; i < size - 1; i++) {
			commits.get(i).delete();
		}
	}
}
