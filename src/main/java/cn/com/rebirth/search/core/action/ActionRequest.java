/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ActionRequest.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Interface ActionRequest.
 *
 * @author l.xue.nong
 */
public interface ActionRequest extends Streamable {

	/**
	 * Validate.
	 *
	 * @return the action request validation exception
	 */
	ActionRequestValidationException validate();

	/**
	 * Listener threaded.
	 *
	 * @return true, if successful
	 */
	boolean listenerThreaded();

	/**
	 * Listener threaded.
	 *
	 * @param listenerThreaded the listener threaded
	 * @return the action request
	 */
	ActionRequest listenerThreaded(boolean listenerThreaded);
}
