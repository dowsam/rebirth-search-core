/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SimilarityProvider.java 2012-7-6 14:30:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.similarity;

import org.apache.lucene.search.Similarity;

import cn.com.rebirth.core.inject.Provider;
import cn.com.rebirth.search.core.index.IndexComponent;

/**
 * The Interface SimilarityProvider.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface SimilarityProvider<T extends Similarity> extends IndexComponent, Provider<T> {

	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.Provider#get()
	 */
	T get();
}
