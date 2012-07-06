/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FilterParserFactory.java 2012-3-29 15:02:01 l.xue.nong$$
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