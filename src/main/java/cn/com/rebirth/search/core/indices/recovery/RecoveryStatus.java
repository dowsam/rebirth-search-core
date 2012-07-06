/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RecoveryStatus.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.store.IndexOutput;

import cn.com.rebirth.commons.concurrent.ConcurrentCollections;

/**
 * The Class RecoveryStatus.
 *
 * @author l.xue.nong
 */
public class RecoveryStatus {

	/**
	 * The Enum Stage.
	 *
	 * @author l.xue.nong
	 */
	public static enum Stage {

		/** The init. */
		INIT,

		/** The index. */
		INDEX,

		/** The translog. */
		TRANSLOG,

		/** The finalize. */
		FINALIZE,

		/** The done. */
		DONE
	}

	/** The open index outputs. */
	ConcurrentMap<String, IndexOutput> openIndexOutputs = ConcurrentCollections.newConcurrentMap();

	/** The checksums. */
	ConcurrentMap<String, String> checksums = ConcurrentCollections.newConcurrentMap();

	/** The start time. */
	final long startTime = System.currentTimeMillis();

	/** The time. */
	long time;

	/** The phase1 file names. */
	List<String> phase1FileNames;

	/** The phase1 file sizes. */
	List<Long> phase1FileSizes;

	/** The phase1 existing file names. */
	List<String> phase1ExistingFileNames;

	/** The phase1 existing file sizes. */
	List<Long> phase1ExistingFileSizes;

	/** The phase1 total size. */
	long phase1TotalSize;

	/** The phase1 existing total size. */
	long phase1ExistingTotalSize;

	/** The stage. */
	volatile Stage stage = Stage.INIT;

	/** The current translog operations. */
	volatile long currentTranslogOperations = 0;

	/** The current files size. */
	AtomicLong currentFilesSize = new AtomicLong();

	/**
	 * Start time.
	 *
	 * @return the long
	 */
	public long startTime() {
		return startTime;
	}

	/**
	 * Time.
	 *
	 * @return the long
	 */
	public long time() {
		return this.time;
	}

	/**
	 * Phase1 total size.
	 *
	 * @return the long
	 */
	public long phase1TotalSize() {
		return phase1TotalSize;
	}

	/**
	 * Phase1 existing total size.
	 *
	 * @return the long
	 */
	public long phase1ExistingTotalSize() {
		return phase1ExistingTotalSize;
	}

	/**
	 * Stage.
	 *
	 * @return the stage
	 */
	public Stage stage() {
		return stage;
	}

	/**
	 * Current translog operations.
	 *
	 * @return the long
	 */
	public long currentTranslogOperations() {
		return currentTranslogOperations;
	}

	/**
	 * Current files size.
	 *
	 * @return the long
	 */
	public long currentFilesSize() {
		return currentFilesSize.get();
	}
}
