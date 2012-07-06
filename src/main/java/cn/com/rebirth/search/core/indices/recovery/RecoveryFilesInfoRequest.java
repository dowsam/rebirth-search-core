/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RecoveryFilesInfoRequest.java 2012-7-6 14:29:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class RecoveryFilesInfoRequest.
 *
 * @author l.xue.nong
 */
class RecoveryFilesInfoRequest implements Streamable {

	/** The shard id. */
	ShardId shardId;

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

	/**
	 * Instantiates a new recovery files info request.
	 */
	RecoveryFilesInfoRequest() {
	}

	/**
	 * Instantiates a new recovery files info request.
	 *
	 * @param shardId the shard id
	 * @param phase1FileNames the phase1 file names
	 * @param phase1FileSizes the phase1 file sizes
	 * @param phase1ExistingFileNames the phase1 existing file names
	 * @param phase1ExistingFileSizes the phase1 existing file sizes
	 * @param phase1TotalSize the phase1 total size
	 * @param phase1ExistingTotalSize the phase1 existing total size
	 */
	RecoveryFilesInfoRequest(ShardId shardId, List<String> phase1FileNames, List<Long> phase1FileSizes,
			List<String> phase1ExistingFileNames, List<Long> phase1ExistingFileSizes, long phase1TotalSize,
			long phase1ExistingTotalSize) {
		this.shardId = shardId;
		this.phase1FileNames = phase1FileNames;
		this.phase1FileSizes = phase1FileSizes;
		this.phase1ExistingFileNames = phase1ExistingFileNames;
		this.phase1ExistingFileSizes = phase1ExistingFileSizes;
		this.phase1TotalSize = phase1TotalSize;
		this.phase1ExistingTotalSize = phase1ExistingTotalSize;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardId = ShardId.readShardId(in);
		int size = in.readVInt();
		phase1FileNames = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			phase1FileNames.add(in.readUTF());
		}

		size = in.readVInt();
		phase1FileSizes = new ArrayList<Long>(size);
		for (int i = 0; i < size; i++) {
			phase1FileSizes.add(in.readVLong());
		}

		size = in.readVInt();
		phase1ExistingFileNames = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			phase1ExistingFileNames.add(in.readUTF());
		}

		size = in.readVInt();
		phase1ExistingFileSizes = new ArrayList<Long>(size);
		for (int i = 0; i < size; i++) {
			phase1ExistingFileSizes.add(in.readVLong());
		}

		phase1TotalSize = in.readVLong();
		phase1ExistingTotalSize = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardId.writeTo(out);

		out.writeVInt(phase1FileNames.size());
		for (String phase1FileName : phase1FileNames) {
			out.writeUTF(phase1FileName);
		}

		out.writeVInt(phase1FileSizes.size());
		for (Long phase1FileSize : phase1FileSizes) {
			out.writeVLong(phase1FileSize);
		}

		out.writeVInt(phase1ExistingFileNames.size());
		for (String phase1ExistingFileName : phase1ExistingFileNames) {
			out.writeUTF(phase1ExistingFileName);
		}

		out.writeVInt(phase1ExistingFileSizes.size());
		for (Long phase1ExistingFileSize : phase1ExistingFileSizes) {
			out.writeVLong(phase1ExistingFileSize);
		}

		out.writeVLong(phase1TotalSize);
		out.writeVLong(phase1ExistingTotalSize);
	}
}
