/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DumpContributor.java 2012-3-29 15:00:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.dump;


/**
 * The Interface DumpContributor.
 *
 * @author l.xue.nong
 */
public interface DumpContributor {

	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	String getName();

	
	/**
	 * Contribute.
	 *
	 * @param dump the dump
	 * @throws DumpContributionFailedException the dump contribution failed exception
	 */
	void contribute(Dump dump) throws DumpContributionFailedException;
}
