/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LogByteSizeMergePolicyProvider.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.merge.policy;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;

import cn.com.rebirth.commons.Preconditions;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeUnit;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.store.Store;

/**
 * The Class LogByteSizeMergePolicyProvider.
 *
 * @author l.xue.nong
 */
public class LogByteSizeMergePolicyProvider extends AbstractIndexShardComponent implements
		MergePolicyProvider<LogByteSizeMergePolicy> {

	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	/** The compound format. */
	private volatile boolean compoundFormat;

	/** The min merge size. */
	private volatile ByteSizeValue minMergeSize;

	/** The max merge size. */
	private volatile ByteSizeValue maxMergeSize;

	/** The merge factor. */
	private volatile int mergeFactor;

	/** The max merge docs. */
	private volatile int maxMergeDocs;

	/** The calibrate size by deletes. */
	private final boolean calibrateSizeByDeletes;

	/** The async merge. */
	private boolean asyncMerge;

	/** The policies. */
	private final Set<CustomLogByteSizeMergePolicy> policies = new CopyOnWriteArraySet<CustomLogByteSizeMergePolicy>();

	/** The apply settings. */
	private final ApplySettings applySettings = new ApplySettings();

	/**
	 * Instantiates a new log byte size merge policy provider.
	 *
	 * @param store the store
	 * @param indexSettingsService the index settings service
	 */
	@Inject
	public LogByteSizeMergePolicyProvider(Store store, IndexSettingsService indexSettingsService) {
		super(store.shardId(), store.indexSettings());
		Preconditions.checkNotNull(store, "Store must be provided to merge policy");
		this.indexSettingsService = indexSettingsService;

		this.compoundFormat = indexSettings.getAsBoolean("index.compound_format", store.suggestUseCompoundFile());
		this.minMergeSize = componentSettings.getAsBytesSize("min_merge_size", new ByteSizeValue(
				(long) (LogByteSizeMergePolicy.DEFAULT_MIN_MERGE_MB * 1024 * 1024), ByteSizeUnit.BYTES));
		this.maxMergeSize = componentSettings.getAsBytesSize("max_merge_size", new ByteSizeValue(
				(long) LogByteSizeMergePolicy.DEFAULT_MAX_MERGE_MB, ByteSizeUnit.MB));
		this.mergeFactor = componentSettings.getAsInt("merge_factor", LogByteSizeMergePolicy.DEFAULT_MERGE_FACTOR);
		this.maxMergeDocs = componentSettings.getAsInt("max_merge_docs", LogByteSizeMergePolicy.DEFAULT_MAX_MERGE_DOCS);
		this.calibrateSizeByDeletes = componentSettings.getAsBoolean("calibrate_size_by_deletes", true);
		this.asyncMerge = indexSettings.getAsBoolean("index.merge.async", true);
		logger.debug("using [log_bytes_size] merge policy with merge_factor[" + mergeFactor + "], min_merge_size["
				+ minMergeSize + "], max_merge_size[" + maxMergeSize + "], max_merge_docs[" + maxMergeDocs
				+ "], calibrate_size_by_deletes[{}], async_merge[{}]", calibrateSizeByDeletes, asyncMerge);

		indexSettingsService.addListener(applySettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.merge.policy.MergePolicyProvider#newMergePolicy()
	 */
	@Override
	public LogByteSizeMergePolicy newMergePolicy() {
		CustomLogByteSizeMergePolicy mergePolicy;
		if (asyncMerge) {
			mergePolicy = new EnableMergeLogByteSizeMergePolicy(this);
		} else {
			mergePolicy = new CustomLogByteSizeMergePolicy(this);
		}
		mergePolicy.setMinMergeMB(minMergeSize.mbFrac());
		mergePolicy.setMaxMergeMB(maxMergeSize.mbFrac());
		mergePolicy.setMergeFactor(mergeFactor);
		mergePolicy.setMaxMergeDocs(maxMergeDocs);
		mergePolicy.setCalibrateSizeByDeletes(calibrateSizeByDeletes);
		mergePolicy.setUseCompoundFile(compoundFormat);

		policies.add(mergePolicy);
		return mergePolicy;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.CloseableIndexComponent#close(boolean)
	 */
	@Override
	public void close(boolean delete) throws RebirthException {
		indexSettingsService.removeListener(applySettings);
	}

	static {
		IndexMetaData.addDynamicSettings("index.merge.policy.min_merge_size", "index.merge.policy.max_merge_size",
				"index.merge.policy.max_merge_docs", "index.merge.policy.merge_factor", "index.compound_format");
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
			ByteSizeValue minMergeSize = settings.getAsBytesSize("index.merge.policy.min_merge_size",
					LogByteSizeMergePolicyProvider.this.minMergeSize);
			if (!minMergeSize.equals(LogByteSizeMergePolicyProvider.this.minMergeSize)) {
				logger.info("updating min_merge_size from [{}] to [{}]",
						LogByteSizeMergePolicyProvider.this.minMergeSize, minMergeSize);
				LogByteSizeMergePolicyProvider.this.minMergeSize = minMergeSize;
				for (CustomLogByteSizeMergePolicy policy : policies) {
					policy.setMinMergeMB(minMergeSize.mbFrac());
				}
			}

			ByteSizeValue maxMergeSize = settings.getAsBytesSize("index.merge.policy.max_merge_size",
					LogByteSizeMergePolicyProvider.this.maxMergeSize);
			if (!maxMergeSize.equals(LogByteSizeMergePolicyProvider.this.maxMergeSize)) {
				logger.info("updating max_merge_size from [{}] to [{}]",
						LogByteSizeMergePolicyProvider.this.maxMergeSize, maxMergeSize);
				LogByteSizeMergePolicyProvider.this.maxMergeSize = maxMergeSize;
				for (CustomLogByteSizeMergePolicy policy : policies) {
					policy.setMaxMergeMB(maxMergeSize.mbFrac());
				}
			}

			int maxMergeDocs = settings.getAsInt("index.merge.policy.max_merge_docs",
					LogByteSizeMergePolicyProvider.this.maxMergeDocs);
			if (maxMergeDocs != LogByteSizeMergePolicyProvider.this.maxMergeDocs) {
				logger.info("updating max_merge_docs from [{}] to [{}]",
						LogByteSizeMergePolicyProvider.this.maxMergeDocs, maxMergeDocs);
				LogByteSizeMergePolicyProvider.this.maxMergeDocs = maxMergeDocs;
				for (CustomLogByteSizeMergePolicy policy : policies) {
					policy.setMaxMergeDocs(maxMergeDocs);
				}
			}

			int mergeFactor = settings.getAsInt("index.merge.policy.merge_factor",
					LogByteSizeMergePolicyProvider.this.mergeFactor);
			if (mergeFactor != LogByteSizeMergePolicyProvider.this.mergeFactor) {
				logger.info("updating merge_factor from [{}] to [{}]", LogByteSizeMergePolicyProvider.this.mergeFactor,
						mergeFactor);
				LogByteSizeMergePolicyProvider.this.mergeFactor = mergeFactor;
				for (CustomLogByteSizeMergePolicy policy : policies) {
					policy.setMergeFactor(mergeFactor);
				}
			}

			boolean compoundFormat = settings.getAsBoolean("index.compound_format",
					LogByteSizeMergePolicyProvider.this.compoundFormat);
			if (compoundFormat != LogByteSizeMergePolicyProvider.this.compoundFormat) {
				logger.info("updating index.compound_format from [{}] to [{}]",
						LogByteSizeMergePolicyProvider.this.compoundFormat, compoundFormat);
				LogByteSizeMergePolicyProvider.this.compoundFormat = compoundFormat;
				for (CustomLogByteSizeMergePolicy policy : policies) {
					policy.setUseCompoundFile(compoundFormat);
				}
			}
		}
	}

	/**
	 * The Class CustomLogByteSizeMergePolicy.
	 *
	 * @author l.xue.nong
	 */
	public static class CustomLogByteSizeMergePolicy extends LogByteSizeMergePolicy {

		/** The provider. */
		private final LogByteSizeMergePolicyProvider provider;

		/**
		 * Instantiates a new custom log byte size merge policy.
		 *
		 * @param provider the provider
		 */
		public CustomLogByteSizeMergePolicy(LogByteSizeMergePolicyProvider provider) {
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
	 * The Class EnableMergeLogByteSizeMergePolicy.
	 *
	 * @author l.xue.nong
	 */
	public static class EnableMergeLogByteSizeMergePolicy extends CustomLogByteSizeMergePolicy implements
			EnableMergePolicy {

		/** The enable merge. */
		private final ThreadLocal<Boolean> enableMerge = new ThreadLocal<Boolean>() {
			@Override
			protected Boolean initialValue() {
				return Boolean.FALSE;
			}
		};

		/**
		 * Instantiates a new enable merge log byte size merge policy.
		 *
		 * @param provider the provider
		 */
		public EnableMergeLogByteSizeMergePolicy(LogByteSizeMergePolicyProvider provider) {
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
		 * @see cn.com.rebirth.search.core.index.merge.policy.LogByteSizeMergePolicyProvider.CustomLogByteSizeMergePolicy#close()
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
