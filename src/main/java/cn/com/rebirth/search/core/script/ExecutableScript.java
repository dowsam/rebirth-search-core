/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ExecutableScript.java 2012-7-6 14:30:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.script;

/**
 * The Interface ExecutableScript.
 *
 * @author l.xue.nong
 */
public interface ExecutableScript {

	/**
	 * Sets the next var.
	 *
	 * @param name the name
	 * @param value the value
	 */
	void setNextVar(String name, Object value);

	/**
	 * Run.
	 *
	 * @return the object
	 */
	Object run();

	/**
	 * Unwrap.
	 *
	 * @param value the value
	 * @return the object
	 */
	Object unwrap(Object value);
}
