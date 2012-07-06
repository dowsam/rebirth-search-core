/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OsStats.java 2012-7-6 14:30:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.os;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

/**
 * The Class OsStats.
 *
 * @author l.xue.nong
 */
public class OsStats implements Streamable, Serializable, ToXContent {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6653101600073480228L;

	/** The Constant EMPTY_LOAD. */
	public static final double[] EMPTY_LOAD = new double[0];

	/** The timestamp. */
	long timestamp;

	/** The load average. */
	double[] loadAverage = EMPTY_LOAD;

	/** The uptime. */
	long uptime = -1;

	/** The cpu. */
	Cpu cpu = null;

	/** The mem. */
	Mem mem = null;

	/** The swap. */
	Swap swap = null;

	/**
	 * Instantiates a new os stats.
	 */
	OsStats() {
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

	/**
	 * Load average.
	 *
	 * @return the double[]
	 */
	public double[] loadAverage() {
		return loadAverage;
	}

	/**
	 * Gets the load average.
	 *
	 * @return the load average
	 */
	public double[] getLoadAverage() {
		return loadAverage();
	}

	/**
	 * Uptime.
	 *
	 * @return the time value
	 */
	public TimeValue uptime() {
		return new TimeValue(uptime, TimeUnit.SECONDS);
	}

	/**
	 * Gets the uptime.
	 *
	 * @return the uptime
	 */
	public TimeValue getUptime() {
		return uptime();
	}

	/**
	 * Cpu.
	 *
	 * @return the cpu
	 */
	public Cpu cpu() {
		return this.cpu;
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
		return this.mem;
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
	 * Swap.
	 *
	 * @return the swap
	 */
	public Swap swap() {
		return this.swap;
	}

	/**
	 * Gets the swap.
	 *
	 * @return the swap
	 */
	public Swap getSwap() {
		return swap();
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant OS. */
		static final XContentBuilderString OS = new XContentBuilderString("os");

		/** The Constant TIMESTAMP. */
		static final XContentBuilderString TIMESTAMP = new XContentBuilderString("timestamp");

		/** The Constant UPTIME. */
		static final XContentBuilderString UPTIME = new XContentBuilderString("uptime");

		/** The Constant UPTIME_IN_MILLIS. */
		static final XContentBuilderString UPTIME_IN_MILLIS = new XContentBuilderString("uptime_in_millis");

		/** The Constant LOAD_AVERAGE. */
		static final XContentBuilderString LOAD_AVERAGE = new XContentBuilderString("load_average");

		/** The Constant CPU. */
		static final XContentBuilderString CPU = new XContentBuilderString("cpu");

		/** The Constant SYS. */
		static final XContentBuilderString SYS = new XContentBuilderString("sys");

		/** The Constant USER. */
		static final XContentBuilderString USER = new XContentBuilderString("user");

		/** The Constant IDLE. */
		static final XContentBuilderString IDLE = new XContentBuilderString("idle");

		/** The Constant MEM. */
		static final XContentBuilderString MEM = new XContentBuilderString("mem");

		/** The Constant SWAP. */
		static final XContentBuilderString SWAP = new XContentBuilderString("swap");

		/** The Constant FREE. */
		static final XContentBuilderString FREE = new XContentBuilderString("free");

		/** The Constant FREE_IN_BYTES. */
		static final XContentBuilderString FREE_IN_BYTES = new XContentBuilderString("free_in_bytes");

		/** The Constant USED. */
		static final XContentBuilderString USED = new XContentBuilderString("used");

		/** The Constant USED_IN_BYTES. */
		static final XContentBuilderString USED_IN_BYTES = new XContentBuilderString("used_in_bytes");

		/** The Constant FREE_PERCENT. */
		static final XContentBuilderString FREE_PERCENT = new XContentBuilderString("free_percent");

		/** The Constant USED_PERCENT. */
		static final XContentBuilderString USED_PERCENT = new XContentBuilderString("used_percent");

		/** The Constant ACTUAL_FREE. */
		static final XContentBuilderString ACTUAL_FREE = new XContentBuilderString("actual_free");

		/** The Constant ACTUAL_FREE_IN_BYTES. */
		static final XContentBuilderString ACTUAL_FREE_IN_BYTES = new XContentBuilderString("actual_free_in_bytes");

		/** The Constant ACTUAL_USED. */
		static final XContentBuilderString ACTUAL_USED = new XContentBuilderString("actual_used");

		/** The Constant ACTUAL_USED_IN_BYTES. */
		static final XContentBuilderString ACTUAL_USED_IN_BYTES = new XContentBuilderString("actual_used_in_bytes");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.OS);
		builder.field(Fields.TIMESTAMP, timestamp);

		builder.field(Fields.UPTIME, uptime().format());
		builder.field(Fields.UPTIME_IN_MILLIS, uptime().millis());

		builder.startArray(Fields.LOAD_AVERAGE);
		for (double value : loadAverage) {
			builder.value(value);
		}
		builder.endArray();

		if (cpu != null) {
			builder.startObject(Fields.CPU);
			builder.field(Fields.SYS, cpu.sys());
			builder.field(Fields.USER, cpu.user());
			builder.field(Fields.IDLE, cpu.idle());
			builder.endObject();
		}

		if (mem != null) {
			builder.startObject(Fields.MEM);
			builder.field(Fields.FREE, mem.free().toString());
			builder.field(Fields.FREE_IN_BYTES, mem.free);
			builder.field(Fields.USED, mem.used().toString());
			builder.field(Fields.USED_IN_BYTES, mem.used);

			builder.field(Fields.FREE_PERCENT, mem.freePercent());
			builder.field(Fields.USED_PERCENT, mem.usedPercent());

			builder.field(Fields.ACTUAL_FREE, mem.actualFree().toString());
			builder.field(Fields.ACTUAL_FREE_IN_BYTES, mem.actualFree);
			builder.field(Fields.ACTUAL_USED, mem.actualUsed().toString());
			builder.field(Fields.ACTUAL_USED_IN_BYTES, mem.actualUsed);

			builder.endObject();
		}

		if (swap != null) {
			builder.startObject(Fields.SWAP);
			builder.field(Fields.USED, swap.used().toString());
			builder.field(Fields.USED_IN_BYTES, swap.used);
			builder.field(Fields.FREE, swap.free().toString());
			builder.field(Fields.FREE_IN_BYTES, swap.free);
			builder.endObject();
		}

		builder.endObject();
		return builder;
	}

	/**
	 * Read os stats.
	 *
	 * @param in the in
	 * @return the os stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static OsStats readOsStats(StreamInput in) throws IOException {
		OsStats stats = new OsStats();
		stats.readFrom(in);
		return stats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		timestamp = in.readVLong();
		loadAverage = new double[in.readVInt()];
		for (int i = 0; i < loadAverage.length; i++) {
			loadAverage[i] = in.readDouble();
		}
		uptime = in.readLong();
		if (in.readBoolean()) {
			cpu = Cpu.readCpu(in);
		}
		if (in.readBoolean()) {
			mem = Mem.readMem(in);
		}
		if (in.readBoolean()) {
			swap = Swap.readSwap(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(timestamp);
		out.writeVInt(loadAverage.length);
		for (double val : loadAverage) {
			out.writeDouble(val);
		}
		out.writeLong(uptime);
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
		if (swap == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			swap.writeTo(out);
		}
	}

	/**
	 * The Class Swap.
	 *
	 * @author l.xue.nong
	 */
	public static class Swap implements Streamable, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -9199325143356617048L;

		/** The free. */
		long free = -1;

		/** The used. */
		long used = -1;

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
		 * Used.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue used() {
			return new ByteSizeValue(used);
		}

		/**
		 * Gets the used.
		 *
		 * @return the used
		 */
		public ByteSizeValue getUsed() {
			return used();
		}

		/**
		 * Read swap.
		 *
		 * @param in the in
		 * @return the swap
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static Swap readSwap(StreamInput in) throws IOException {
			Swap swap = new Swap();
			swap.readFrom(in);
			return swap;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			free = in.readLong();
			used = in.readLong();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeLong(free);
			out.writeLong(used);
		}
	}

	/**
	 * The Class Mem.
	 *
	 * @author l.xue.nong
	 */
	public static class Mem implements Streamable, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 6930634619203404191L;

		/** The free. */
		long free = -1;

		/** The free percent. */
		short freePercent = -1;

		/** The used. */
		long used = -1;

		/** The used percent. */
		short usedPercent = -1;

		/** The actual free. */
		long actualFree = -1;

		/** The actual used. */
		long actualUsed = -1;

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
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			free = in.readLong();
			freePercent = in.readShort();
			used = in.readLong();
			usedPercent = in.readShort();
			actualFree = in.readLong();
			actualUsed = in.readLong();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeLong(free);
			out.writeShort(freePercent);
			out.writeLong(used);
			out.writeShort(usedPercent);
			out.writeLong(actualFree);
			out.writeLong(actualUsed);
		}

		/**
		 * Used.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue used() {
			return new ByteSizeValue(used);
		}

		/**
		 * Gets the used.
		 *
		 * @return the used
		 */
		public ByteSizeValue getUsed() {
			return used();
		}

		/**
		 * Used percent.
		 *
		 * @return the short
		 */
		public short usedPercent() {
			return usedPercent;
		}

		/**
		 * Gets the used percent.
		 *
		 * @return the used percent
		 */
		public short getUsedPercent() {
			return usedPercent();
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
		 * Free percent.
		 *
		 * @return the short
		 */
		public short freePercent() {
			return freePercent;
		}

		/**
		 * Gets the free percent.
		 *
		 * @return the free percent
		 */
		public short getFreePercent() {
			return freePercent();
		}

		/**
		 * Actual free.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue actualFree() {
			return new ByteSizeValue(actualFree);
		}

		/**
		 * Gets the actual free.
		 *
		 * @return the actual free
		 */
		public ByteSizeValue getActualFree() {
			return actualFree();
		}

		/**
		 * Actual used.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue actualUsed() {
			return new ByteSizeValue(actualUsed);
		}

		/**
		 * Gets the actual used.
		 *
		 * @return the actual used
		 */
		public ByteSizeValue getActualUsed() {
			return actualUsed();
		}
	}

	/**
	 * The Class Cpu.
	 *
	 * @author l.xue.nong
	 */
	public static class Cpu implements Streamable, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -4302262661219625799L;

		/** The sys. */
		short sys = -1;

		/** The user. */
		short user = -1;

		/** The idle. */
		short idle = -1;

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
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			sys = in.readShort();
			user = in.readShort();
			idle = in.readShort();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeShort(sys);
			out.writeShort(user);
			out.writeShort(idle);
		}

		/**
		 * Sys.
		 *
		 * @return the short
		 */
		public short sys() {
			return sys;
		}

		/**
		 * Gets the sys.
		 *
		 * @return the sys
		 */
		public short getSys() {
			return sys();
		}

		/**
		 * User.
		 *
		 * @return the short
		 */
		public short user() {
			return user;
		}

		/**
		 * Gets the user.
		 *
		 * @return the user
		 */
		public short getUser() {
			return user();
		}

		/**
		 * Idle.
		 *
		 * @return the short
		 */
		public short idle() {
			return idle;
		}

		/**
		 * Gets the idle.
		 *
		 * @return the idle
		 */
		public short getIdle() {
			return idle();
		}
	}
}
