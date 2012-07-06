/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GatewayRecoveryStatus.java 2012-3-29 15:02:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.status;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;


/**
 * The Class GatewayRecoveryStatus.
 *
 * @author l.xue.nong
 */
public class GatewayRecoveryStatus {

	
	/**
	 * The Enum Stage.
	 *
	 * @author l.xue.nong
	 */
	public enum Stage {

		
		/** The INIT. */
		INIT((byte) 0),
		
		/** The INDEX. */
		INDEX((byte) 1),
		
		/** The TRANSLOG. */
		TRANSLOG((byte) 2),
		
		/** The FINALIZE. */
		FINALIZE((byte) 3),
		
		/** The DONE. */
		DONE((byte) 4);

		
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
			return value;
		}

		
		/**
		 * From value.
		 *
		 * @param value the value
		 * @return the stage
		 */
		public static Stage fromValue(byte value) {
			if (value == 0) {
				return INIT;
			} else if (value == 1) {
				return INDEX;
			} else if (value == 2) {
				return TRANSLOG;
			} else if (value == 3) {
				return FINALIZE;
			} else if (value == 4) {
				return DONE;
			}
			throw new RestartIllegalArgumentException("No stage found for [" + value + ']');
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

	
	/** The reused index size. */
	final long reusedIndexSize;

	
	/** The recovered index size. */
	final long recoveredIndexSize;

	
	/** The recovered translog operations. */
	final long recoveredTranslogOperations;

	
	/**
	 * Instantiates a new gateway recovery status.
	 *
	 * @param stage the stage
	 * @param startTime the start time
	 * @param time the time
	 * @param indexSize the index size
	 * @param reusedIndexSize the reused index size
	 * @param recoveredIndexSize the recovered index size
	 * @param recoveredTranslogOperations the recovered translog operations
	 */
	public GatewayRecoveryStatus(Stage stage, long startTime, long time, long indexSize, long reusedIndexSize,
			long recoveredIndexSize, long recoveredTranslogOperations) {
		this.stage = stage;
		this.startTime = startTime;
		this.time = time;
		this.indexSize = indexSize;
		this.reusedIndexSize = reusedIndexSize;
		this.recoveredIndexSize = recoveredIndexSize;
		this.recoveredTranslogOperations = recoveredTranslogOperations;
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
		return this.startTime;
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
	 * Reused index size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue reusedIndexSize() {
		return new ByteSizeValue(reusedIndexSize);
	}

	
	/**
	 * Gets the reused index size.
	 *
	 * @return the reused index size
	 */
	public ByteSizeValue getReusedIndexSize() {
		return reusedIndexSize();
	}

	
	/**
	 * Expected recovered index size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue expectedRecoveredIndexSize() {
		return new ByteSizeValue(indexSize - reusedIndexSize);
	}

	
	/**
	 * Gets the expected recovered index size.
	 *
	 * @return the expected recovered index size
	 */
	public ByteSizeValue getExpectedRecoveredIndexSize() {
		return expectedRecoveredIndexSize();
	}

	
	/**
	 * Recovered index size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue recoveredIndexSize() {
		return new ByteSizeValue(recoveredIndexSize);
	}

	
	/**
	 * Gets the recovered index size.
	 *
	 * @return the recovered index size
	 */
	public ByteSizeValue getRecoveredIndexSize() {
		return recoveredIndexSize();
	}

	
	/**
	 * Index recovery progress.
	 *
	 * @return the int
	 */
	public int indexRecoveryProgress() {
		if (recoveredIndexSize == 0) {
			if (indexSize != 0 && indexSize == reusedIndexSize) {
				return 100;
			}
			return 0;
		}
		return (int) (((double) recoveredIndexSize) / expectedRecoveredIndexSize().bytes() * 100);
	}

	
	/**
	 * Gets the index recovery progress.
	 *
	 * @return the index recovery progress
	 */
	public int getIndexRecoveryProgress() {
		return indexRecoveryProgress();
	}

	
	/**
	 * Recovered translog operations.
	 *
	 * @return the long
	 */
	public long recoveredTranslogOperations() {
		return recoveredTranslogOperations;
	}

	
	/**
	 * Gets the recovered translog operations.
	 *
	 * @return the recovered translog operations
	 */
	public long getRecoveredTranslogOperations() {
		return recoveredTranslogOperations();
	}
}
