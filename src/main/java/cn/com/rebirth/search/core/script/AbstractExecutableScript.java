/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractExecutableScript.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.script;

/**
 * The Class AbstractExecutableScript.
 *
 * @author l.xue.nong
 */
public abstract class AbstractExecutableScript implements ExecutableScript {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.script.ExecutableScript#setNextVar(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setNextVar(String name, Object value) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.script.ExecutableScript#unwrap(java.lang.Object)
	 */
	@Override
	public Object unwrap(Object value) {
		return value;
	}
}