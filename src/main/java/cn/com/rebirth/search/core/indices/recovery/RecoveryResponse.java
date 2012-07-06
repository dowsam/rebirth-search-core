/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RecoveryResponse.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;

import com.google.common.collect.Lists;

/**
 * The Class RecoveryResponse.
 *
 * @author l.xue.nong
 */
class RecoveryResponse implements Streamable {

	/** The phase1 file names. */
	List<String> phase1FileNames = Lists.newArrayList();

	/** The phase1 file sizes. */
	List<Long> phase1FileSizes = Lists.newArrayList();

	/** The phase1 existing file names. */
	List<String> phase1ExistingFileNames = Lists.newArrayList();

	/** The phase1 existing file sizes. */
	List<Long> phase1ExistingFileSizes = Lists.newArrayList();

	/** The phase1 total size. */
	long phase1TotalSize;

	/** The phase1 existing total size. */
	long phase1ExistingTotalSize;

	/** The phase1 time. */
	long phase1Time;

	/** The phase1 throttling wait time. */
	long phase1ThrottlingWaitTime;

	/** The start time. */
	long startTime;

	/** The phase2 operations. */
	int phase2Operations;

	/** The phase2 time. */
	long phase2Time;

	/** The phase3 operations. */
	int phase3Operations;

	/** The phase3 time. */
	long phase3Time;

	/**
	 * Instantiates a new recovery response.
	 */
	RecoveryResponse() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		phase1FileNames = Lists.newArrayListWithCapacity(size);
		for (int i = 0; i < size; i++) {
			phase1FileNames.add(in.readUTF());
		}
		size = in.readVInt();
		phase1FileSizes = Lists.newArrayListWithCapacity(size);
		for (int i = 0; i < size; i++) {
			phase1FileSizes.add(in.readVLong());
		}

		size = in.readVInt();
		phase1ExistingFileNames = Lists.newArrayListWithCapacity(size);
		for (int i = 0; i < size; i++) {
			phase1ExistingFileNames.add(in.readUTF());
		}
		size = in.readVInt();
		phase1ExistingFileSizes = Lists.newArrayListWithCapacity(size);
		for (int i = 0; i < size; i++) {
			phase1ExistingFileSizes.add(in.readVLong());
		}

		phase1TotalSize = in.readVLong();
		phase1ExistingTotalSize = in.readVLong();
		phase1Time = in.readVLong();
		phase1ThrottlingWaitTime = in.readVLong();
		startTime = in.readVLong();
		phase2Operations = in.readVInt();
		phase2Time = in.readVLong();
		phase3Operations = in.readVInt();
		phase3Time = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(phase1FileNames.size());
		for (String name : phase1FileNames) {
			out.writeUTF(name);
		}
		out.writeVInt(phase1FileSizes.size());
		for (long size : phase1FileSizes) {
			out.writeVLong(size);
		}

		out.writeVInt(phase1ExistingFileNames.size());
		for (String name : phase1ExistingFileNames) {
			out.writeUTF(name);
		}
		out.writeVInt(phase1ExistingFileSizes.size());
		for (long size : phase1ExistingFileSizes) {
			out.writeVLong(size);
		}

		out.writeVLong(phase1TotalSize);
		out.writeVLong(phase1ExistingTotalSize);
		out.writeVLong(phase1Time);
		out.writeVLong(phase1ThrottlingWaitTime);
		out.writeVLong(startTime);
		out.writeVInt(phase2Operations);
		out.writeVLong(phase2Time);
		out.writeVInt(phase3Operations);
		out.writeVLong(phase3Time);
	}
}
