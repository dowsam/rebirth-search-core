/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryParserFactory.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import cn.com.rebirth.commons.settings.Settings;

/**
 * A factory for creating QueryParser objects.
 */
public interface QueryParserFactory {

	/**
	 * Creates the.
	 *
	 * @param name the name
	 * @param settings the settings
	 * @return the query parser
	 */
	QueryParser create(String name, Settings settings);
}
