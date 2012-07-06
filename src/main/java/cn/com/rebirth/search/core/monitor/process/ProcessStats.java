/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ProcessStats.java 2012-3-29 15:02:08 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.process;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;


/**
 * The Class ProcessStats.
 *
 * @author l.xue.nong
 */
public class ProcessStats implements Streamable, Serializable, ToXContent {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8615214950636329226L;

	
	/** The timestamp. */
	long timestamp = -1;

	
	/** The open file descriptors. */
	long openFileDescriptors;

	
	/** The cpu. */
	Cpu cpu = null;

	
	/** The mem. */
	Mem mem = null;

	
	/**
	 * Instantiates a new process stats.
	 */
	ProcessStats() {
	}

	
	/**
	 * Timestamp.
	 *
	 * @return the long
	 */
	public long timestamp() {
		return this.timestamp;
	}

	
	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp();
	}

	
	/**
	 * Open file descriptors.
	 *
	 * @return the long
	 */
	public long openFileDescriptors() {
		return this.openFileDescriptors;
	}

	
	/**
	 * Gets the open file descriptors.
	 *
	 * @return the open file descriptors
	 */
	public long getOpenFileDescriptors() {
		return openFileDescriptors;
	}

	
	/**
	 * Cpu.
	 *
	 * @return the cpu
	 */
	public Cpu cpu() {
		return cpu;
	}

	
	/**
	 * Gets the cpu.
	 *
	 * @return the cpu
	 */
	public Cpu getCpu() {
		return cpu();
	}

	
	/**
	 * Mem.
	 *
	 * @return the mem
	 */
	public Mem mem() {
		return mem;
	}

	
	/**
	 * Gets the mem.
	 *
	 * @return the mem
	 */
	public Mem getMem() {
		return mem();
	}

	
	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		
		/** The Constant PROCESS. */
		static final XContentBuilderString PROCESS = new XContentBuilderString("process");

		
		/** The Constant TIMESTAMP. */
		static final XContentBuilderString TIMESTAMP = new XContentBuilderString("timestamp");

		
		/** The Constant OPEN_FILE_DESCRIPTORS. */
		static final XContentBuilderString OPEN_FILE_DESCRIPTORS = new XContentBuilderString("open_file_descriptors");

		
		/** The Constant CPU. */
		static final XContentBuilderString CPU = new XContentBuilderString("cpu");

		
		/** The Constant PERCENT. */
		static final XContentBuilderString PERCENT = new XContentBuilderString("percent");

		
		/** The Constant SYS. */
		static final XContentBuilderString SYS = new XContentBuilderString("sys");

		
		/** The Constant SYS_IN_MILLIS. */
		static final XContentBuilderString SYS_IN_MILLIS = new XContentBuilderString("sys_in_millis");

		
		/** The Constant USER. */
		static final XContentBuilderString USER = new XContentBuilderString("user");

		
		/** The Constant USER_IN_MILLIS. */
		static final XContentBuilderString USER_IN_MILLIS = new XContentBuilderString("user_in_millis");

		
		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		
		/** The Constant TOTAL_IN_MILLIS. */
		static final XContentBuilderString TOTAL_IN_MILLIS = new XContentBuilderString("total_in_millis");

		
		/** The Constant MEM. */
		static final XContentBuilderString MEM = new XContentBuilderString("mem");

		
		/** The Constant RESIDENT. */
		static final XContentBuilderString RESIDENT = new XContentBuilderString("resident");

		
		/** The Constant RESIDENT_IN_BYTES. */
		static final XContentBuilderString RESIDENT_IN_BYTES = new XContentBuilderString("resident_in_bytes");

		
		/** The Constant SHARE. */
		static final XContentBuilderString SHARE = new XContentBuilderString("share");

		
		/** The Constant SHARE_IN_BYTES. */
		static final XContentBuilderString SHARE_IN_BYTES = new XContentBuilderString("share_in_bytes");

		
		/** The Constant TOTAL_VIRTUAL. */
		static final XContentBuilderString TOTAL_VIRTUAL = new XContentBuilderString("total_virtual");

		
		/** The Constant TOTAL_VIRTUAL_IN_BYTES. */
		static final XContentBuilderString TOTAL_VIRTUAL_IN_BYTES = new XContentBuilderString("total_virtual_in_bytes");
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.PROCESS);
		builder.field(Fields.TIMESTAMP, timestamp);
		builder.field(Fields.OPEN_FILE_DESCRIPTORS, openFileDescriptors);
		if (cpu != null) {
			builder.startObject(Fields.CPU);
			builder.field(Fields.PERCENT, cpu.percent());
			builder.field(Fields.SYS, cpu.sys().format());
			builder.field(Fields.SYS_IN_MILLIS, cpu.sys().millis());
			builder.field(Fields.USER, cpu.user().format());
			builder.field(Fields.USER_IN_MILLIS, cpu.user().millis());
			builder.field(Fields.TOTAL, cpu.total().format());
			builder.field(Fields.TOTAL_IN_MILLIS, cpu.total().millis());
			builder.endObject();
		}
		if (mem != null) {
			builder.startObject(Fields.MEM);
			builder.field(Fields.RESIDENT, mem.resident().toString());
			builder.field(Fields.RESIDENT_IN_BYTES, mem.resident().bytes());
			builder.field(Fields.SHARE, mem.share().toString());
			builder.field(Fields.SHARE_IN_BYTES, mem.share().bytes());
			builder.field(Fields.TOTAL_VIRTUAL, mem.totalVirtual().toString());
			builder.field(Fields.TOTAL_VIRTUAL_IN_BYTES, mem.totalVirtual().bytes());
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}

	
	/**
	 * Read process stats.
	 *
	 * @param in the in
	 * @return the process stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ProcessStats readProcessStats(StreamInput in) throws IOException {
		ProcessStats stats = new ProcessStats();
		stats.readFrom(in);
		return stats;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		timestamp = in.readVLong();
		openFileDescriptors = in.readLong();
		if (in.readBoolean()) {
			cpu = Cpu.readCpu(in);
		}
		if (in.readBoolean()) {
			mem = Mem.readMem(in);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(timestamp);
		out.writeLong(openFileDescriptors);
		if (cpu == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			cpu.writeTo(out);
		}
		if (mem == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			mem.writeTo(out);
		}
	}

	
	/**
	 * The Class Mem.
	 *
	 * @author l.xue.nong
	 */
	public static class Mem implements Streamable, Serializable {

		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 6436300006737234567L;

		
		/** The total virtual. */
		long totalVirtual = -1;

		
		/** The resident. */
		long resident = -1;

		
		/** The share. */
		long share = -1;

		
		/**
		 * Instantiates a new mem.
		 */
		Mem() {
		}

		
		/**
		 * Read mem.
		 *
		 * @param in the in
		 * @return the mem
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static Mem readMem(StreamInput in) throws IOException {
			Mem mem = new Mem();
			mem.readFrom(in);
			return mem;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			totalVirtual = in.readLong();
			resident = in.readLong();
			share = in.readLong();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeLong(totalVirtual);
			out.writeLong(resident);
			out.writeLong(share);
		}

		
		/**
		 * Total virtual.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue totalVirtual() {
			return new ByteSizeValue(totalVirtual);
		}

		
		/**
		 * Gets the total virtual.
		 *
		 * @return the total virtual
		 */
		public ByteSizeValue getTotalVirtual() {
			return totalVirtual();
		}

		
		/**
		 * Resident.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue resident() {
			return new ByteSizeValue(resident);
		}

		
		/**
		 * Gets the resident.
		 *
		 * @return the resident
		 */
		public ByteSizeValue getResident() {
			return resident();
		}

		
		/**
		 * Share.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue share() {
			return new ByteSizeValue(share);
		}

		
		/**
		 * Gets the share.
		 *
		 * @return the share
		 */
		public ByteSizeValue getShare() {
			return share();
		}
	}

	
	/**
	 * The Class Cpu.
	 *
	 * @author l.xue.nong
	 */
	public static class Cpu implements Streamable, Serializable {

		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1443613038752835426L;

		
		/** The percent. */
		short percent = -1;

		
		/** The sys. */
		long sys = -1;

		
		/** The user. */
		long user = -1;

		
		/** The total. */
		long total = -1;

		
		/**
		 * Instantiates a new cpu.
		 */
		Cpu() {

		}

		
		/**
		 * Read cpu.
		 *
		 * @param in the in
		 * @return the cpu
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static Cpu readCpu(StreamInput in) throws IOException {
			Cpu cpu = new Cpu();
			cpu.readFrom(in);
			return cpu;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			percent = in.readShort();
			sys = in.readLong();
			user = in.readLong();
			total = in.readLong();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeShort(percent);
			out.writeLong(sys);
			out.writeLong(user);
			out.writeLong(total);
		}

		
		/**
		 * Percent.
		 *
		 * @return the short
		 */
		public short percent() {
			return percent;
		}

		
		/**
		 * Gets the percent.
		 *
		 * @return the percent
		 */
		public short getPercent() {
			return percent();
		}

		
		/**
		 * Sys.
		 *
		 * @return the time value
		 */
		public TimeValue sys() {
			return new TimeValue(sys);
		}

		
		/**
		 * Gets the sys.
		 *
		 * @return the sys
		 */
		public TimeValue getSys() {
			return sys();
		}

		
		/**
		 * User.
		 *
		 * @return the time value
		 */
		public TimeValue user() {
			return new TimeValue(user);
		}

		
		/**
		 * Total.
		 *
		 * @return the time value
		 */
		public TimeValue total() {
			return new TimeValue(total);
		}

		
		/**
		 * Gets the total.
		 *
		 * @return the total
		 */
		public TimeValue getTotal() {
			return total();
		}

		
		/**
		 * Gets the user.
		 *
		 * @return the user
		 */
		public TimeValue getUser() {
			return user();
		}

	}
}
