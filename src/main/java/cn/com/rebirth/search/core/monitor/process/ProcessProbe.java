/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ProcessProbe.java 2012-7-6 14:28:50 l.xue.nong$$
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
