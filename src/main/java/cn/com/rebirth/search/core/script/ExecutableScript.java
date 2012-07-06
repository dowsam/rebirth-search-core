/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ExecutableScript.java 2012-3-29 15:00:57 l.xue.nong$$
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
