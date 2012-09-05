/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LogDocMergePolicyProvider.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.merge.policy;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;

import cn.com.rebirth.commons.Preconditions;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.store.Store;

/**
 * The Class LogDocMergePolicyProvider.
 *
 * @author l.xue.nong
 */
public class LogDocMergePolicyProvider extends AbstractIndexShardComponent implements
		MergePolicyProvider<LogDocMergePolicy> {

	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	/** The compound format. */
	private volatile boolean compoundFormat;

	/** The min merge docs. */
	private volatile int minMergeDocs;

	/** The max merge docs. */
	private volatile int maxMergeDocs;

	/** The merge factor. */
	private volatile int mergeFactor;

	/** The calibrate size by deletes. */
	private final boolean calibrateSizeByDeletes;

	/** The async merge. */
	private boolean asyncMerge;

	/** The policies. */
	private final Set<CustomLogDocMergePolicy> policies = new CopyOnWriteArraySet<CustomLogDocMergePolicy>();

	/** The apply settings. */
	private final ApplySettings applySettings = new ApplySettings();

	/**
	 * Instantiates a new log doc merge policy provider.
	 *
	 * @param store the store
	 * @param indexSettingsService the index settings service
	 */
	@Inject
	public LogDocMergePolicyProvider(Store store, IndexSettingsService indexSettingsService) {
		super(store.shardId(), store.indexSettings());
		Preconditions.checkNotNull(store, "Store must be provided to merge policy");
		this.indexSettingsService = indexSettingsService;

		this.compoundFormat = indexSettings.getAsBoolean("index.compound_format", store.suggestUseCompoundFile());
		this.minMergeDocs = componentSettings.getAsInt("min_merge_docs", LogDocMergePolicy.DEFAULT_MIN_MERGE_DOCS);
		this.maxMergeDocs = componentSettings.getAsInt("max_merge_docs", LogDocMergePolicy.DEFAULT_MAX_MERGE_DOCS);
		this.mergeFactor = componentSettings.getAsInt("merge_factor", LogDocMergePolicy.DEFAULT_MERGE_FACTOR);
		this.calibrateSizeByDeletes = componentSettings.getAsBoolean("calibrate_size_by_deletes", true);
		this.asyncMerge = indexSettings.getAsBoolean("index.merge.async", true);
		logger.debug("using [log_doc] merge policy with merge_factor[" + mergeFactor + "], min_merge_docs["
				+ minMergeDocs + "], max_merge_docs[" + maxMergeDocs
				+ "], calibrate_size_by_deletes[{}], async_merge[{}]", calibrateSizeByDeletes, asyncMerge);

		indexSettingsService.addListener(applySettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.CloseableIndexComponent#close(boolean)
	 */
	@Override
	public void close(boolean delete) throws RebirthException {
		indexSettingsService.removeListener(applySettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.merge.policy.MergePolicyProvider#newMergePolicy()
	 */
	@Override
	public LogDocMergePolicy newMergePolicy() {
		CustomLogDocMergePolicy mergePolicy;
		if (asyncMerge) {
			mergePolicy = new EnableMergeLogDocMergePolicy(this);
		} else {
			mergePolicy = new CustomLogDocMergePolicy(this);
		}
		mergePolicy.setMinMergeDocs(minMergeDocs);
		mergePolicy.setMaxMergeDocs(maxMergeDocs);
		mergePolicy.setMergeFactor(mergeFactor);
		mergePolicy.setCalibrateSizeByDeletes(calibrateSizeByDeletes);
		mergePolicy.setUseCompoundFile(compoundFormat);
		policies.add(mergePolicy);
		return mergePolicy;
	}

	static {
		IndexMetaData.addDynamicSettings("index.merge.policy.min_merge_docs", "index.merge.policy.max_merge_docs",
				"index.merge.policy.merge_factor", "index.compound_format");
	}

	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements IndexSettingsService.Listener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.settings.IndexSettingsService.Listener#onRefreshSettings(cn.com.rebirth.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			int minMergeDocs = settings.getAsInt("index.merge.policy.min_merge_docs",
					LogDocMergePolicyProvider.this.minMergeDocs);
			if (minMergeDocs != LogDocMergePolicyProvider.this.minMergeDocs) {
				logger.info("updating min_merge_docs from [{}] to [{}]", LogDocMergePolicyProvider.this.minMergeDocs,
						minMergeDocs);
				LogDocMergePolicyProvider.this.minMergeDocs = minMergeDocs;
				for (CustomLogDocMergePolicy policy : policies) {
					policy.setMinMergeDocs(minMergeDocs);
				}
			}

			int maxMergeDocs = settings.getAsInt("index.merge.policy.max_merge_docs",
					LogDocMergePolicyProvider.this.maxMergeDocs);
			if (maxMergeDocs != LogDocMergePolicyProvider.this.maxMergeDocs) {
				logger.info("updating max_merge_docs from [{}] to [{}]", LogDocMergePolicyProvider.this.maxMergeDocs,
						maxMergeDocs);
				LogDocMergePolicyProvider.this.maxMergeDocs = maxMergeDocs;
				for (CustomLogDocMergePolicy policy : policies) {
					policy.setMaxMergeDocs(maxMergeDocs);
				}
			}

			int mergeFactor = settings.getAsInt("index.merge.policy.merge_factor",
					LogDocMergePolicyProvider.this.mergeFactor);
			if (mergeFactor != LogDocMergePolicyProvider.this.mergeFactor) {
				logger.info("updating merge_factor from [{}] to [{}]", LogDocMergePolicyProvider.this.mergeFactor,
						mergeFactor);
				LogDocMergePolicyProvider.this.mergeFactor = mergeFactor;
				for (CustomLogDocMergePolicy policy : policies) {
					policy.setMergeFactor(mergeFactor);
				}
			}

			boolean compoundFormat = settings.getAsBoolean("index.compound_format",
					LogDocMergePolicyProvider.this.compoundFormat);
			if (compoundFormat != LogDocMergePolicyProvider.this.compoundFormat) {
				logger.info("updating index.compound_format from [{}] to [{}]",
						LogDocMergePolicyProvider.this.compoundFormat, compoundFormat);
				LogDocMergePolicyProvider.this.compoundFormat = compoundFormat;
				for (CustomLogDocMergePolicy policy : policies) {
					policy.setUseCompoundFile(compoundFormat);
				}
			}
		}
	}

	/**
	 * The Class CustomLogDocMergePolicy.
	 *
	 * @author l.xue.nong
	 */
	public static class CustomLogDocMergePolicy extends LogDocMergePolicy {

		/** The provider. */
		private final LogDocMergePolicyProvider provider;

		/**
		 * Instantiates a new custom log doc merge policy.
		 *
		 * @param provider the provider
		 */
		public CustomLogDocMergePolicy(LogDocMergePolicyProvider provider) {
			super();
			this.provider = provider;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.index.LogMergePolicy#close()
		 */
		@Override
		public void close() {
			super.close();
			provider.policies.remove(this);
		}
	}

	/**
	 * The Class EnableMergeLogDocMergePolicy.
	 *
	 * @author l.xue.nong
	 */
	public static class EnableMergeLogDocMergePolicy extends CustomLogDocMergePolicy implements EnableMergePolicy {

		/** The enable merge. */
		private final ThreadLocal<Boolean> enableMerge = new ThreadLocal<Boolean>() {
			@Override
			protected Boolean initialValue() {
				return Boolean.FALSE;
			}
		};

		/**
		 * Instantiates a new enable merge log doc merge policy.
		 *
		 * @param provider the provider
		 */
		public EnableMergeLogDocMergePolicy(LogDocMergePolicyProvider provider) {
			super(provider);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.merge.policy.EnableMergePolicy#enableMerge()
		 */
		@Override
		public void enableMerge() {
			enableMerge.set(Boolean.TRUE);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.merge.policy.EnableMergePolicy#disableMerge()
		 */
		@Override
		public void disableMerge() {
			enableMerge.set(Boolean.FALSE);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.merge.policy.EnableMergePolicy#isMergeEnabled()
		 */
		@Override
		public boolean isMergeEnabled() {
			return enableMerge.get() == Boolean.TRUE;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.merge.policy.LogDocMergePolicyProvider.CustomLogDocMergePolicy#close()
		 */
		@Override
		public void close() {
			enableMerge.remove();
			super.close();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.index.LogMergePolicy#findMerges(org.apache.lucene.index.SegmentInfos)
		 */
		@Override
		public MergeSpecification findMerges(SegmentInfos infos) throws IOException {
			if (enableMerge.get() == Boolean.FALSE) {
				return null;
			}
			return super.findMerges(infos);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.index.LogMergePolicy#findForcedMerges(org.apache.lucene.index.SegmentInfos, int, java.util.Map)
		 */
		@Override
		public MergeSpecification findForcedMerges(SegmentInfos infos, int maxSegmentCount,
				Map<SegmentInfo, Boolean> segmentsToMerge) throws IOException {
			if (enableMerge.get() == Boolean.FALSE) {
				return null;
			}
			return super.findForcedMerges(infos, maxSegmentCount, segmentsToMerge);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.index.LogMergePolicy#findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos)
		 */
		@Override
		public MergeSpecification findForcedDeletesMerges(SegmentInfos infos) throws CorruptIndexException, IOException {
			if (enableMerge.get() == Boolean.FALSE) {
				return null;
			}
			return super.findForcedDeletesMerges(infos);
		}
	}
}
