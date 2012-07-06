/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FsStats.java 2012-3-29 15:02:27 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.fs;

import java.io.IOException;
import java.util.Iterator;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

import com.google.common.collect.Iterators;


/**
 * The Class FsStats.
 *
 * @author l.xue.nong
 */
public class FsStats implements Iterable<FsStats.Info>, Streamable, ToXContent {

	
	/**
	 * The Class Info.
	 *
	 * @author l.xue.nong
	 */
	public static class Info implements Streamable {

		
		/** The path. */
		String path;

		
		/** The mount. */
		@Nullable
		String mount;

		
		/** The dev. */
		@Nullable
		String dev;

		
		/** The total. */
		long total = -1;

		
		/** The free. */
		long free = -1;

		
		/** The available. */
		long available = -1;

		
		/** The disk reads. */
		long diskReads = -1;

		
		/** The disk writes. */
		long diskWrites = -1;

		
		/** The disk read bytes. */
		long diskReadBytes = -1;

		
		/** The disk write bytes. */
		long diskWriteBytes = -1;

		
		/** The disk queue. */
		double diskQueue = -1;

		
		/** The disk service time. */
		double diskServiceTime = -1;

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			path = in.readUTF();
			mount = in.readOptionalUTF();
			dev = in.readOptionalUTF();
			total = in.readLong();
			free = in.readLong();
			available = in.readLong();
			diskReads = in.readLong();
			diskWrites = in.readLong();
			diskReadBytes = in.readLong();
			diskWriteBytes = in.readLong();
			diskQueue = in.readDouble();
			diskServiceTime = in.readDouble();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(path);
			out.writeOptionalUTF(mount);
			out.writeOptionalUTF(dev);
			out.writeLong(total);
			out.writeLong(free);
			out.writeLong(available);
			out.writeLong(diskReads);
			out.writeLong(diskWrites);
			out.writeLong(diskReadBytes);
			out.writeLong(diskWriteBytes);
			out.writeDouble(diskQueue);
			out.writeDouble(diskServiceTime);
		}

		
		/**
		 * Total.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue total() {
			return new ByteSizeValue(total);
		}

		
		/**
		 * Gets the total.
		 *
		 * @return the total
		 */
		public ByteSizeValue getTotal() {
			return total();
		}

		
		/**
		 * Free.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue free() {
			return new ByteSizeValue(free);
		}

		
		/**
		 * Gets the free.
		 *
		 * @return the free
		 */
		public ByteSizeValue getFree() {
			return free();
		}

		
		/**
		 * Available.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue available() {
			return new ByteSizeValue(available);
		}

		
		/**
		 * Gets the available.
		 *
		 * @return the available
		 */
		public ByteSizeValue getAvailable() {
			return available();
		}

		
		/**
		 * Disk reads.
		 *
		 * @return the long
		 */
		public long diskReads() {
			return this.diskReads;
		}

		
		/**
		 * Gets the disk reads.
		 *
		 * @return the disk reads
		 */
		public long getDiskReads() {
			return this.diskReads;
		}

		
		/**
		 * Disk writes.
		 *
		 * @return the long
		 */
		public long diskWrites() {
			return this.diskWrites;
		}

		
		/**
		 * Gets the disk writes.
		 *
		 * @return the disk writes
		 */
		public long getDiskWrites() {
			return this.diskWrites;
		}

		
		/**
		 * Disk read size in bytes.
		 *
		 * @return the long
		 */
		public long diskReadSizeInBytes() {
			return diskReadBytes;
		}

		
		/**
		 * Gets the disk read size in bytes.
		 *
		 * @return the disk read size in bytes
		 */
		public long getDiskReadSizeInBytes() {
			return diskReadBytes;
		}

		
		/**
		 * Disk read size size.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue diskReadSizeSize() {
			return new ByteSizeValue(diskReadBytes);
		}

		
		/**
		 * Gets the disk read size size.
		 *
		 * @return the disk read size size
		 */
		public ByteSizeValue getDiskReadSizeSize() {
			return new ByteSizeValue(diskReadBytes);
		}

		
		/**
		 * Disk write size in bytes.
		 *
		 * @return the long
		 */
		public long diskWriteSizeInBytes() {
			return diskWriteBytes;
		}

		
		/**
		 * Gets the disk write size in bytes.
		 *
		 * @return the disk write size in bytes
		 */
		public long getDiskWriteSizeInBytes() {
			return diskWriteBytes;
		}

		
		/**
		 * Disk write size size.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue diskWriteSizeSize() {
			return new ByteSizeValue(diskWriteBytes);
		}

		
		/**
		 * Gets the disk write size size.
		 *
		 * @return the disk write size size
		 */
		public ByteSizeValue getDiskWriteSizeSize() {
			return new ByteSizeValue(diskWriteBytes);
		}

		
		/**
		 * Disk queue.
		 *
		 * @return the double
		 */
		public double diskQueue() {
			return diskQueue;
		}

		
		/**
		 * Gets the disk queue.
		 *
		 * @return the disk queue
		 */
		public double getDiskQueue() {
			return diskQueue;
		}

		
		/**
		 * Disk service time.
		 *
		 * @return the double
		 */
		public double diskServiceTime() {
			return diskServiceTime;
		}

		
		/**
		 * Gets the disk service time.
		 *
		 * @return the disk service time
		 */
		public double getDiskServiceTime() {
			return diskServiceTime;
		}
	}

	
	/** The timestamp. */
	long timestamp;

	
	/** The infos. */
	Info[] infos;

	
	/**
	 * Instantiates a new fs stats.
	 */
	FsStats() {

	}

	
	/**
	 * Instantiates a new fs stats.
	 *
	 * @param timestamp the timestamp
	 * @param infos the infos
	 */
	FsStats(long timestamp, Info[] infos) {
		this.timestamp = timestamp;
		this.infos = infos;
	}

	
	/**
	 * Timestamp.
	 *
	 * @return the long
	 */
	public long timestamp() {
		return timestamp;
	}

	
	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Info> iterator() {
		return Iterators.forArray(infos);
	}

	
	/**
	 * Read fs stats.
	 *
	 * @param in the in
	 * @return the fs stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static FsStats readFsStats(StreamInput in) throws IOException {
		FsStats stats = new FsStats();
		stats.readFrom(in);
		return stats;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		timestamp = in.readVLong();
		infos = new Info[in.readVInt()];
		for (int i = 0; i < infos.length; i++) {
			infos[i] = new Info();
			infos[i].readFrom(in);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(timestamp);
		out.writeVInt(infos.length);
		for (Info info : infos) {
			info.writeTo(out);
		}
	}

	
	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		
		/** The Constant FS. */
		static final XContentBuilderString FS = new XContentBuilderString("fs");

		
		/** The Constant TIMESTAMP. */
		static final XContentBuilderString TIMESTAMP = new XContentBuilderString("timestamp");

		
		/** The Constant DATA. */
		static final XContentBuilderString DATA = new XContentBuilderString("data");

		
		/** The Constant PATH. */
		static final XContentBuilderString PATH = new XContentBuilderString("path");

		
		/** The Constant MOUNT. */
		static final XContentBuilderString MOUNT = new XContentBuilderString("mount");

		
		/** The Constant DEV. */
		static final XContentBuilderString DEV = new XContentBuilderString("dev");

		
		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		
		/** The Constant TOTAL_IN_BYTES. */
		static final XContentBuilderString TOTAL_IN_BYTES = new XContentBuilderString("total_in_bytes");

		
		/** The Constant FREE. */
		static final XContentBuilderString FREE = new XContentBuilderString("free");

		
		/** The Constant FREE_IN_BYTES. */
		static final XContentBuilderString FREE_IN_BYTES = new XContentBuilderString("free_in_bytes");

		
		/** The Constant AVAILABLE. */
		static final XContentBuilderString AVAILABLE = new XContentBuilderString("available");

		
		/** The Constant AVAILABLE_IN_BYTES. */
		static final XContentBuilderString AVAILABLE_IN_BYTES = new XContentBuilderString("available_in_bytes");

		
		/** The Constant DISK_READS. */
		static final XContentBuilderString DISK_READS = new XContentBuilderString("disk_reads");

		
		/** The Constant DISK_WRITES. */
		static final XContentBuilderString DISK_WRITES = new XContentBuilderString("disk_writes");

		
		/** The Constant DISK_READ_SIZE. */
		static final XContentBuilderString DISK_READ_SIZE = new XContentBuilderString("disk_read_size");

		
		/** The Constant DISK_READ_SIZE_IN_BYTES. */
		static final XContentBuilderString DISK_READ_SIZE_IN_BYTES = new XContentBuilderString(
				"disk_read_size_in_bytes");

		
		/** The Constant DISK_WRITE_SIZE. */
		static final XContentBuilderString DISK_WRITE_SIZE = new XContentBuilderString("disk_write_size");

		
		/** The Constant DISK_WRITE_SIZE_IN_BYTES. */
		static final XContentBuilderString DISK_WRITE_SIZE_IN_BYTES = new XContentBuilderString(
				"disk_write_size_in_bytes");

		
		/** The Constant DISK_QUEUE. */
		static final XContentBuilderString DISK_QUEUE = new XContentBuilderString("disk_queue");

		
		/** The Constant DISK_SERVICE_TIME. */
		static final XContentBuilderString DISK_SERVICE_TIME = new XContentBuilderString("disk_service_time");
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.FS);
		builder.field(Fields.TIMESTAMP, timestamp);
		builder.startArray(Fields.DATA);
		for (Info info : infos) {
			builder.startObject();
			builder.field(Fields.PATH, info.path);
			if (info.mount != null) {
				builder.field(Fields.MOUNT, info.mount);
			}
			if (info.dev != null) {
				builder.field(Fields.DEV, info.dev);
			}

			if (info.total != -1) {
				builder.field(Fields.TOTAL, info.total().toString());
				builder.field(Fields.TOTAL_IN_BYTES, info.total);
			}
			if (info.free != -1) {
				builder.field(Fields.FREE, info.free().toString());
				builder.field(Fields.FREE_IN_BYTES, info.free);
			}
			if (info.available != -1) {
				builder.field(Fields.AVAILABLE, info.available().toString());
				builder.field(Fields.AVAILABLE_IN_BYTES, info.available);
			}

			if (info.diskReads != -1) {
				builder.field(Fields.DISK_READS, info.diskReads);
			}
			if (info.diskWrites != -1) {
				builder.field(Fields.DISK_WRITES, info.diskWrites);
			}

			if (info.diskReadBytes != -1) {
				builder.field(Fields.DISK_READ_SIZE, info.diskReadSizeSize().toString());
				builder.field(Fields.DISK_READ_SIZE_IN_BYTES, info.diskReadSizeInBytes());
			}
			if (info.diskWriteBytes != -1) {
				builder.field(Fields.DISK_WRITE_SIZE, info.diskWriteSizeSize().toString());
				builder.field(Fields.DISK_WRITE_SIZE_IN_BYTES, info.diskWriteSizeInBytes());
			}

			if (info.diskQueue != -1) {
				builder.field(Fields.DISK_QUEUE, Strings.format1Decimals(info.diskQueue, ""));
			}
			if (info.diskServiceTime != -1) {
				builder.field(Fields.DISK_SERVICE_TIME, Strings.format1Decimals(info.diskServiceTime, ""));
			}

			builder.endObject();
		}
		builder.endArray();
		builder.endObject();
		return builder;
	}
}
