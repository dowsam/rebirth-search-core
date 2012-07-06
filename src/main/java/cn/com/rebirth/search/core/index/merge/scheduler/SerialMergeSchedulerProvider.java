/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SerialMergeSchedulerProvider.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.merge.scheduler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.index.TrackingSerialMergeScheduler;
import org.apache.lucene.store.AlreadyClosedException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.merge.policy.EnableMergePolicy;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class SerialMergeSchedulerProvider.
 *
 * @author l.xue.nong
 */
public class SerialMergeSchedulerProvider extends AbstractIndexShardComponent implements MergeSchedulerProvider {

	/** The schedulers. */
	private Set<CustomSerialMergeScheduler> schedulers = new CopyOnWriteArraySet<CustomSerialMergeScheduler>();

	/**
	 * Instantiates a new serial merge scheduler provider.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 */
	@Inject
	public SerialMergeSchedulerProvider(ShardId shardId, @IndexSettings Settings indexSettings) {
		super(shardId, indexSettings);
		logger.trace("using [serial] merge scheduler");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.merge.scheduler.MergeSchedulerProvider#newMergeScheduler()
	 */
	@Override
	public MergeScheduler newMergeScheduler() {
		CustomSerialMergeScheduler scheduler = new CustomSerialMergeScheduler(this);
		schedulers.add(scheduler);
		return scheduler;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.merge.scheduler.MergeSchedulerProvider#stats()
	 */
	@Override
	public MergeStats stats() {
		MergeStats mergeStats = new MergeStats();
		for (CustomSerialMergeScheduler scheduler : schedulers) {
			mergeStats.add(scheduler.totalMerges(), scheduler.totalMergeTime(), scheduler.totalMergeNumDocs(),
					scheduler.totalMergeSizeInBytes(), scheduler.currentMerges(), scheduler.currentMergesNumDocs(),
					scheduler.currentMergesSizeInBytes());
		}
		return mergeStats;
	}

	/**
	 * The Class CustomSerialMergeScheduler.
	 *
	 * @author l.xue.nong
	 */
	public static class CustomSerialMergeScheduler extends TrackingSerialMergeScheduler {

		/** The provider. */
		private final SerialMergeSchedulerProvider provider;

		/**
		 * Instantiates a new custom serial merge scheduler.
		 *
		 * @param provider the provider
		 */
		public CustomSerialMergeScheduler(SerialMergeSchedulerProvider provider) {
			super();
			this.provider = provider;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.index.TrackingSerialMergeScheduler#merge(org.apache.lucene.index.IndexWriter)
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
		 * @see org.apache.lucene.index.TrackingSerialMergeScheduler#close()
		 */
		@Override
		public void close() {
			super.close();
			provider.schedulers.remove(this);
		}
	}
}
