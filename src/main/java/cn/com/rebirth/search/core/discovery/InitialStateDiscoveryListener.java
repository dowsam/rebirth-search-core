/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InitialStateDiscoveryListener.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery;

/**
 * The listener interface for receiving initialStateDiscovery events.
 * The class that is interested in processing a initialStateDiscovery
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addInitialStateDiscoveryListener<code> method. When
 * the initialStateDiscovery event occurs, that object's appropriate
 * method is invoked.
 *
 * @see InitialStateDiscoveryEvent
 */
public interface InitialStateDiscoveryListener {

	/**
	 * Initial state processed.
	 */
	void initialStateProcessed();
}
