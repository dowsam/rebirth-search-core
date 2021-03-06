/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TimeoutClusterStateListener.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster;

import cn.com.rebirth.commons.unit.TimeValue;

/**
 * The listener interface for receiving timeoutClusterState events.
 * The class that is interested in processing a timeoutClusterState
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTimeoutClusterStateListener<code> method. When
 * the timeoutClusterState event occurs, that object's appropriate
 * method is invoked.
 *
 * @see TimeoutClusterStateEvent
 */
public interface TimeoutClusterStateListener extends ClusterStateListener {

	/**
	 * Post added.
	 */
	void postAdded();

	/**
	 * On close.
	 */
	void onClose();

	/**
	 * On timeout.
	 *
	 * @param timeout the timeout
	 */
	void onTimeout(TimeValue timeout);
}
