/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DumpContributorFactory.java 2012-3-29 15:01:42 l.xue.nong$$
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
