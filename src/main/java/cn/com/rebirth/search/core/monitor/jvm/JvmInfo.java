/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core JvmInfo.java 2012-3-29 15:02:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.jvm;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;


/**
 * The Class JvmInfo.
 *
 * @author l.xue.nong
 */
public class JvmInfo implements Streamable, Serializable, ToXContent {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6414742427948268466L;

	
	/** The INSTANCE. */
	private static JvmInfo INSTANCE;

	static {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

		
		long pid;
		String xPid = runtimeMXBean.getName();
		try {
			xPid = xPid.split("@")[0];
			pid = Long.parseLong(xPid);
		} catch (Exception e) {
			pid = -1;
		}
		JvmInfo info = new JvmInfo();
		info.pid = pid;
		info.startTime = runtimeMXBean.getStartTime();
		info.version = runtimeMXBean.getSystemProperties().get("java.version");
		info.vmName = runtimeMXBean.getVmName();
		info.vmVendor = runtimeMXBean.getVmVendor();
		info.vmVersion = runtimeMXBean.getVmVersion();
		info.mem = new Mem();
		info.mem.heapInit = memoryMXBean.getHeapMemoryUsage().getInit() < 0 ? 0 : memoryMXBean.getHeapMemoryUsage()
				.getInit();
		info.mem.heapMax = memoryMXBean.getHeapMemoryUsage().getMax() < 0 ? 0 : memoryMXBean.getHeapMemoryUsage()
				.getMax();
		info.mem.nonHeapInit = memoryMXBean.getNonHeapMemoryUsage().getInit() < 0 ? 0 : memoryMXBean
				.getNonHeapMemoryUsage().getInit();
		info.mem.nonHeapMax = memoryMXBean.getNonHeapMemoryUsage().getMax() < 0 ? 0 : memoryMXBean
				.getNonHeapMemoryUsage().getMax();
		info.inputArguments = runtimeMXBean.getInputArguments().toArray(
				new String[runtimeMXBean.getInputArguments().size()]);
		info.bootClassPath = runtimeMXBean.getBootClassPath();
		info.classPath = runtimeMXBean.getClassPath();
		info.systemProperties = runtimeMXBean.getSystemProperties();

		INSTANCE = info;
	}

	
	/**
	 * Jvm info.
	 *
	 * @return the jvm info
	 */
	public static JvmInfo jvmInfo() {
		return INSTANCE;
	}

	
	/** The pid. */
	long pid = -1;

	
	/** The version. */
	String version = "";

	
	/** The vm name. */
	String vmName = "";

	
	/** The vm version. */
	String vmVersion = "";

	
	/** The vm vendor. */
	String vmVendor = "";

	
	/** The start time. */
	long startTime = -1;

	
	/** The mem. */
	Mem mem;

	
	/** The input arguments. */
	String[] inputArguments;

	
	/** The boot class path. */
	String bootClassPath;

	
	/** The class path. */
	String classPath;

	
	/** The system properties. */
	Map<String, String> systemProperties;

	
	/**
	 * Instantiates a new jvm info.
	 */
	private JvmInfo() {
	}

	
	/**
	 * Pid.
	 *
	 * @return the long
	 */
	public long pid() {
		return this.pid;
	}

	
	/**
	 * Gets the pid.
	 *
	 * @return the pid
	 */
	public long getPid() {
		return pid;
	}

	
	/**
	 * Version.
	 *
	 * @return the string
	 */
	public String version() {
		return this.version;
	}

	
	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return this.version;
	}

	
	/**
	 * Version as integer.
	 *
	 * @return the int
	 */
	public int versionAsInteger() {
		try {
			int i = 0;
			String sVersion = "";
			for (; i < version.length(); i++) {
				if (!Character.isDigit(version.charAt(i)) && version.charAt(i) != '.') {
					break;
				}
				if (version.charAt(i) != '.') {
					sVersion += version.charAt(i);
				}
			}
			if (i == 0) {
				return -1;
			}
			return Integer.parseInt(sVersion);
		} catch (Exception e) {
			return -1;
		}
	}

	
	/**
	 * Version update pack.
	 *
	 * @return the int
	 */
	public int versionUpdatePack() {
		try {
			int i = 0;
			String sVersion = "";
			for (; i < version.length(); i++) {
				if (!Character.isDigit(version.charAt(i)) && version.charAt(i) != '.') {
					break;
				}
				if (version.charAt(i) != '.') {
					sVersion += version.charAt(i);
				}
			}
			if (i == 0) {
				return -1;
			}
			Integer.parseInt(sVersion);
			int from;
			if (version.charAt(i) == '_') {
				
				from = ++i;
			} else if (version.charAt(i) == '-' && version.charAt(i + 1) == 'u') {
				
				i = i + 2;
				from = i;
			} else {
				return -1;
			}
			for (; i < version.length(); i++) {
				if (!Character.isDigit(version.charAt(i)) && version.charAt(i) != '.') {
					break;
				}
			}
			if (from == i) {
				return -1;
			}
			return Integer.parseInt(version.substring(from, i));
		} catch (Exception e) {
			return -1;
		}
	}

	
	/**
	 * Vm name.
	 *
	 * @return the string
	 */
	public String vmName() {
		return vmName;
	}

	
	/**
	 * Gets the vm name.
	 *
	 * @return the vm name
	 */
	public String getVmName() {
		return vmName;
	}

	
	/**
	 * Vm version.
	 *
	 * @return the string
	 */
	public String vmVersion() {
		return vmVersion;
	}

	
	/**
	 * Gets the vm version.
	 *
	 * @return the vm version
	 */
	public String getVmVersion() {
		return vmVersion;
	}

	
	/**
	 * Vm vendor.
	 *
	 * @return the string
	 */
	public String vmVendor() {
		return vmVendor;
	}

	
	/**
	 * Gets the vm vendor.
	 *
	 * @return the vm vendor
	 */
	public String getVmVendor() {
		return vmVendor;
	}

	
	/**
	 * Start time.
	 *
	 * @return the long
	 */
	public long startTime() {
		return startTime;
	}

	
	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public long getStartTime() {
		return startTime;
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
	 * Input arguments.
	 *
	 * @return the string[]
	 */
	public String[] inputArguments() {
		return inputArguments;
	}

	
	/**
	 * Gets the input arguments.
	 *
	 * @return the input arguments
	 */
	public String[] getInputArguments() {
		return inputArguments;
	}

	
	/**
	 * Boot class path.
	 *
	 * @return the string
	 */
	public String bootClassPath() {
		return bootClassPath;
	}

	
	/**
	 * Gets the boot class path.
	 *
	 * @return the boot class path
	 */
	public String getBootClassPath() {
		return bootClassPath;
	}

	
	/**
	 * Class path.
	 *
	 * @return the string
	 */
	public String classPath() {
		return classPath;
	}

	
	/**
	 * Gets the class path.
	 *
	 * @return the class path
	 */
	public String getClassPath() {
		return classPath;
	}

	
	/**
	 * System properties.
	 *
	 * @return the map
	 */
	public Map<String, String> systemProperties() {
		return systemProperties;
	}

	
	/**
	 * Gets the system properties.
	 *
	 * @return the system properties
	 */
	public Map<String, String> getSystemProperties() {
		return systemProperties;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.JVM);
		builder.field(Fields.PID, pid);
		builder.field(Fields.VERSION, version);
		builder.field(Fields.VM_NAME, vmName);
		builder.field(Fields.VM_VERSION, vmVersion);
		builder.field(Fields.VM_VENDOR, vmVendor);
		builder.field(Fields.START_TIME, startTime);

		builder.startObject(Fields.MEM);
		builder.field(Fields.HEAP_INIT, mem.heapInit().toString());
		builder.field(Fields.HEAP_INIT_IN_BYTES, mem.heapInit);
		builder.field(Fields.HEAP_MAX, mem.heapMax().toString());
		builder.field(Fields.HEAP_MAX_IN_BYTES, mem.heapMax);
		builder.field(Fields.NON_HEAP_INIT, mem.nonHeapInit().toString());
		builder.field(Fields.NON_HEAP_INIT_IN_BYTES, mem.nonHeapInit);
		builder.field(Fields.NON_HEAP_MAX, mem.nonHeapMax().toString());
		builder.field(Fields.NON_HEAP_MAX_IN_BYTES, mem.nonHeapMax);
		builder.endObject();

		builder.endObject();
		return builder;
	}

	
	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		
		/** The Constant JVM. */
		static final XContentBuilderString JVM = new XContentBuilderString("jvm");

		
		/** The Constant PID. */
		static final XContentBuilderString PID = new XContentBuilderString("pid");

		
		/** The Constant VERSION. */
		static final XContentBuilderString VERSION = new XContentBuilderString("version");

		
		/** The Constant VM_NAME. */
		static final XContentBuilderString VM_NAME = new XContentBuilderString("vm_name");

		
		/** The Constant VM_VERSION. */
		static final XContentBuilderString VM_VERSION = new XContentBuilderString("vm_version");

		
		/** The Constant VM_VENDOR. */
		static final XContentBuilderString VM_VENDOR = new XContentBuilderString("vm_vendor");

		
		/** The Constant START_TIME. */
		static final XContentBuilderString START_TIME = new XContentBuilderString("start_time");

		
		/** The Constant MEM. */
		static final XContentBuilderString MEM = new XContentBuilderString("mem");

		
		/** The Constant HEAP_INIT. */
		static final XContentBuilderString HEAP_INIT = new XContentBuilderString("heap_init");

		
		/** The Constant HEAP_INIT_IN_BYTES. */
		static final XContentBuilderString HEAP_INIT_IN_BYTES = new XContentBuilderString("heap_init_in_bytes");

		
		/** The Constant HEAP_MAX. */
		static final XContentBuilderString HEAP_MAX = new XContentBuilderString("heap_max");

		
		/** The Constant HEAP_MAX_IN_BYTES. */
		static final XContentBuilderString HEAP_MAX_IN_BYTES = new XContentBuilderString("heap_max_in_bytes");

		
		/** The Constant NON_HEAP_INIT. */
		static final XContentBuilderString NON_HEAP_INIT = new XContentBuilderString("non_heap_init");

		
		/** The Constant NON_HEAP_INIT_IN_BYTES. */
		static final XContentBuilderString NON_HEAP_INIT_IN_BYTES = new XContentBuilderString("non_heap_init_in_bytes");

		
		/** The Constant NON_HEAP_MAX. */
		static final XContentBuilderString NON_HEAP_MAX = new XContentBuilderString("non_heap_max");

		
		/** The Constant NON_HEAP_MAX_IN_BYTES. */
		static final XContentBuilderString NON_HEAP_MAX_IN_BYTES = new XContentBuilderString("non_heap_max_in_bytes");
	}

	
	/**
	 * Read jvm info.
	 *
	 * @param in the in
	 * @return the jvm info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static JvmInfo readJvmInfo(StreamInput in) throws IOException {
		JvmInfo jvmInfo = new JvmInfo();
		jvmInfo.readFrom(in);
		return jvmInfo;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		pid = in.readLong();
		version = in.readUTF();
		vmName = in.readUTF();
		vmVersion = in.readUTF();
		vmVendor = in.readUTF();
		startTime = in.readLong();
		inputArguments = new String[in.readInt()];
		for (int i = 0; i < inputArguments.length; i++) {
			inputArguments[i] = in.readUTF();
		}
		bootClassPath = in.readUTF();
		classPath = in.readUTF();
		systemProperties = new HashMap<String, String>();
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			systemProperties.put(in.readUTF(), in.readUTF());
		}
		mem = new Mem();
		mem.readFrom(in);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(pid);
		out.writeUTF(version);
		out.writeUTF(vmName);
		out.writeUTF(vmVersion);
		out.writeUTF(vmVendor);
		out.writeLong(startTime);
		out.writeInt(inputArguments.length);
		for (String inputArgument : inputArguments) {
			out.writeUTF(inputArgument);
		}
		out.writeUTF(bootClassPath);
		out.writeUTF(classPath);
		out.writeInt(systemProperties.size());
		for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeUTF(entry.getValue());
		}
		mem.writeTo(out);
	}

	
	/**
	 * The Class Mem.
	 *
	 * @author l.xue.nong
	 */
	public static class Mem implements Streamable, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 214089958902976888L;

		
		/** The heap init. */
		long heapInit = 0;

		
		/** The heap max. */
		long heapMax = 0;

		
		/** The non heap init. */
		long nonHeapInit = 0;

		
		/** The non heap max. */
		long nonHeapMax = 0;

		
		/**
		 * Instantiates a new mem.
		 */
		Mem() {
		}

		
		/**
		 * Heap init.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue heapInit() {
			return new ByteSizeValue(heapInit);
		}

		
		/**
		 * Gets the heap init.
		 *
		 * @return the heap init
		 */
		public ByteSizeValue getHeapInit() {
			return heapInit();
		}

		
		/**
		 * Heap max.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue heapMax() {
			return new ByteSizeValue(heapMax);
		}

		
		/**
		 * Gets the heap max.
		 *
		 * @return the heap max
		 */
		public ByteSizeValue getHeapMax() {
			return heapMax();
		}

		
		/**
		 * Non heap init.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue nonHeapInit() {
			return new ByteSizeValue(nonHeapInit);
		}

		
		/**
		 * Gets the non heap init.
		 *
		 * @return the non heap init
		 */
		public ByteSizeValue getNonHeapInit() {
			return nonHeapInit();
		}

		
		/**
		 * Non heap max.
		 *
		 * @return the byte size value
		 */
		public ByteSizeValue nonHeapMax() {
			return new ByteSizeValue(nonHeapMax);
		}

		
		/**
		 * Gets the non heap max.
		 *
		 * @return the non heap max
		 */
		public ByteSizeValue getNonHeapMax() {
			return nonHeapMax();
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
			heapInit = in.readVLong();
			heapMax = in.readVLong();
			nonHeapInit = in.readVLong();
			nonHeapMax = in.readVLong();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeVLong(heapInit);
			out.writeVLong(heapMax);
			out.writeVLong(nonHeapInit);
			out.writeVLong(nonHeapMax);
		}
	}
}
