/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardGateway.java 2012-7-6 14:30:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway;

import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.search.core.index.CloseableIndexComponent;
import cn.com.rebirth.search.core.index.deletionpolicy.SnapshotIndexCommit;
import cn.com.rebirth.search.core.index.shard.IndexShardComponent;
import cn.com.rebirth.search.core.index.translog.Translog;

/**
 * The Interface IndexShardGateway.
 *
 * @author l.xue.nong
 */
public interface IndexShardGateway extends IndexShardComponent, CloseableIndexComponent {

	/**
	 * Type.
	 *
	 * @return the string
	 */
	String type();

	/**
	 * Recovery status.
	 *
	 * @return the recovery status
	 */
	RecoveryStatus recoveryStatus();

	/**
	 * Last snapshot status.
	 *
	 * @return the snapshot status
	 */
	SnapshotStatus lastSnapshotStatus();

	/**
	 * Current snapshot status.
	 *
	 * @return the snapshot status
	 */
	SnapshotStatus currentSnapshotStatus();

	/**
	 * Recover.
	 *
	 * @param indexShouldExists the index should exists
	 * @param recoveryStatus the recovery status
	 * @throws IndexShardGatewayRecoveryException the index shard gateway recovery exception
	 */
	void recover(boolean indexShouldExists, RecoveryStatus recoveryStatus) throws IndexShardGatewayRecoveryException;

	/**
	 * Snapshot.
	 *
	 * @param snapshot the snapshot
	 * @return the snapshot status
	 * @throws IndexShardGatewaySnapshotFailedException the index shard gateway snapshot failed exception
	 */
	SnapshotStatus snapshot(Snapshot snapshot) throws IndexShardGatewaySnapshotFailedException;

	/**
	 * Requires snapshot.
	 *
	 * @return true, if successful
	 */
	boolean requiresSnapshot();

	/**
	 * Requires snapshot scheduling.
	 *
	 * @return true, if successful
	 */
	boolean requiresSnapshotScheduling();

	/**
	 * Obtain snapshot lock.
	 *
	 * @return the snapshot lock
	 * @throws Exception the exception
	 */
	SnapshotLock obtainSnapshotLock() throws Exception;

	/**
	 * The Interface SnapshotLock.
	 *
	 * @author l.xue.nong
	 */
	public static interface SnapshotLock {

		/**
		 * Release.
		 */
		void release();
	}

	/** The Constant NO_SNAPSHOT_LOCK. */
	public static final SnapshotLock NO_SNAPSHOT_LOCK = new SnapshotLock() {
		@Override
		public void release() {
		}
	};

	/**
	 * The Class Snapshot.
	 *
	 * @author l.xue.nong
	 */
	public static class Snapshot {

		/** The index commit. */
		private final SnapshotIndexCommit indexCommit;

		/** The translog snapshot. */
		private final Translog.Snapshot translogSnapshot;

		/** The last index version. */
		private final long lastIndexVersion;

		/** The last translog id. */
		private final long lastTranslogId;

		/** The last translog length. */
		private final long lastTranslogLength;

		/** The last total translog operations. */
		private final int lastTotalTranslogOperations;

		/**
		 * Instantiates a new snapshot.
		 *
		 * @param indexCommit the index commit
		 * @param translogSnapshot the translog snapshot
		 * @param lastIndexVersion the last index version
		 * @param lastTranslogId the last translog id
		 * @param lastTranslogLength the last translog length
		 * @param lastTotalTranslogOperations the last total translog operations
		 */
		public Snapshot(SnapshotIndexCommit indexCommit, Translog.Snapshot translogSnapshot, long lastIndexVersion,
				long lastTranslogId, long lastTranslogLength, int lastTotalTranslogOperations) {
			this.indexCommit = indexCommit;
			this.translogSnapshot = translogSnapshot;
			this.lastIndexVersion = lastIndexVersion;
			this.lastTranslogId = lastTranslogId;
			this.lastTranslogLength = lastTranslogLength;
			this.lastTotalTranslogOperations = lastTotalTranslogOperations;
		}

		/**
		 * Index changed.
		 *
		 * @return true, if successful
		 */
		public boolean indexChanged() {
			return lastIndexVersion != indexCommit.getVersion();
		}

		/**
		 * New translog created.
		 *
		 * @return true, if successful
		 */
		public boolean newTranslogCreated() {
			return translogSnapshot.translogId() != lastTranslogId;
		}

		/**
		 * Same translog new operations.
		 *
		 * @return true, if successful
		 */
		public boolean sameTranslogNewOperations() {
			if (newTranslogCreated()) {
				throw new RebirthIllegalStateException("Should not be called when there is a new translog");
			}
			return translogSnapshot.length() > lastTranslogLength;
		}

		/**
		 * Index commit.
		 *
		 * @return the snapshot index commit
		 */
		public SnapshotIndexCommit indexCommit() {
			return indexCommit;
		}

		/**
		 * Translog snapshot.
		 *
		 * @return the translog. snapshot
		 */
		public Translog.Snapshot translogSnapshot() {
			return translogSnapshot;
		}

		/**
		 * Last index version.
		 *
		 * @return the long
		 */
		public long lastIndexVersion() {
			return lastIndexVersion;
		}

		/**
		 * Last translog id.
		 *
		 * @return the long
		 */
		public long lastTranslogId() {
			return lastTranslogId;
		}

		/**
		 * Last translog length.
		 *
		 * @return the long
		 */
		public long lastTranslogLength() {
			return lastTranslogLength;
		}

		/**
		 * Last total translog operations.
		 *
		 * @return the int
		 */
		public int lastTotalTranslogOperations() {
			return this.lastTotalTranslogOperations;
		}
	}
}
