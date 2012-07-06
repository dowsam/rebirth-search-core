/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core OsProbe.java 2012-3-29 15:01:30 l.xue.nong$$
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
