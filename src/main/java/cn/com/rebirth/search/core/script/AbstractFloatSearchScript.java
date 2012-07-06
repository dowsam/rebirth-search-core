/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractFloatSearchScript.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.script;


/**
 * The Class AbstractFloatSearchScript.
 *
 * @author l.xue.nong
 */
public abstract class AbstractFloatSearchScript extends AbstractSearchScript {

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.script.ExecutableScript#run()
     */
    @Override
    public Object run() {
        return runAsFloat();
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.script.AbstractSearchScript#runAsFloat()
     */
    @Override
    public abstract float runAsFloat();

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.script.AbstractSearchScript#runAsDouble()
     */
    @Override
    public double runAsDouble() {
        return runAsFloat();
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.script.AbstractSearchScript#runAsLong()
     */
    @Override
    public long runAsLong() {
        return (long) runAsFloat();
    }
}