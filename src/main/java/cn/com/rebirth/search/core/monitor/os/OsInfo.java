/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OsInfo.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.os;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

/**
 * The Class OsInfo.
 *
 * @author l.xue.nong
 */
public class OsInfo implements Streamable, Serializable, ToXContent {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 273016926648489348L;

	/** The refresh interval. */
	long refreshInterval;

	/** The cpu. */
	Cpu cpu = null;

	/** The mem. */
	Mem mem = null;

	/** The swap. */
	Swap swap = null;

	/**
	 * Instantiates a new os info.
	 */
	OsInfo() {
	}

	/**
	 * Refresh interval.
	 *
	 * @return the long
	 */
	public long refreshInterval() {
		return this.refreshInterval;
	}

	/**
	 * Gets the refresh interval.
	 *
	 * @return the refresh interval
	 */
	public long getRefreshInterval() {
		return this.refreshInterval;
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

		/** The Constant REFRESH_INTERVAL. */
		static final XContentBuilderString REFRESH_INTERVAL = new XContentBuilderString("refresh_interval");

		/** The Constant CPU. */
		static final XContentBuilderString CPU = new XContentBuilderString("cpu");

		/** The Constant VENDOR. */
		static final XContentBuilderString VENDOR = new XContentBuilderString("vendor");

		/** The Constant MODEL. */
		static final XContentBuilderString MODEL = new XContentBuilderString("model");

		/** The Constant MHZ. */
		static final XContentBuilderString MHZ = new XContentBuilderString("mhz");

		/** The Constant TOTAL_CORES. */
		static final XContentBuilderString TOTAL_CORES = new XContentBuilderString("total_cores");

		/** The Constant TOTAL_SOCKETS. */
		static final XContentBuilderString TOTAL_SOCKETS = new XContentBuilderString("total_sockets");

		/** The Constant CORES_PER_SOCKET. */
		static final XContentBuilderString CORES_PER_SOCKET = new XContentBuilderString("cores_per_socket");

		/** The Constant CACHE_SIZE. */
		static final XContentBuilderString CACHE_SIZE = new XContentBuilderString("cache_size");

		/** The Constant CACHE_SIZE_IN_BYTES. */
		static final XContentBuilderString CACHE_SIZE_IN_BYTES = new XContentBuilderString("cache_size_in_bytes");

		/** The Constant MEM. */
		static final XContentBuilderString MEM = new XContentBuilderString("mem");

		/** The Constant SWAP. */
		static final XContentBuilderString SWAP = new XContentBuilderString("swap");

		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		/** The Constant TOTAL_IN_BYTES. */
		static final XContentBuilderString TOTAL_IN_BYTES = new XContentBuilderString("total_in_bytes");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.OS);
		builder.field(Fields.REFRESH_INTERVAL, refreshInterval);
		if (cpu != null) {
			builder.startObject(Fields.CPU);
			builder.field(Fields.VENDOR, cpu.vendor());
			builder.field(Fields.MODEL, cpu.model());
			builder.field(Fields.MHZ, cpu.mhz());
			builder.field(Fields.TOTAL_CORES, cpu.totalCores());
			builder.field(Fields.TOTAL_SOCKETS, cpu.totalSockets());
			builder.field(Fields.CORES_PER_SOCKET, cpu.coresPerSocket());
			builder.field(Fields.CACHE_SIZE, cpu.cacheSize().toString());
			builder.field(Fields.CACHE_SIZE_IN_BYTES, cpu.cacheSize().bytes());
			builder.endObject();
		}
		if (mem != null) {
			builder.startObject(Fields.MEM);
			builder.field(Fields.TOTAL, mem.total().toString());
			builder.field(Fields.TOTAL_IN_BYTES, mem.total);
			builder.endObject();
		}
		if (swap != null) {
			builder.startObject(Fields.SWAP);
			builder.field(Fields.TOTAL, swap.total().toString());
			builder.field(Fields.TOTAL_IN_BYTES, swap.total);
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}

	/**
	 * Read os info.
	 *
	 * @param in the in
	 * @return the os info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static OsInfo readOsInfo(StreamInput in) throws IOException {
		OsInfo info = new OsInfo();
		info.readFrom(in);
		return info;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		refreshInterval = in.readLong();
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
		out.writeLong(refreshInterval);
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
		private static final long serialVersionUID = 4870147119872185998L;

		/** The total. */
		long total = -1;

		/**
		 * Instantiates a new swap.
		 */
		Swap() {

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
			total = in.readLong();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeLong(total);
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

	}

	/**
	 * The Class Mem.
	 *
	 * @author l.xue.nong
	 */
	public static class Mem implements Streamable, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -3874356343722647475L;

		/** The total. */
		long total = -1;

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
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			total = in.readLong();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeLong(total);
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

	}

	/**
	 * The Class Cpu.
	 *
	 * @author l.xue.nong
	 */
	public static class Cpu implements Streamable, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -5122209251662317660L;

		/** The vendor. */
		String vendor = "";

		/** The model. */
		String model = "";

		/** The mhz. */
		int mhz = -1;

		/** The total cores. */
		int totalCores = -1;

		/** The total sockets. */
		int totalSockets = -1;

		/** The cores per socket. */
		int coresPerSocket = -1;

		/** The cache size. */
		long cacheSize = -1;

		/**
		 * Instantiates a new cpu.
		 */
		Cpu() {

		}

		/**
		 * Vendor.
		 *
		 * @return the string
		 */
		public String vendor() {
			return this.vendor;
		}

		/**
		 * Gets the vendor.
		 *
		 * @return the vendor
		 */
		public String getVendor() {
			return vendor();
		}

		/**
		 * Model.
		 *
		 * @return the string
		 */
		public String model() {
			return model;
		}

		/**
		 * Gets the model.
		 *
		 * @return the model
		 */
		public String getModel() {
			return model;
		}

		/**
		 * Mhz.
		 *
		 * @return the int
		 */
		public int mhz() {
			return mhz;
		}

		/**
		 * Gets the mhz.
		 *
		 * @return the mhz
		 */
		public int getMhz() {
			return mhz;
		}

		/**
		 * Total cores.
		 *
		 * @return the int
		 */
		public int totalCores() {
			return totalCores;
		}

		/**
		 * Gets the total cores.
		 *
		 * @return the total cores
		 */
		public int getTotalCores() {
			return totalCores();
		}

		/**
		 * Total sockets.
		 *
		 * @return the int
		 */
		public int totalSockets() {
			return totalSockets;
		}

		/**
		 * Gets the total sockets.
		 *
		 * @return the total sockets
		 */
		public int getTotalSockets() {
			return totalSockets();
		}

		/**
		 * Cores per socket.
		 *
		 * @return the int
		 */
		public int coresPerSocket() {
			return coresPerSocket;
		}

		/**
		 * Gets the cores per socket.
		 *
		 * @return the cores per socket
		 */
		public int getCoresPerSocket() {
			return coresPerSocket();
		}

		/**
		 * Cache size.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue cacheSize() {
			return new ByteSizeValue(cacheSize);
		}

		/**
		 * Gets the cache size.
		 *
		 * @return the cache size
		 */
		public ByteSizeValue getCacheSize() {
			return cacheSize();
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
			vendor = in.readUTF();
			model = in.readUTF();
			mhz = in.readInt();
			totalCores = in.readInt();
			totalSockets = in.readInt();
			coresPerSocket = in.readInt();
			cacheSize = in.readLong();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(vendor);
			out.writeUTF(model);
			out.writeInt(mhz);
			out.writeInt(totalCores);
			out.writeInt(totalSockets);
			out.writeInt(coresPerSocket);
			out.writeLong(cacheSize);
		}
	}
}
