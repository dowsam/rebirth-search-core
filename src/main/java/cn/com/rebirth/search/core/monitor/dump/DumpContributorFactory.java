/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DumpContributorFactory.java 2012-7-6 14:30:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.dump;

import cn.com.rebirth.commons.settings.Settings;

/**
 * A factory for creating DumpContributor objects.
 */
public interface DumpContributorFactory {

	/**
	 * Creates the.
	 *
	 * @param name the name
	 * @param settings the settings
	 * @return the dump contributor
	 */
	DumpContributor create(String name, Settings settings);
}
