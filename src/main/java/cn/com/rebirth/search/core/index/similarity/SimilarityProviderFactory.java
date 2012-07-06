/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SimilarityProviderFactory.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.similarity;

import cn.com.rebirth.commons.settings.Settings;


/**
 * A factory for creating SimilarityProvider objects.
 */
public interface SimilarityProviderFactory {

	
	/**
	 * Creates the.
	 *
	 * @param name the name
	 * @param settings the settings
	 * @return the similarity provider
	 */
	SimilarityProvider create(String name, Settings settings);
}
