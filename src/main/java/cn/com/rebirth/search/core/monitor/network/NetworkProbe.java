/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NetworkProbe.java 2012-3-29 15:01:29 l.xue.nong$$
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
