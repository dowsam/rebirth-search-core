/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverClusterStateListener.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river.cluster;

/**
 * The listener interface for receiving riverClusterState events.
 * The class that is interested in processing a riverClusterState
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addRiverClusterStateListener<code> method. When
 * the riverClusterState event occurs, that object's appropriate
 * method is invoked.
 *
 * @see RiverClusterStateEvent
 */
public interface RiverClusterStateListener {

	/**
	 * River cluster changed.
	 *
	 * @param event the event
	 */
	void riverClusterChanged(RiverClusterChangedEvent event);
}
