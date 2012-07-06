/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeInfo.java 2012-7-6 14:29:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.info;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.action.support.nodes.NodeOperationResponse;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.http.HttpInfo;
import cn.com.rebirth.search.core.monitor.jvm.JvmInfo;
import cn.com.rebirth.search.core.monitor.network.NetworkInfo;
import cn.com.rebirth.search.core.monitor.os.OsInfo;
import cn.com.rebirth.search.core.monitor.process.ProcessInfo;
import cn.com.rebirth.search.core.threadpool.ThreadPoolInfo;
import cn.com.rebirth.search.core.transport.TransportInfo;

import com.google.common.collect.ImmutableMap;

/**
 * The Class NodeInfo.
 *
 * @author l.xue.nong
 */
public class NodeInfo extends NodeOperationResponse {

	/** The service attributes. */
	@Nullable
	private ImmutableMap<String, String> serviceAttributes;

	/** The hostname. */
	@Nullable
	private String hostname;

	/** The settings. */
	@Nullable
	private Settings settings;

	/** The os. */
	@Nullable
	private OsInfo os;

	/** The process. */
	@Nullable
	private ProcessInfo process;

	/** The jvm. */
	@Nullable
	private JvmInfo jvm;

	/** The thread pool. */
	@Nullable
	private ThreadPoolInfo threadPool;

	/** The network. */
	@Nullable
	private NetworkInfo network;

	/** The transport. */
	@Nullable
	private TransportInfo transport;

	/** The http. */
	@Nullable
	private HttpInfo http;

	/**
	 * Instantiates a new node info.
	 */
	NodeInfo() {
	}

	/**
	 * Instantiates a new node info.
	 *
	 * @param hostname the hostname
	 * @param node the node
	 * @param serviceAttributes the service attributes
	 * @param settings the settings
	 * @param os the os
	 * @param process the process
	 * @param jvm the jvm
	 * @param threadPool the thread pool
	 * @param network the network
	 * @param transport the transport
	 * @param http the http
	 */
	public NodeInfo(@Nullable String hostname, DiscoveryNode node,
			@Nullable ImmutableMap<String, String> serviceAttributes, @Nullable Settings settings, @Nullable OsInfo os,
			@Nullable ProcessInfo process, @Nullable JvmInfo jvm, @Nullable ThreadPoolInfo threadPool,
			@Nullable NetworkInfo network, @Nullable TransportInfo transport, @Nullable HttpInfo http) {
		super(node);
		this.hostname = hostname;
		this.serviceAttributes = serviceAttributes;
		this.settings = settings;
		this.os = os;
		this.process = process;
		this.jvm = jvm;
		this.threadPool = threadPool;
		this.network = network;
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
		return hostname();
	}

	/**
	 * Service attributes.
	 *
	 * @return the immutable map
	 */
	@Nullable
	public ImmutableMap<String, String> serviceAttributes() {
		return this.serviceAttributes;
	}

	/**
	 * Gets the service attributes.
	 *
	 * @return the service attributes
	 */
	@Nullable
	public ImmutableMap<String, String> getServiceAttributes() {
		return serviceAttributes();
	}

	/**
	 * Settings.
	 *
	 * @return the settings
	 */
	@Nullable
	public Settings settings() {
		return this.settings;
	}

	/**
	 * Gets the settings.
	 *
	 * @return the settings
	 */
	@Nullable
	public Settings getSettings() {
		return settings();
	}

	/**
	 * Os.
	 *
	 * @return the os info
	 */
	@Nullable
	public OsInfo os() {
		return this.os;
	}

	/**
	 * Gets the os.
	 *
	 * @return the os
	 */
	@Nullable
	public OsInfo getOs() {
		return os();
	}

	/**
	 * Process.
	 *
	 * @return the process info
	 */
	@Nullable
	public ProcessInfo process() {
		return process;
	}

	/**
	 * Gets the process.
	 *
	 * @return the process
	 */
	@Nullable
	public ProcessInfo getProcess() {
		return process();
	}

	/**
	 * Jvm.
	 *
	 * @return the jvm info
	 */
	@Nullable
	public JvmInfo jvm() {
		return jvm;
	}

	/**
	 * Gets the jvm.
	 *
	 * @return the jvm
	 */
	@Nullable
	public JvmInfo getJvm() {
		return jvm();
	}

	/**
	 * Thread pool.
	 *
	 * @return the thread pool info
	 */
	@Nullable
	public ThreadPoolInfo threadPool() {
		return this.threadPool;
	}

	/**
	 * Gets the thread pool.
	 *
	 * @return the thread pool
	 */
	@Nullable
	public ThreadPoolInfo getThreadPool() {
		return threadPool();
	}

	/**
	 * Network.
	 *
	 * @return the network info
	 */
	@Nullable
	public NetworkInfo network() {
		return network;
	}

	/**
	 * Gets the network.
	 *
	 * @return the network
	 */
	@Nullable
	public NetworkInfo getNetwork() {
		return network();
	}

	/**
	 * Transport.
	 *
	 * @return the transport info
	 */
	@Nullable
	public TransportInfo transport() {
		return transport;
	}

	/**
	 * Gets the transport.
	 *
	 * @return the transport
	 */
	@Nullable
	public TransportInfo getTransport() {
		return transport();
	}

	/**
	 * Http.
	 *
	 * @return the http info
	 */
	@Nullable
	public HttpInfo http() {
		return http;
	}

	/**
	 * Gets the http.
	 *
	 * @return the http
	 */
	@Nullable
	public HttpInfo getHttp() {
		return http();
	}

	/**
	 * Read node info.
	 *
	 * @param in the in
	 * @return the node info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static NodeInfo readNodeInfo(StreamInput in) throws IOException {
		NodeInfo nodeInfo = new NodeInfo();
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
			ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
			int size = in.readVInt();
			for (int i = 0; i < size; i++) {
				builder.put(in.readUTF(), in.readUTF());
			}
			serviceAttributes = builder.build();
		}
		if (in.readBoolean()) {
			settings = ImmutableSettings.readSettingsFromStream(in);
		}
		if (in.readBoolean()) {
			os = OsInfo.readOsInfo(in);
		}
		if (in.readBoolean()) {
			process = ProcessInfo.readProcessInfo(in);
		}
		if (in.readBoolean()) {
			jvm = JvmInfo.readJvmInfo(in);
		}
		if (in.readBoolean()) {
			threadPool = ThreadPoolInfo.readThreadPoolInfo(in);
		}
		if (in.readBoolean()) {
			network = NetworkInfo.readNetworkInfo(in);
		}
		if (in.readBoolean()) {
			transport = TransportInfo.readTransportInfo(in);
		}
		if (in.readBoolean()) {
			http = HttpInfo.readHttpInfo(in);
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
		if (serviceAttributes() == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeVInt(serviceAttributes.size());
			for (Map.Entry<String, String> entry : serviceAttributes.entrySet()) {
				out.writeUTF(entry.getKey());
				out.writeUTF(entry.getValue());
			}
		}
		if (settings == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			ImmutableSettings.writeSettingsToStream(settings, out);
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
