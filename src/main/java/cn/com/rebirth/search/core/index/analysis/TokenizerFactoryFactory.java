/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TokenizerFactoryFactory.java 2012-3-29 15:00:44 l.xue.nong$$
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
