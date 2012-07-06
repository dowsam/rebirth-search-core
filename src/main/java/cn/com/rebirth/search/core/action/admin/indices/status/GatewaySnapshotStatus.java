/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GatewaySnapshotStatus.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.status;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;


/**
 * The Class GatewaySnapshotStatus.
 *
 * @author l.xue.nong
 */
public class GatewaySnapshotStatus {

	
	/**
	 * The Enum Stage.
	 *
	 * @author l.xue.nong
	 */
	public static enum Stage {

		
		/** The NONE. */
		NONE((byte) 0),

		
		/** The INDEX. */
		INDEX((byte) 1),

		
		/** The TRANSLOG. */
		TRANSLOG((byte) 2),

		
		/** The FINALIZE. */
		FINALIZE((byte) 3),

		
		/** The DONE. */
		DONE((byte) 4),

		
		/** The FAILURE. */
		FAILURE((byte) 5);

		
		/** The value. */
		private final byte value;

		
		/**
		 * Instantiates a new stage.
		 *
		 * @param value the value
		 */
		Stage(byte value) {
			this.value = value;
		}

		
		/**
		 * Value.
		 *
		 * @return the byte
		 */
		public byte value() {
			return this.value;
		}

		
		/**
		 * From value.
		 *
		 * @param value the value
		 * @return the stage
		 */
		public static Stage fromValue(byte value) {
			if (value == 0) {
				return Stage.NONE;
			} else if (value == 1) {
				return Stage.INDEX;
			} else if (value == 2) {
				return Stage.TRANSLOG;
			} else if (value == 3) {
				return Stage.FINALIZE;
			} else if (value == 4) {
				return Stage.DONE;
			} else if (value == 5) {
				return Stage.FAILURE;
			}
			throw new RestartIllegalArgumentException("No stage found for [" + value + "]");
		}
	}

	
	/** The stage. */
	final Stage stage;

	
	/** The start time. */
	final long startTime;

	
	/** The time. */
	final long time;

	
	/** The index size. */
	final long indexSize;

	
	/** The expected number of operations. */
	final int expectedNumberOfOperations;

	
	/**
	 * Instantiates a new gateway snapshot status.
	 *
	 * @param stage the stage
	 * @param startTime the start time
	 * @param time the time
	 * @param indexSize the index size
	 * @param expectedNumberOfOperations the expected number of operations
	 */
	public GatewaySnapshotStatus(Stage stage, long startTime, long time, long indexSize, int expectedNumberOfOperations) {
		this.stage = stage;
		this.startTime = startTime;
		this.time = time;
		this.indexSize = indexSize;
		this.expectedNumberOfOperations = expectedNumberOfOperations;
	}

	
	/**
	 * Stage.
	 *
	 * @return the stage
	 */
	public Stage stage() {
		return this.stage;
	}

	
	/**
	 * Gets the stage.
	 *
	 * @return the stage
	 */
	public Stage getStage() {
		return stage();
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
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public long getStartTime() {
		return startTime();
	}

	
	/**
	 * Time.
	 *
	 * @return the time value
	 */
	public TimeValue time() {
		return TimeValue.timeValueMillis(time);
	}

	
	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public TimeValue getTime() {
		return time();
	}

	
	/**
	 * Index size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue indexSize() {
		return new ByteSizeValue(indexSize);
	}

	
	/**
	 * Gets the index size.
	 *
	 * @return the index size
	 */
	public ByteSizeValue getIndexSize() {
		return indexSize();
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
	 * Gets the expected number of operations.
	 *
	 * @return the expected number of operations
	 */
	public int getExpectedNumberOfOperations() {
		return expectedNumberOfOperations();
	}
}
