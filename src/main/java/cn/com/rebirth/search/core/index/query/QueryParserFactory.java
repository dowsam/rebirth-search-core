/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryParserFactory.java 2012-3-29 15:00:55 l.xue.nong$$
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
