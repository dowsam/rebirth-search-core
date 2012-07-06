/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractDoubleSearchScript.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.script;

/**
 * The Class AbstractDoubleSearchScript.
 *
 * @author l.xue.nong
 */
public abstract class AbstractDoubleSearchScript extends AbstractSearchScript {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.script.ExecutableScript#run()
	 */
	@Override
	public Object run() {
		return runAsDouble();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.script.AbstractSearchScript#runAsDouble()
	 */
	@Override
	public abstract double runAsDouble();

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.script.AbstractSearchScript#runAsLong()
	 */
	@Override
	public long runAsLong() {
		return (long) runAsDouble();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.script.AbstractSearchScript#runAsFloat()
	 */
	@Override
	public float runAsFloat() {
		return (float) runAsDouble();
	}
}