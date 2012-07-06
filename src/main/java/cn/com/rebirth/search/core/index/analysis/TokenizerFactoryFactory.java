/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TokenizerFactoryFactory.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;

/**
 * A factory for creating TokenizerFactory objects.
 */
public interface TokenizerFactoryFactory {

	/**
	 * Creates the.
	 *
	 * @param name the name
	 * @param settings the settings
	 * @return the tokenizer factory
	 */
	TokenizerFactory create(String name, Settings settings);
}
