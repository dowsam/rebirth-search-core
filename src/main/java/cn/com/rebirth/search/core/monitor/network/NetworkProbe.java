/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NetworkProbe.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.network;

/**
 * The Interface NetworkProbe.
 *
 * @author l.xue.nong
 */
public interface NetworkProbe {

	/**
	 * Network info.
	 *
	 * @return the network info
	 */
	NetworkInfo networkInfo();

	/**
	 * Network stats.
	 *
	 * @return the network stats
	 */
	NetworkStats networkStats();

	/**
	 * Ifconfig.
	 *
	 * @return the string
	 */
	String ifconfig();
}
