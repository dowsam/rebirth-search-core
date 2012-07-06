/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ProcessProbe.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.process;


/**
 * The Interface ProcessProbe.
 *
 * @author l.xue.nong
 */
public interface ProcessProbe {

    
    /**
     * Process info.
     *
     * @return the process info
     */
    ProcessInfo processInfo();

    
    /**
     * Process stats.
     *
     * @return the process stats
     */
    ProcessStats processStats();
}
