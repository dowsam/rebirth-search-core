/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SnapshotStatus.java 2012-7-6 14:30:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway;

/**
 * The Class SnapshotStatus.
 *
 * @author l.xue.nong
 */
public class SnapshotStatus {

	/**
	 * The Enum Stage.
	 *
	 * @author l.xue.nong
	 */
	public static enum Stage {

		/** The none. */
		NONE,

		/** The index. */
		INDEX,

		/** The translog. */
		TRANSLOG,

		/** The finalize. */
		FINALIZE,

		/** The done. */
		DONE,

		/** The failure. */
		FAILURE
	}

	/** The stage. */
	private Stage stage = Stage.NONE;

	/** The start time. */
	private long startTime;

	/** The time. */
	private long time;

	/** The index. */
	private Index index = new Index();

	/** The translog. */
	private Translog translog = new Translog();

	/** The failure. */
	private Throwable failure;

	/**
	 * Stage.
	 *
	 * @return the stage
	 */
	public Stage stage() {
		return this.stage;
	}

	/**
	 * Update stage.
	 *
	 * @param stage the stage
	 * @return the snapshot status
	 */
	public SnapshotStatus updateStage(Stage stage) {
		this.stage = stage;
		return this;
	}

	/**
	 * Start time.
	 *
	 * @return the long
	 */
	public long startTime() {
		return this.startTime;
	}

	/**
	 * Start time.
	 *
	 * @param startTime the start time
	 */
	public void startTime(long startTime) {
		this.startTime = startTime;
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
	 * Time.
	 *
	 * @param time the time
	 */
	public void time(long time) {
		this.time = time;
	}

	/**
	 * Failed.
	 *
	 * @param failure the failure
	 */
	public void failed(Throwable failure) {
		this.failure = failure;
	}

	/**
	 * Index.
	 *
	 * @return the index
	 */
	public Index index() {
		return index;
	}

	/**
	 * Translog.
	 *
	 * @return the translog
	 */
	public Translog translog() {
		return translog;
	}

	/**
	 * The Class Index.
	 *
	 * @author l.xue.nong
	 */
	public static class Index {

		/** The start time. */
		private long startTime;

		/** The time. */
		private long time;

		/** The number of files. */
		private int numberOfFiles;

		/** The total size. */
		private long totalSize;

		/**
		 * Start time.
		 *
		 * @return the long
		 */
		public long startTime() {
			return this.startTime;
		}

		/**
		 * Start time.
		 *
		 * @param startTime the start time
		 */
		public void startTime(long startTime) {
			this.startTime = startTime;
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
		 * Time.
		 *
		 * @param time the time
		 */
		public void time(long time) {
			this.time = time;
		}

		/**
		 * Files.
		 *
		 * @param numberOfFiles the number of files
		 * @param totalSize the total size
		 */
		public void files(int numberOfFiles, long totalSize) {
			this.numberOfFiles = numberOfFiles;
			this.totalSize = totalSize;
		}

		/**
		 * Number of files.
		 *
		 * @return the int
		 */
		public int numberOfFiles() {
			return numberOfFiles;
		}

		/**
		 * Total size.
		 *
		 * @return the long
		 */
		public long totalSize() {
			return totalSize;
		}
	}

	/**
	 * The Class Translog.
	 *
	 * @author l.xue.nong
	 */
	public static class Translog {

		/** The start time. */
		private long startTime;

		/** The time. */
		private long time;

		/** The expected number of operations. */
		private int expectedNumberOfOperations;

		/**
		 * Start time.
		 *
		 * @return the long
		 */
		public long startTime() {
			return this.startTime;
		}

		/**
		 * Start time.
		 *
		 * @param startTime the start time
		 */
		public void startTime(long startTime) {
			this.startTime = startTime;
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
		 * Time.
		 *
		 * @param time the time
		 */
		public void time(long time) {
			this.time = time;
		}

		/**
		 * Expected number of operations.
		 *
		 * @return the int
		 */
		public int expectedNumberOfOperations() {
			return expectedNumberOfOperations;
		}

		/**
		 * Expected number of operations.
		 *
		 * @param expectedNumberOfOperations the expected number of operations
		 */
		public void expectedNumberOfOperations(int expectedNumberOfOperations) {
			this.expectedNumberOfOperations = expectedNumberOfOperations;
		}
	}
}
