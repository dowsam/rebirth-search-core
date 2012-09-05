/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeBuilder.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.node;

import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.node.internal.InternalNode;

/**
 * The Class NodeBuilder.
 *
 * @author l.xue.nong
 */
public class NodeBuilder {

	/** The settings. */
	private final ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();

	/** The load config settings. */
	private boolean loadConfigSettings = true;

	/**
	 * Node builder.
	 *
	 * @return the node builder
	 */
	public static NodeBuilder nodeBuilder() {
		return new NodeBuilder();
	}

	/**
	 * Settings.
	 *
	 * @return the immutable settings. builder
	 */
	public ImmutableSettings.Builder settings() {
		return settings;
	}

	/**
	 * Gets the settings.
	 *
	 * @return the settings
	 */
	public ImmutableSettings.Builder getSettings() {
		return settings;
	}

	/**
	 * Settings.
	 *
	 * @param settings the settings
	 * @return the node builder
	 */
	public NodeBuilder settings(Settings.Builder settings) {
		return settings(settings.build());
	}

	/**
	 * Settings.
	 *
	 * @param settings the settings
	 * @return the node builder
	 */
	public NodeBuilder settings(Settings settings) {
		this.settings.put(settings);
		return this;
	}

	/**
	 * Load config settings.
	 *
	 * @param loadConfigSettings the load config settings
	 * @return the node builder
	 */
	public NodeBuilder loadConfigSettings(boolean loadConfigSettings) {
		this.loadConfigSettings = loadConfigSettings;
		return this;
	}

	/**
	 * Client.
	 *
	 * @param client the client
	 * @return the node builder
	 */
	public NodeBuilder client(boolean client) {
		settings.put("node.client", client);
		return this;
	}

	/**
	 * Data.
	 *
	 * @param data the data
	 * @return the node builder
	 */
	public NodeBuilder data(boolean data) {
		settings.put("node.data", data);
		return this;
	}

	/**
	 * Local.
	 *
	 * @param local the local
	 * @return the node builder
	 */
	public NodeBuilder local(boolean local) {
		settings.put("node.local", local);
		return this;
	}

	/**
	 * Cluster name.
	 *
	 * @param clusterName the cluster name
	 * @return the node builder
	 */
	public NodeBuilder clusterName(String clusterName) {
		settings.put("cluster.name", clusterName);
		return this;
	}

	/**
	 * Builds the.
	 *
	 * @return the node
	 */
	public Node build() {
		return new InternalNode(settings.build(), loadConfigSettings);
	}

	/**
	 * Node.
	 *
	 * @return the node
	 */
	public Node node() {
		return build().start();
	}
}
