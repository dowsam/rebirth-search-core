/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeStats.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.stats;

import java.io.IOException;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.core.monitor.fs.FsStats;
import cn.com.rebirth.core.monitor.jvm.JvmStats;
import cn.com.rebirth.core.monitor.network.NetworkStats;
import cn.com.rebirth.core.monitor.os.OsStats;
import cn.com.rebirth.core.monitor.process.ProcessStats;
import cn.com.rebirth.core.threadpool.ThreadPoolStats;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.http.HttpStats;
import cn.com.rebirth.search.core.indices.NodeIndicesStats;
import cn.com.rebirth.search.core.transport.TransportStats;

/**
 * The Class NodeStats.
 *
 * @author l.xue.nong
 */
public class NodeStats extends NodeOperationResponse {

	/** The hostname. */
	@Nullable
	private String hostname;

	/** The indices. */
	@Nullable
	private NodeIndicesStats indices;

	/** The os. */
	@Nullable
	private OsStats os;

	/** The process. */
	@Nullable
	private ProcessStats process;

	/** The jvm. */
	@Nullable
	private JvmStats jvm;

	/** The thread pool. */
	@Nullable
	private ThreadPoolStats threadPool;

	/** The network. */
	@Nullable
	private NetworkStats network;

	/** The fs. */
	@Nullable
	private FsStats fs;

	/** The transport. */
	@Nullable
	private TransportStats transport;

	/** The http. */
	@Nullable
	private HttpStats http;

	/**
	 * Instantiates a new node stats.
	 */
	NodeStats() {
	}

	/**
	 * Instantiates a new node stats.
	 *
	 * @param node the node
	 * @param hostname the hostname
	 * @param indices the indices
	 * @param os the os
	 * @param process the process
	 * @param jvm the jvm
	 * @param threadPool the thread pool
	 * @param network the network
	 * @param fs the fs
	 * @param transport the transport
	 * @param http the http
	 */
	public NodeStats(DiscoveryNode node, @Nullable String hostname, @Nullable NodeIndicesStats indices,
			@Nullable OsStats os, @Nullable ProcessStats process, @Nullable JvmStats jvm,
			@Nullable ThreadPoolStats threadPool, @Nullable NetworkStats network, @Nullable FsStats fs,
			@Nullable TransportStats transport, @Nullable HttpStats http) {
		super(node);
		this.hostname = hostname;
		this.indices = indices;
		this.os = os;
		this.process = process;
		this.jvm = jvm;
		this.threadPool = threadPool;
		this.network = network;
		this.fs = fs;
		this.transport = transport;
		this.http = http;
	}

	/**
	 * Hostname.
	 *
	 * @return the string
	 */
	@Nullable
	public String hostname() {
		return this.hostname;
	}

	/**
	 * Gets the hostname.
	 *
	 * @return the hostname
	 */
	@Nullable
	public String getHostname() {
		return this.hostname;
	}

	/**
	 * Indices.
	 *
	 * @return the node indices stats
	 */
	@Nullable
	public NodeIndicesStats indices() {
		return this.indices;
	}

	/**
	 * Gets the indices.
	 *
	 * @return the indices
	 */
	@Nullable
	public NodeIndicesStats getIndices() {
		return indices();
	}

	/**
	 * Os.
	 *
	 * @return the os stats
	 */
	@Nullable
	public OsStats os() {
		return this.os;
	}

	/**
	 * Gets the os.
	 *
	 * @return the os
	 */
	@Nullable
	public OsStats getOs() {
		return os();
	}

	/**
	 * Process.
	 *
	 * @return the process stats
	 */
	@Nullable
	public ProcessStats process() {
		return process;
	}

	/**
	 * Gets the process.
	 *
	 * @return the process
	 */
	@Nullable
	public ProcessStats getProcess() {
		return process();
	}

	/**
	 * Jvm.
	 *
	 * @return the jvm stats
	 */
	@Nullable
	public JvmStats jvm() {
		return jvm;
	}

	/**
	 * Gets the jvm.
	 *
	 * @return the jvm
	 */
	@Nullable
	public JvmStats getJvm() {
		return jvm();
	}

	/**
	 * Thread pool.
	 *
	 * @return the thread pool stats
	 */
	@Nullable
	public ThreadPoolStats threadPool() {
		return this.threadPool;
	}

	/**
	 * Gets the thread pool.
	 *
	 * @return the thread pool
	 */
	@Nullable
	public ThreadPoolStats getThreadPool() {
		return threadPool();
	}

	/**
	 * Network.
	 *
	 * @return the network stats
	 */
	@Nullable
	public NetworkStats network() {
		return network;
	}

	/**
	 * Gets the network.
	 *
	 * @return the network
	 */
	@Nullable
	public NetworkStats getNetwork() {
		return network();
	}

	/**
	 * Fs.
	 *
	 * @return the fs stats
	 */
	@Nullable
	public FsStats fs() {
		return fs;
	}

	/**
	 * Gets the fs.
	 *
	 * @return the fs
	 */
	@Nullable
	public FsStats getFs() {
		return fs();
	}

	/**
	 * Transport.
	 *
	 * @return the transport stats
	 */
	@Nullable
	public TransportStats transport() {
		return this.transport;
	}

	/**
	 * Gets the transport.
	 *
	 * @return the transport
	 */
	@Nullable
	public TransportStats getTransport() {
		return transport();
	}

	/**
	 * Http.
	 *
	 * @return the http stats
	 */
	@Nullable
	public HttpStats http() {
		return this.http;
	}

	/**
	 * Gets the http.
	 *
	 * @return the http
	 */
	@Nullable
	public HttpStats getHttp() {
		return http();
	}

	/**
	 * Read node stats.
	 *
	 * @param in the in
	 * @return the node stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static NodeStats readNodeStats(StreamInput in) throws IOException {
		NodeStats nodeInfo = new NodeStats();
		nodeInfo.readFrom(in);
		return nodeInfo;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		if (in.readBoolean()) {
			hostname = in.readUTF();
		}
		if (in.readBoolean()) {
			indices = NodeIndicesStats.readIndicesStats(in);
		}
		if (in.readBoolean()) {
			os = OsStats.readOsStats(in);
		}
		if (in.readBoolean()) {
			process = ProcessStats.readProcessStats(in);
		}
		if (in.readBoolean()) {
			jvm = JvmStats.readJvmStats(in);
		}
		if (in.readBoolean()) {
			threadPool = ThreadPoolStats.readThreadPoolStats(in);
		}
		if (in.readBoolean()) {
			network = NetworkStats.readNetworkStats(in);
		}
		if (in.readBoolean()) {
			fs = FsStats.readFsStats(in);
		}
		if (in.readBoolean()) {
			transport = TransportStats.readTransportStats(in);
		}
		if (in.readBoolean()) {
			http = HttpStats.readHttpStats(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		if (hostname == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(hostname);
		}
		if (indices == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			indices.writeTo(out);
		}
		if (os == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			os.writeTo(out);
		}
		if (process == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			process.writeTo(out);
		}
		if (jvm == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			jvm.writeTo(out);
		}
		if (threadPool == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			threadPool.writeTo(out);
		}
		if (network == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			network.writeTo(out);
		}
		if (fs == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			fs.writeTo(out);
		}
		if (transport == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			transport.writeTo(out);
		}
		if (http == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			http.writeTo(out);
		}
	}
}