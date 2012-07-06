/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ConcurrentMergeSchedulerProvider.java 2012-3-29 15:02:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.merge.scheduler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.index.TrackingConcurrentMergeScheduler;
import org.apache.lucene.store.AlreadyClosedException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.merge.policy.EnableMergePolicy;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class ConcurrentMergeSchedulerProvider.
 *
 * @author l.xue.nong
 */
public class ConcurrentMergeSchedulerProvider extends AbstractIndexShardComponent implements MergeSchedulerProvider {

	
	/** The max thread count. */
	private final int maxThreadCount;

	
	/** The max merge count. */
	private final int maxMergeCount;

	
	/** The schedulers. */
	private Set<CustomConcurrentMergeScheduler> schedulers = new CopyOnWriteArraySet<CustomConcurrentMergeScheduler>();

	
	/**
	 * Instantiates a new concurrent merge scheduler provider.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 */
	@Inject
	public ConcurrentMergeSchedulerProvider(ShardId shardId, @IndexSettings Settings indexSettings) {
		super(shardId, indexSettings);

		
		this.maxThreadCount = componentSettings.getAsInt("max_thread_count",
				Math.max(1, Math.min(3, Runtime.getRuntime().availableProcessors() / 2)));
		this.maxMergeCount = componentSettings.getAsInt("max_merge_count", maxThreadCount + 2);
		logger.debug("using [concurrent] merge scheduler with max_thread_count[{}]", maxThreadCount);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.merge.scheduler.MergeSchedulerProvider#newMergeScheduler()
	 */
	@Override
	public MergeScheduler newMergeScheduler() {
		CustomConcurrentMergeScheduler concurrentMergeScheduler = new CustomConcurrentMergeScheduler(shardId, this);
		concurrentMergeScheduler.setMaxMergeCount(maxMergeCount);
		concurrentMergeScheduler.setMaxThreadCount(maxThreadCount);
		schedulers.add(concurrentMergeScheduler);
		return concurrentMergeScheduler;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.merge.scheduler.MergeSchedulerProvider#stats()
	 */
	@Override
	public MergeStats stats() {
		MergeStats mergeStats = new MergeStats();
		for (CustomConcurrentMergeScheduler scheduler : schedulers) {
			mergeStats.add(scheduler.totalMerges(), scheduler.totalMergeTime(), scheduler.totalMergeNumDocs(),
					scheduler.totalMergeSizeInBytes(), scheduler.currentMerges(), scheduler.currentMergesNumDocs(),
					scheduler.currentMergesSizeInBytes());
		}
		return mergeStats;
	}

	
	/**
	 * The Class CustomConcurrentMergeScheduler.
	 *
	 * @author l.xue.nong
	 */
	public static class CustomConcurrentMergeScheduler extends TrackingConcurrentMergeScheduler {

		
		/** The shard id. */
		private final ShardId shardId;

		
		/** The provider. */
		private final ConcurrentMergeSchedulerProvider provider;

		
		/**
		 * Instantiates a new custom concurrent merge scheduler.
		 *
		 * @param shardId the shard id
		 * @param provider the provider
		 */
		private CustomConcurrentMergeScheduler(ShardId shardId, ConcurrentMergeSchedulerProvider provider) {
			this.shardId = shardId;
			this.provider = provider;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.index.ConcurrentMergeScheduler#merge(org.apache.lucene.index.IndexWriter)
		 */
		@Override
		public void merge(IndexWriter writer) throws CorruptIndexException, IOException {
			try {
				
				if (writer.getConfig().getMergePolicy() instanceof EnableMergePolicy) {
					if (!((EnableMergePolicy) writer.getConfig().getMergePolicy()).isMergeEnabled()) {
						return;
					}
				}
			} catch (AlreadyClosedException e) {
				
				
				
				return;
			}
			try {
				super.merge(writer);
			} catch (IOException e) {
				logger.warn("failed to merge", e);
				throw e;
			}
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.index.ConcurrentMergeScheduler#getMergeThread(org.apache.lucene.index.IndexWriter, org.apache.lucene.index.MergePolicy.OneMerge)
		 */
		@Override
		protected MergeThread getMergeThread(IndexWriter writer, MergePolicy.OneMerge merge) throws IOException {
			MergeThread thread = super.getMergeThread(writer, merge);
			thread.setName("[" + shardId.index().name() + "][" + shardId.id() + "]: " + thread.getName());
			return thread;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.index.ConcurrentMergeScheduler#handleMergeException(java.lang.Throwable)
		 */
		@Override
		protected void handleMergeException(Throwable exc) {
			logger.warn("failed to merge", exc);
			super.handleMergeException(exc);
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.index.ConcurrentMergeScheduler#close()
		 */
		@Override
		public void close() {
			super.close();
			provider.schedulers.remove(this);
		}
	}
}
