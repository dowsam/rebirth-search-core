/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TieredMergePolicyProvider.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.merge.policy;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.TieredMergePolicy;

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
 * The Class TieredMergePolicyProvider.
 *
 * @author l.xue.nong
 */
public class TieredMergePolicyProvider extends AbstractIndexShardComponent implements
		MergePolicyProvider<TieredMergePolicy> {

	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	/** The policies. */
	private final Set<CustomTieredMergePolicyProvider> policies = new CopyOnWriteArraySet<CustomTieredMergePolicyProvider>();

	/** The compound format. */
	private volatile boolean compoundFormat;

	/** The force merge deletes pct allowed. */
	private volatile double forceMergeDeletesPctAllowed;

	/** The floor segment. */
	private volatile ByteSizeValue floorSegment;

	/** The max merge at once. */
	private volatile int maxMergeAtOnce;

	/** The max merge at once explicit. */
	private volatile int maxMergeAtOnceExplicit;

	/** The max merged segment. */
	private volatile ByteSizeValue maxMergedSegment;

	/** The segments per tier. */
	private volatile double segmentsPerTier;

	/** The reclaim deletes weight. */
	private volatile double reclaimDeletesWeight;

	/** The async merge. */
	private boolean asyncMerge;

	/** The apply settings. */
	private final ApplySettings applySettings = new ApplySettings();

	/**
	 * Instantiates a new tiered merge policy provider.
	 *
	 * @param store the store
	 * @param indexSettingsService the index settings service
	 */
	@Inject
	public TieredMergePolicyProvider(Store store, IndexSettingsService indexSettingsService) {
		super(store.shardId(), store.indexSettings());
		this.indexSettingsService = indexSettingsService;

		this.compoundFormat = indexSettings.getAsBoolean("index.compound_format", store.suggestUseCompoundFile());
		this.asyncMerge = indexSettings.getAsBoolean("index.merge.async", true);
		this.forceMergeDeletesPctAllowed = componentSettings.getAsDouble("expunge_deletes_allowed", 10d);
		this.floorSegment = componentSettings.getAsBytesSize("floor_segment", new ByteSizeValue(2, ByteSizeUnit.MB));
		this.maxMergeAtOnce = componentSettings.getAsInt("max_merge_at_once", 10);
		this.maxMergeAtOnceExplicit = componentSettings.getAsInt("max_merge_at_once_explicit", 30);

		this.maxMergedSegment = componentSettings.getAsBytesSize("max_merged_segment",
				componentSettings.getAsBytesSize("max_merge_segment", new ByteSizeValue(5, ByteSizeUnit.GB)));
		this.segmentsPerTier = componentSettings.getAsDouble("segments_per_tier", 10d);
		this.reclaimDeletesWeight = componentSettings.getAsDouble("reclaim_deletes_weight", 2.0d);

		logger.debug("using [tiered] merge policy with expunge_deletes_allowed[" + forceMergeDeletesPctAllowed
				+ "], floor_segment[" + floorSegment + "], max_merge_at_once[" + maxMergeAtOnce
				+ "], max_merge_at_once_explicit[" + maxMergeAtOnceExplicit + "], max_merged_segment["
				+ maxMergedSegment + "], segments_per_tier[" + segmentsPerTier
				+ "], reclaim_deletes_weight[{}], async_merge[{}]", reclaimDeletesWeight, asyncMerge);

		indexSettingsService.addListener(applySettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.merge.policy.MergePolicyProvider#newMergePolicy()
	 */
	@Override
	public TieredMergePolicy newMergePolicy() {
		CustomTieredMergePolicyProvider mergePolicy;
		if (asyncMerge) {
			mergePolicy = new EnableMergeTieredMergePolicyProvider(this);
		} else {
			mergePolicy = new CustomTieredMergePolicyProvider(this);
		}
		mergePolicy.setUseCompoundFile(compoundFormat);
		mergePolicy.setForceMergeDeletesPctAllowed(forceMergeDeletesPctAllowed);
		mergePolicy.setFloorSegmentMB(floorSegment.mbFrac());
		mergePolicy.setMaxMergeAtOnce(maxMergeAtOnce);
		mergePolicy.setMaxMergeAtOnceExplicit(maxMergeAtOnceExplicit);
		mergePolicy.setMaxMergedSegmentMB(maxMergedSegment.mbFrac());
		mergePolicy.setSegmentsPerTier(segmentsPerTier);
		mergePolicy.setReclaimDeletesWeight(reclaimDeletesWeight);
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
		IndexMetaData.addDynamicSettings("index.merge.policy.expunge_deletes_allowed",
				"index.merge.policy.floor_segment", "index.merge.policy.max_merge_at_once",
				"index.merge.policy.max_merge_at_once_explicit", "index.merge.policy.max_merged_segment",
				"index.merge.policy.segments_per_tier", "index.merge.policy.reclaim_deletes_weight",
				"index.compound_format");
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
			double expungeDeletesPctAllowed = settings.getAsDouble("index.merge.policy.expunge_deletes_allowed",
					TieredMergePolicyProvider.this.forceMergeDeletesPctAllowed);
			if (expungeDeletesPctAllowed != TieredMergePolicyProvider.this.forceMergeDeletesPctAllowed) {
				logger.info("updating [expunge_deletes_allowed] from [{}] to [{}]",
						TieredMergePolicyProvider.this.forceMergeDeletesPctAllowed, expungeDeletesPctAllowed);
				TieredMergePolicyProvider.this.forceMergeDeletesPctAllowed = expungeDeletesPctAllowed;
				for (CustomTieredMergePolicyProvider policy : policies) {
					policy.setForceMergeDeletesPctAllowed(expungeDeletesPctAllowed);
				}
			}

			ByteSizeValue floorSegment = settings.getAsBytesSize("index.merge.policy.floor_segment",
					TieredMergePolicyProvider.this.floorSegment);
			if (!floorSegment.equals(TieredMergePolicyProvider.this.floorSegment)) {
				logger.info("updating [floor_segment] from [{}] to [{}]", TieredMergePolicyProvider.this.floorSegment,
						floorSegment);
				TieredMergePolicyProvider.this.floorSegment = floorSegment;
				for (CustomTieredMergePolicyProvider policy : policies) {
					policy.setFloorSegmentMB(floorSegment.mbFrac());
				}
			}

			int maxMergeAtOnce = settings.getAsInt("index.merge.policy.max_merge_at_once",
					TieredMergePolicyProvider.this.maxMergeAtOnce);
			if (maxMergeAtOnce != TieredMergePolicyProvider.this.maxMergeAtOnce) {
				logger.info("updating [max_merge_at_once] from [{}] to [{}]",
						TieredMergePolicyProvider.this.maxMergeAtOnce, maxMergeAtOnce);
				TieredMergePolicyProvider.this.maxMergeAtOnce = maxMergeAtOnce;
				for (CustomTieredMergePolicyProvider policy : policies) {
					policy.setMaxMergeAtOnce(maxMergeAtOnce);
				}
			}

			int maxMergeAtOnceExplicit = settings.getAsInt("index.merge.policy.max_merge_at_once_explicit",
					TieredMergePolicyProvider.this.maxMergeAtOnceExplicit);
			if (maxMergeAtOnceExplicit != TieredMergePolicyProvider.this.maxMergeAtOnceExplicit) {
				logger.info("updating [max_merge_at_once_explicit] from [{}] to [{}]",
						TieredMergePolicyProvider.this.maxMergeAtOnceExplicit, maxMergeAtOnceExplicit);
				TieredMergePolicyProvider.this.maxMergeAtOnceExplicit = maxMergeAtOnceExplicit;
				for (CustomTieredMergePolicyProvider policy : policies) {
					policy.setMaxMergeAtOnceExplicit(maxMergeAtOnceExplicit);
				}
			}

			ByteSizeValue maxMergedSegment = settings.getAsBytesSize("index.merge.policy.max_merged_segment",
					TieredMergePolicyProvider.this.maxMergedSegment);
			if (!maxMergedSegment.equals(TieredMergePolicyProvider.this.maxMergedSegment)) {
				logger.info("updating [max_merged_segment] from [{}] to [{}]",
						TieredMergePolicyProvider.this.maxMergedSegment, maxMergedSegment);
				TieredMergePolicyProvider.this.maxMergedSegment = maxMergedSegment;
				for (CustomTieredMergePolicyProvider policy : policies) {
					policy.setFloorSegmentMB(maxMergedSegment.mbFrac());
				}
			}

			double segmentsPerTier = settings.getAsDouble("index.merge.policy.segments_per_tier",
					TieredMergePolicyProvider.this.segmentsPerTier);
			if (segmentsPerTier != TieredMergePolicyProvider.this.segmentsPerTier) {
				logger.info("updating [segments_per_tier] from [{}] to [{}]",
						TieredMergePolicyProvider.this.segmentsPerTier, segmentsPerTier);
				TieredMergePolicyProvider.this.segmentsPerTier = segmentsPerTier;
				for (CustomTieredMergePolicyProvider policy : policies) {
					policy.setSegmentsPerTier(segmentsPerTier);
				}
			}

			double reclaimDeletesWeight = settings.getAsDouble("index.merge.policy.reclaim_deletes_weight",
					TieredMergePolicyProvider.this.reclaimDeletesWeight);
			if (reclaimDeletesWeight != TieredMergePolicyProvider.this.reclaimDeletesWeight) {
				logger.info("updating [reclaim_deletes_weight] from [{}] to [{}]",
						TieredMergePolicyProvider.this.reclaimDeletesWeight, reclaimDeletesWeight);
				TieredMergePolicyProvider.this.reclaimDeletesWeight = reclaimDeletesWeight;
				for (CustomTieredMergePolicyProvider policy : policies) {
					policy.setReclaimDeletesWeight(reclaimDeletesWeight);
				}
			}

			boolean compoundFormat = settings.getAsBoolean("index.compound_format",
					TieredMergePolicyProvider.this.compoundFormat);
			if (compoundFormat != TieredMergePolicyProvider.this.compoundFormat) {
				logger.info("updating index.compound_format from [{}] to [{}]",
						TieredMergePolicyProvider.this.compoundFormat, compoundFormat);
				TieredMergePolicyProvider.this.compoundFormat = compoundFormat;
				for (CustomTieredMergePolicyProvider policy : policies) {
					policy.setUseCompoundFile(compoundFormat);
				}
			}
		}
	}

	/**
	 * The Class CustomTieredMergePolicyProvider.
	 *
	 * @author l.xue.nong
	 */
	public static class CustomTieredMergePolicyProvider extends TieredMergePolicy {

		/** The provider. */
		private final TieredMergePolicyProvider provider;

		/**
		 * Instantiates a new custom tiered merge policy provider.
		 *
		 * @param provider the provider
		 */
		public CustomTieredMergePolicyProvider(TieredMergePolicyProvider provider) {
			super();
			this.provider = provider;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.index.TieredMergePolicy#close()
		 */
		@Override
		public void close() {
			super.close();
			provider.policies.remove(this);
		}
	}

	/**
	 * The Class EnableMergeTieredMergePolicyProvider.
	 *
	 * @author l.xue.nong
	 */
	public static class EnableMergeTieredMergePolicyProvider extends CustomTieredMergePolicyProvider implements
			EnableMergePolicy {

		/** The enable merge. */
		private final ThreadLocal<Boolean> enableMerge = new ThreadLocal<Boolean>() {
			@Override
			protected Boolean initialValue() {
				return Boolean.FALSE;
			}
		};

		/**
		 * Instantiates a new enable merge tiered merge policy provider.
		 *
		 * @param provider the provider
		 */
		public EnableMergeTieredMergePolicyProvider(TieredMergePolicyProvider provider) {
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
		 * @see cn.com.rebirth.search.core.index.merge.policy.TieredMergePolicyProvider.CustomTieredMergePolicyProvider#close()
		 */
		@Override
		public void close() {
			enableMerge.remove();
			super.close();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.index.TieredMergePolicy#findMerges(org.apache.lucene.index.SegmentInfos)
		 */
		@Override
		public MergePolicy.MergeSpecification findMerges(SegmentInfos infos) throws IOException {
			if (enableMerge.get() == Boolean.FALSE) {
				return null;
			}
			return super.findMerges(infos);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.index.TieredMergePolicy#findForcedMerges(org.apache.lucene.index.SegmentInfos, int, java.util.Map)
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
		 * @see org.apache.lucene.index.TieredMergePolicy#findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos)
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