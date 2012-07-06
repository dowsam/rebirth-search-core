/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TrackingConcurrentMergeScheduler.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package org.apache.lucene.index;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.metrics.CounterMetric;
import cn.com.rebirth.search.commons.metrics.MeanMetric;

/**
 * The Class TrackingConcurrentMergeScheduler.
 *
 * @author l.xue.nong
 */
public class TrackingConcurrentMergeScheduler extends ConcurrentMergeScheduler {

	/** The logger. */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/** The total merges. */
	private final MeanMetric totalMerges = new MeanMetric();

	/** The total merges num docs. */
	private final CounterMetric totalMergesNumDocs = new CounterMetric();

	/** The total merges size in bytes. */
	private final CounterMetric totalMergesSizeInBytes = new CounterMetric();

	/** The current merges. */
	private final CounterMetric currentMerges = new CounterMetric();

	/** The current merges num docs. */
	private final CounterMetric currentMergesNumDocs = new CounterMetric();

	/** The current merges size in bytes. */
	private final CounterMetric currentMergesSizeInBytes = new CounterMetric();

	/**
	 * Instantiates a new tracking concurrent merge scheduler.
	 */
	public TrackingConcurrentMergeScheduler() {
		super();
	}

	/**
	 * Total merges.
	 *
	 * @return the long
	 */
	public long totalMerges() {
		return totalMerges.count();
	}

	/**
	 * Total merge time.
	 *
	 * @return the long
	 */
	public long totalMergeTime() {
		return totalMerges.sum();
	}

	/**
	 * Total merge num docs.
	 *
	 * @return the long
	 */
	public long totalMergeNumDocs() {
		return totalMergesNumDocs.count();
	}

	/**
	 * Total merge size in bytes.
	 *
	 * @return the long
	 */
	public long totalMergeSizeInBytes() {
		return totalMergesSizeInBytes.count();
	}

	/**
	 * Current merges.
	 *
	 * @return the long
	 */
	public long currentMerges() {
		return currentMerges.count();
	}

	/**
	 * Current merges num docs.
	 *
	 * @return the long
	 */
	public long currentMergesNumDocs() {
		return currentMergesNumDocs.count();
	}

	/**
	 * Current merges size in bytes.
	 *
	 * @return the long
	 */
	public long currentMergesSizeInBytes() {
		return currentMergesSizeInBytes.count();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.index.ConcurrentMergeScheduler#doMerge(org.apache.lucene.index.MergePolicy.OneMerge)
	 */
	@Override
	protected void doMerge(MergePolicy.OneMerge merge) throws IOException {
		int totalNumDocs = merge.totalNumDocs();
		long totalSizeInBytes = merge.totalBytesSize();
		long time = System.currentTimeMillis();
		currentMerges.inc();
		currentMergesNumDocs.inc(totalNumDocs);
		currentMergesSizeInBytes.inc(totalSizeInBytes);
		if (logger.isTraceEnabled()) {
			logger.trace("merge [{}] starting...", merge.info.name);
		}
		try {
			TrackingMergeScheduler.setCurrentMerge(merge);
			super.doMerge(merge);
		} finally {
			TrackingMergeScheduler.removeCurrentMerge();
			long took = System.currentTimeMillis() - time;

			currentMerges.dec();
			currentMergesNumDocs.dec(totalNumDocs);
			currentMergesSizeInBytes.dec(totalSizeInBytes);

			totalMergesNumDocs.inc(totalNumDocs);
			totalMergesSizeInBytes.inc(totalSizeInBytes);
			totalMerges.inc(took);
			if (took > 20000) {
				logger.debug("merge [{}] done, took [{}]", merge.info.name, TimeValue.timeValueMillis(took));
			} else if (logger.isTraceEnabled()) {
				logger.trace("merge [{}] done, took [{}]", merge.info.name, TimeValue.timeValueMillis(took));
			}
		}
	}
}