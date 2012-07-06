/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SimilarityProviderFactory.java 2012-7-6 14:30:33 l.xue.nong$$
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
