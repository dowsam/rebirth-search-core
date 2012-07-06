/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DumpContributor.java 2012-7-6 14:29:25 l.xue.nong$$
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
