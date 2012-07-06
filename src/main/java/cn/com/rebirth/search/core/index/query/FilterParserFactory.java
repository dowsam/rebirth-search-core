/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilterParserFactory.java 2012-7-6 14:30:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import cn.com.rebirth.commons.settings.Settings;

/**
 * A factory for creating FilterParser objects.
 */
public interface FilterParserFactory {

	/**
	 * Creates the.
	 *
	 * @param name the name
	 * @param settings the settings
	 * @return the filter parser
	 */
	FilterParser create(String name, Settings settings);
}