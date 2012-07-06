/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FsIndexShardGateway.java 2012-3-29 15:01:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway.fs;

import java.io.IOException;

import org.apache.lucene.store.Lock;
import org.apache.lucene.store.NativeFSLockFactory;

import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.blobstore.fs.AbstractFsBlobContainer;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;
import cn.com.rebirth.search.core.index.gateway.blobstore.BlobStoreIndexShardGateway;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.store.Store;
import cn.com.rebirth.search.core.threadpool.ThreadPool;


/**
 * The Class FsIndexShardGateway.
 *
 * @author l.xue.nong
 */
public class FsIndexShardGateway extends BlobStoreIndexShardGateway {

	
	/** The snapshot lock. */
	private final boolean snapshotLock;

	
	/**
	 * Instantiates a new fs index shard gateway.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param threadPool the thread pool
	 * @param fsIndexGateway the fs index gateway
	 * @param indexShard the index shard
	 * @param store the store
	 */
	@Inject
	public FsIndexShardGateway(ShardId shardId, @IndexSettings Settings indexSettings, ThreadPool threadPool,
			IndexGateway fsIndexGateway, IndexShard indexShard, Store store) {
		super(shardId, indexSettings, threadPool, fsIndexGateway, indexShard, store);
		this.snapshotLock = indexSettings.getAsBoolean("gateway.fs.snapshot_lock", true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway#type()
	 */
	@Override
	public String type() {
		return "fs";
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.blobstore.BlobStoreIndexShardGateway#obtainSnapshotLock()
	 */
	@Override
	public SnapshotLock obtainSnapshotLock() throws Exception {
		if (!snapshotLock) {
			return NO_SNAPSHOT_LOCK;
		}
		AbstractFsBlobContainer fsBlobContainer = (AbstractFsBlobContainer) blobContainer;
		NativeFSLockFactory lockFactory = new NativeFSLockFactory(fsBlobContainer.filePath());

		Lock lock = lockFactory.makeLock("snapshot.lock");
		boolean obtained = lock.obtain();
		if (!obtained) {
			throw new RestartIllegalStateException("failed to obtain snapshot lock [" + lock + "]");
		}
		return new FsSnapshotLock(lock);
	}

	
	/**
	 * The Class FsSnapshotLock.
	 *
	 * @author l.xue.nong
	 */
	public class FsSnapshotLock implements SnapshotLock {

		
		/** The lock. */
		private final Lock lock;

		
		/**
		 * Instantiates a new fs snapshot lock.
		 *
		 * @param lock the lock
		 */
		public FsSnapshotLock(Lock lock) {
			this.lock = lock;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway.SnapshotLock#release()
		 */
		@Override
		public void release() {
			try {
				lock.release();
			} catch (IOException e) {
				logger.warn("failed to release snapshot lock [{}]", e, lock);
			}
		}
	}
}