/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Node.java 2012-3-29 15:00:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.node;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Interface Node.
 *
 * @author l.xue.nong
 */
public interface Node {

	/**
	 * Settings.
	 *
	 * @return the settings
	 */
	Settings settings();

	/**
	 * Client.
	 *
	 * @return the client
	 */
	Client client();

	/**
	 * Start.
	 *
	 * @return the node
	 */
	Node start();

	/**
	 * Stop.
	 *
	 * @return the node
	 */
	Node stop();

	/**
	 * Close.
	 */
	void close();

	/**
	 * Checks if is closed.
	 *
	 * @return true, if is closed
	 */
	boolean isClosed();
}
