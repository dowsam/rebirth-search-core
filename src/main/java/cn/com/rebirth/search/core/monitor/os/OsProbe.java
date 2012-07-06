/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OsProbe.java 2012-7-6 14:30:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.os;

/**
 * The Interface OsProbe.
 *
 * @author l.xue.nong
 */
public interface OsProbe {

	/**
	 * Os info.
	 *
	 * @return the os info
	 */
	OsInfo osInfo();

	/**
	 * Os stats.
	 *
	 * @return the os stats
	 */
	OsStats osStats();
}
