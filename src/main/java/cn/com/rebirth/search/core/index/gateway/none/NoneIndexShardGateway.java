/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoneIndexShardGateway.java 2012-7-6 14:30:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway.none;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.gateway.none.NoneGateway;
import cn.com.rebirth.search.core.index.gateway.IndexShardGateway;
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewayRecoveryException;
import cn.com.rebirth.search.core.index.gateway.RecoveryStatus;
import cn.com.rebirth.search.core.index.gateway.SnapshotStatus;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;

/**
 * The Class NoneIndexShardGateway.
 *
 * @author l.xue.nong
 */
public class NoneIndexShardGateway extends AbstractIndexShardComponent implements IndexShardGateway {

	/** The index shard. */
	private final InternalIndexShard indexShard;

	/** The recovery status. */
	private final RecoveryStatus recoveryStatus = new RecoveryStatus();

	/**
	 * Instantiates a new none index shard gateway.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexShard the index shard
	 */
	@Inject
	public NoneIndexShardGateway(ShardId shardId, @IndexSettings Settings indexSettings, IndexShard indexShard) {
		super(shardId, indexSettings);
		this.indexShard = (InternalIndexShard) indexShard;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "_none_";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#recoveryStatus()
	 */
	@Override
	public RecoveryStatus recoveryStatus() {
		return recoveryStatus;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#recover(boolean, cn.com.rebirth.search.core.index.gateway.RecoveryStatus)
	 */
	@Override
	public void recover(boolean indexShouldExists, RecoveryStatus recoveryStatus)
			throws IndexShardGatewayRecoveryException {
		recoveryStatus().index().startTime(System.currentTimeMillis());

		try {
			indexShard.store().deleteContent();
		} catch (IOException e) {
			logger.warn("failed to clean store before starting shard", e);
		}
		indexShard.start("post recovery from gateway");
		recoveryStatus.index().time(System.currentTimeMillis() - recoveryStatus.index().startTime());
		recoveryStatus.translog().startTime(System.currentTimeMillis());
		recoveryStatus.translog().time(System.currentTimeMillis() - recoveryStatus.index().startTime());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#type()
	 */
	@Override
	public String type() {
		return NoneGateway.TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#snapshot(cn.com.rebirth.search.core.index.gateway.IndexShardGateway.Snapshot)
	 */
	@Override
	public SnapshotStatus snapshot(Snapshot snapshot) {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#lastSnapshotStatus()
	 */
	@Override
	public SnapshotStatus lastSnapshotStatus() {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#currentSnapshotStatus()
	 */
	@Override
	public SnapshotStatus currentSnapshotStatus() {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#requiresSnapshot()
	 */
	@Override
	public boolean requiresSnapshot() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#requiresSnapshotScheduling()
	 */
	@Override
	public boolean requiresSnapshotScheduling() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.CloseableIndexComponent#close(boolean)
	 */
	@Override
	public void close(boolean delete) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#obtainSnapshotLock()
	 */
	@Override
	public SnapshotLock obtainSnapshotLock() throws Exception {
		return NO_SNAPSHOT_LOCK;
	}
}
