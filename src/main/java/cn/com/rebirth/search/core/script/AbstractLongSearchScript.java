/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractLongSearchScript.java 2012-3-29 15:02:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.script;


/**
 * The Class AbstractLongSearchScript.
 *
 * @author l.xue.nong
 */
public abstract class AbstractLongSearchScript extends AbstractSearchScript {

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.script.ExecutableScript#run()
     */
    @Override
    public Object run() {
        return runAsLong();
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.script.AbstractSearchScript#runAsLong()
     */
    @Override
    public abstract long runAsLong();

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.script.AbstractSearchScript#runAsDouble()
     */
    @Override
    public double runAsDouble() {
        return runAsLong();
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.script.AbstractSearchScript#runAsFloat()
     */
    @Override
    public float runAsFloat() {
        return runAsLong();
    }
}