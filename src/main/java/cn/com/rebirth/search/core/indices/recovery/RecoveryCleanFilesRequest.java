/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RecoveryCleanFilesRequest.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;
import java.util.Set;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.Sets;

/**
 * The Class RecoveryCleanFilesRequest.
 *
 * @author l.xue.nong
 */
class RecoveryCleanFilesRequest implements Streamable {

	/** The shard id. */
	private ShardId shardId;

	/** The snapshot files. */
	private Set<String> snapshotFiles;

	/**
	 * Instantiates a new recovery clean files request.
	 */
	RecoveryCleanFilesRequest() {
	}

	/**
	 * Instantiates a new recovery clean files request.
	 *
	 * @param shardId the shard id
	 * @param snapshotFiles the snapshot files
	 */
	RecoveryCleanFilesRequest(ShardId shardId, Set<String> snapshotFiles) {
		this.shardId = shardId;
		this.snapshotFiles = snapshotFiles;
	}

	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	public ShardId shardId() {
		return shardId;
	}

	/**
	 * Snapshot files.
	 *
	 * @return the sets the
	 */
	public Set<String> snapshotFiles() {
		return snapshotFiles;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardId = ShardId.readShardId(in);
		int size = in.readVInt();
		snapshotFiles = Sets.newHashSetWithExpectedSize(size);
		for (int i = 0; i < size; i++) {
			snapshotFiles.add(in.readUTF());
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardId.writeTo(out);
		out.writeVInt(snapshotFiles.size());
		for (String snapshotFile : snapshotFiles) {
			out.writeUTF(snapshotFile);
		}
	}
}
