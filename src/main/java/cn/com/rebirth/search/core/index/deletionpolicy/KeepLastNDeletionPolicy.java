/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core KeepLastNDeletionPolicy.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.deletionpolicy;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class KeepLastNDeletionPolicy.
 *
 * @author l.xue.nong
 */
public class KeepLastNDeletionPolicy extends AbstractIndexShardComponent implements IndexDeletionPolicy {

	/** The num to keep. */
	private final int numToKeep;

	/**
	 * Instantiates a new keep last n deletion policy.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 */
	@Inject
	public KeepLastNDeletionPolicy(ShardId shardId, @IndexSettings Settings indexSettings) {
		super(shardId, indexSettings);
		this.numToKeep = componentSettings.getAsInt("num_to_keep", 5);
		logger.debug("Using [keep_last_n] deletion policy with num_to_keep[{}]", numToKeep);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.index.IndexDeletionPolicy#onInit(java.util.List)
	 */
	public void onInit(List<? extends IndexCommit> commits) throws IOException {

		doDeletes(commits);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.index.IndexDeletionPolicy#onCommit(java.util.List)
	 */
	public void onCommit(List<? extends IndexCommit> commits) throws IOException {
		doDeletes(commits);
	}

	/**
	 * Do deletes.
	 *
	 * @param commits the commits
	 */
	private void doDeletes(List<? extends IndexCommit> commits) {
		int size = commits.size();
		for (int i = 0; i < size - numToKeep; i++) {
			commits.get(i).delete();
		}
	}

}
