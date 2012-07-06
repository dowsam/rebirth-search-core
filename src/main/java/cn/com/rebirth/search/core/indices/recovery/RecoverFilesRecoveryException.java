/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RecoverFilesRecoveryException.java 2012-3-29 15:00:57 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices.recovery;

import cn.com.rebirth.commons.exception.RestartWrapperException;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class RecoverFilesRecoveryException.
 *
 * @author l.xue.nong
 */
public class RecoverFilesRecoveryException extends IndexShardException implements RestartWrapperException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6487786478289127906L;

	
	/** The number of files. */
	private final int numberOfFiles;

	
	/** The total files size. */
	private final ByteSizeValue totalFilesSize;

	
	/**
	 * Instantiates a new recover files recovery exception.
	 *
	 * @param shardId the shard id
	 * @param numberOfFiles the number of files
	 * @param totalFilesSize the total files size
	 * @param cause the cause
	 */
	public RecoverFilesRecoveryException(ShardId shardId, int numberOfFiles, ByteSizeValue totalFilesSize,
			Throwable cause) {
		super(shardId, "Failed to transfer [" + numberOfFiles + "] files with total size of [" + totalFilesSize + "]",
				cause);
		this.numberOfFiles = numberOfFiles;
		this.totalFilesSize = totalFilesSize;
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
	 * Total files size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue totalFilesSize() {
		return totalFilesSize;
	}
}
