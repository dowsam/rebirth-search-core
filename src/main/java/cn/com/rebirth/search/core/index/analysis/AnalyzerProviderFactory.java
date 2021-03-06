/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AnalyzerProviderFactory.java 2012-7-6 14:30:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;

/**
 * A factory for creating AnalyzerProvider objects.
 */
public interface AnalyzerProviderFactory {

	/**
	 * Creates the.
	 *
	 * @param name the name
	 * @param settings the settings
	 * @return the analyzer provider
	 */
	AnalyzerProvider create(String name, Settings settings);
}
